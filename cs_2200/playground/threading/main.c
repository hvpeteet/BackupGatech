#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include "threadsafeLinkedList.h"

typedef void* (*pthread_func_ptr) (void*);

typedef enum operation {
    ADD_FRONT,
    REMOVE_FRONT,
    ADD_BACK,
    REMOVE_BACK,
    SIZE
} OPERATION;

typedef struct {
    OPERATION op;
    int value;
    int expected;
} CASE;

void basicSanityTest();
void basicTest();
int runTestCase(CASE* c, linked_list* list);
void testApplyFunc();
void testMultiThreadedAdd();
void* testProducer(void* llist);
void* testConsumer(void* llist);

int main(void) {
    // basicTest();
    // testApplyFunc();
    testMultiThreadedAdd();
}

void intPrinter(void* data) {
    printf("%d, ", *((int*)data));
}

void testApplyFunc() {
    int vals[] = {1,2,3,4,5};
    linked_list* list = initLinkedList();
    for (int i = 0; i < sizeof(vals) / sizeof(vals[0]); i++) {
        addToTail(list, &(vals[i]));
    }
    printf("Should see :\n");
    for (int i = 0; i < sizeof(vals) / sizeof(vals[0]); i++) {
        printf("%d, ", vals[i]);
    }
    printf("\nActual:\n");
    applyFunc(list, intPrinter);
    printf("\n");
    freeLinkedList(list);
}

void basicTest() {
    CASE cases[] = {
        {ADD_FRONT, 2, SUCCESS},
        {ADD_FRONT, 1, SUCCESS},
        {ADD_FRONT, 0, SUCCESS},
        {ADD_BACK, 3, SUCCESS},
        {ADD_BACK, 4, SUCCESS},
        {SIZE, 0, 5},
        {REMOVE_FRONT, 0, 0},
        {REMOVE_FRONT, 0, 1},
        {REMOVE_BACK, 0, 4},
        {SIZE, 0, 2},
        {REMOVE_BACK, 0, 3},
        {REMOVE_BACK, 0, 2},
        {SIZE, 0, 0},
    };
    linked_list* list = initLinkedList();
    for (int i = 0; i < sizeof(cases) / sizeof(cases[0]); i++) {
        if (runTestCase(&(cases[i]), list)) {
            printf("failure on case: %d\n", i);
        }
    }
    freeLinkedList(list);
}

int runTestCase(CASE* c, linked_list* list) {
    int retval = 0;
    switch(c->op) {
        case ADD_FRONT:
            retval = addToHead(list, &(c->value));
            break;
        case ADD_BACK:
            retval = addToTail(list, &(c->value));
            break;
        case REMOVE_FRONT:
            retval = *((int*) removeFromHead(list));
            break;
        case REMOVE_BACK:
            retval = *((int*) removeFromTail(list));
            break;
        case SIZE:
            retval = list -> size;
            break;
    }
    if (retval != c->expected) {
        printf("failure, want %d, got %d\n", c->expected, retval);
        return FAILURE;
    }
    return SUCCESS;
}

void testMultiThreadedAdd() {
    printf("\n\nShould see messages in the correct order\n\n");
    
    int num_producers = 2;
    int num_consumers = 4;
    linked_list* list = initLinkedList();
    
    pthread_t* producers = malloc(sizeof(pthread_t) * num_producers);
    for (int i =0; i < num_producers; i++) {
        pthread_create(&(producers[i]), NULL, testProducer, list);
    }

    pthread_t* consumers = malloc(sizeof(pthread_t) * num_consumers);
    for (int i =0; i < num_consumers; i++) {
        pthread_create(&(consumers[i]), NULL, testConsumer, list);
    }

    for (int i =0; i < num_producers; i++) {
        pthread_join(producers[i], NULL);
    }

    for (int i =0; i < num_consumers; i++) {
        pthread_join(consumers[i], NULL);
    }

    free(producers);
    free(consumers);
    freeLinkedList(list);
}

void* testProducer(void* llist) {
    linked_list* list = (linked_list*) llist;
    for (int i = 0; i < 10; i++) {
        int* num = malloc(sizeof(int));
        if (NULL == num) {
            printf("malloc failed");
            exit(FAILURE);
        }
        *num = i;
        addToTail(list, num);
        // sleep(1);
    }
}

void* testConsumer(void* llist) {
    linked_list* list = (linked_list*) llist;
    for (int i = 0; i < 5; i++) {
        int* num = removeFromHead(list);
        free(num);
        sleep(1);
    }
}