/*
 * student.c
 * Multithreaded OS Simulation for CS 2200, Project 6
 * Fall 2015
 *
 * This file contains the CPU scheduler for the simulation.
 * Name:
 * GTID:
 */

#include <assert.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
 #include <string.h>

#include "os-sim.h"

typedef enum {
    FIFO,
    ROBIN,
    PRIORITY,
} schedule_t;

typedef struct {
    pcb_t* head;
    pcb_t* tail;
    int size;
} pcb_linked_list_t;

/*
 * current[] is an array of pointers to the currently running processes.
 * There is one array element corresponding to each CPU in the simulation.
 *
 * current[] should be updated by schedule() each time a process is scheduled
 * on a CPU.  Since the current[] array is accessed by multiple threads, you
 * will need to use a mutex to protect it.  current_mutex has been provided
 * for your use.
 */
static int cpu_count;
static pcb_t **current;
static pthread_mutex_t current_mutex;
static int time_slice;
static schedule_t sim_type;

static pcb_linked_list_t* ready_queue;
static pthread_cond_t ready_queue_not_empty;
static pthread_mutex_t ready_queue_mutex;


pcb_t* getNextProcess() { // pull off the front of the ready queue
    pthread_mutex_lock(&ready_queue_mutex);
    if (ready_queue -> head == NULL) {
        return NULL;
    }
    pcb_t* old_head = ready_queue->head;
    ready_queue->head = old_head->next;
    if (ready_queue->head == NULL) {
        ready_queue->tail = NULL;
    }
    ready_queue->size--;
    old_head->next = NULL;
    pthread_mutex_unlock(&ready_queue_mutex);
    return old_head;
}

void addProccess(pcb_t* newProcess) {
    pthread_mutex_lock(&ready_queue_mutex);
    if (ready_queue->tail == NULL) {
        ready_queue -> head = newProcess;
        ready_queue -> tail = newProcess;
        newProcess->next = NULL;
    } else {
        pcb_t* old_tail = ready_queue->tail;
        old_tail->next = newProcess;
        ready_queue->tail = newProcess;
        newProcess->next = NULL;
    }
    ready_queue -> size++;
    pthread_mutex_unlock(&ready_queue_mutex);
    pthread_cond_signal(&ready_queue_not_empty);
}

static void scheduleWorker(unsigned int cpu_id) {
    if (current[cpu_id] != NULL && current[cpu_id]->state == PROCESS_RUNNING) {
        return;
    }
    pcb_t* nextProcess = getNextProcess();
    // If there is a next process
    if (nextProcess == NULL) {
        current[cpu_id] = NULL;
        context_switch(cpu_id, NULL, time_slice);
    } else {
        nextProcess -> state = PROCESS_RUNNING;
        current[cpu_id] = nextProcess;
        context_switch(cpu_id, nextProcess, time_slice);
    }
}
/*
 * schedule() is your CPU scheduler.  It should perform the following tasks:
 *
 *   1. Select and remove a runnable process from your ready queue which
 *	you will have to implement with a linked list or something of the sort.
 *
 *   2. Set the process state to RUNNING
 *
 *   3. Call context_switch(), to tell the simulator which process to execute
 *      next on the CPU.  If no process is runnable, call context_switch()
 *      with a pointer to NULL to select the idle process.
 *	The current array (see above) is how you access the currently running
 *	process indexed by the cpu id. See above for full description.
 *	context_switch() is prototyped in os-sim.h. Look there for more information
 *	about it and its parameters.
 */
static void schedule(unsigned int cpu_id)
{
    pthread_mutex_lock(&current_mutex);
    scheduleWorker(cpu_id);
    pthread_mutex_unlock(&current_mutex);
}


/*
 * idle() is your idle process.  It is called by the simulator when the idle
 * process is scheduled.
 *
 * This function should block until a process is added to your ready queue.
 * It should then call schedule() to select the process to run on the CPU.
 */
extern void idle(unsigned int cpu_id)
{
    //while(ready_queue.size == 0) {
    // Don't need a while here since we want schedule to attempt to run each time.
    // Schedule will start idle back up if it could not get another process.
    pthread_cond_wait(&ready_queue_not_empty, &ready_queue_mutex);
    pthread_mutex_unlock(&ready_queue_mutex);
    //}
    schedule(cpu_id);
}


static void preemptWorker(unsigned int cpu_id) {
    if (current[cpu_id] != NULL) {
        // fprintf(stderr, "preempting process %s\n", current[cpu_id]->name);
        current[cpu_id]->state = PROCESS_READY;
        addProccess(current[cpu_id]);
    }
}
/*
 * preempt() is the handler called by the simulator when a process is
 * preempted due to its timeslice expiring.
 *
 * This function should place the currently running process back in the
 * ready queue, and call schedule() to select a new runnable process.
 */
extern void preempt(unsigned int cpu_id)
{
    pthread_mutex_lock(&current_mutex);
    preemptWorker(cpu_id);
    scheduleWorker(cpu_id);
    pthread_mutex_unlock(&current_mutex);
}


/*
 * yield() is the handler called by the simulator when a process yields the
 * CPU to perform an I/O request.
 *
 * It should mark the process as WAITING, then call schedule() to select
 * a new process for the CPU.
 */
extern void yield(unsigned int cpu_id)
{
    pthread_mutex_lock(&current_mutex);
    current[cpu_id] -> state = PROCESS_WAITING;
    pthread_mutex_unlock(&current_mutex);
    schedule(cpu_id);
}


/*
 * terminate() is the handler called by the simulator when a process completes.
 * It should mark the process as terminated, then call schedule() to select
 * a new process for the CPU.
 */
extern void terminate(unsigned int cpu_id)
{
    pthread_mutex_lock(&current_mutex);
    current[cpu_id] -> state = PROCESS_TERMINATED;
    pthread_mutex_unlock(&current_mutex);
    schedule(cpu_id);
}


/*
 * wake_up() is the handler called by the simulator when a process's I/O
 * request completes.  It should perform the following tasks:
 *
 *   1. Mark the process as READY, and insert it into the ready queue.
 *
 *   2. If the scheduling algorithm is static priority, wake_up() may need
 *      to preempt the CPU with the lowest priority process to allow it to
 *      execute the process which just woke up.  However, if any CPU is
 *      currently running idle, or all of the CPUs are running processes
 *      with a higher priority than the one which just woke up, wake_up()
 *      should not preempt any CPUs.
 *	To preempt a process, use force_preempt(). Look in os-sim.h for
 * 	its prototype and the parameters it takes in.
 */
extern void wake_up(pcb_t *process)
{
    // fprintf(stderr, "waking process %s\n", process->name);
    process -> state = PROCESS_READY;
    addProccess(process);
    if (sim_type == PRIORITY) {
        pthread_mutex_lock(&current_mutex);
        int min_cpu_id = -1;
        int min_priority = process -> static_priority;
        for (int i = 0; i < cpu_count; i++) {
            if (current[i] == NULL || current[i]->state != PROCESS_RUNNING) {
                min_cpu_id = i;
                break;
            }
            if (min_priority > current[i]->static_priority) {
                min_priority = current[i]->static_priority;
                min_cpu_id = i;
            }
        }
        if (min_cpu_id >= 0) {
            pthread_mutex_unlock(&current_mutex);
            force_preempt(min_cpu_id);
        } else {
            pthread_mutex_unlock(&current_mutex);
        }
    }
}


/*
 * main() simply parses command line arguments, then calls start_simulator().
 * You will need to modify it to support the -r and -p command-line parameters.
 */
int main(int argc, char *argv[])
{

    time_slice = -1;
    /* Parse command-line arguments */
    if (argc == 3 && strcmp(argv[2],"-p") == 0) {
        sim_type = PRIORITY;
        printf("Using priority scheduler\n");
    } else if (argc == 4 && strcmp(argv[2], "-r") == 0) {
        sim_type = ROBIN;
        time_slice = atoi(argv[3]);
        printf("Using Round-Robin scheduler\n");
    } else if (argc == 2) {
        sim_type = FIFO;
        printf("Using FIFO scheduler\n");
    } else {
        fprintf(stderr, "CS 2200 Project 4 -- Multithreaded OS Simulator\n"
            "Usage: ./os-sim <# CPUs> [ -r <time slice> | -p ]\n"
            "    Default : FIFO Scheduler\n"
            "         -r : Round-Robin Scheduler\n"
            "         -p : Static Priority Scheduler\n\n");
        return -1;
    }
    cpu_count = atoi(argv[1]);

    /* Allocate the current[] array and its mutex */
    current = calloc(sizeof(pcb_t*), cpu_count);
    ready_queue = calloc(sizeof(pcb_linked_list_t), 1);
    assert(current != NULL);
    pthread_mutex_init(&current_mutex, NULL);
    pthread_mutex_init(&ready_queue_mutex, NULL);
    pthread_cond_init(&ready_queue_not_empty, NULL);

    /* Start the simulator in the library */
    start_simulator(cpu_count);

    return 0;
}


