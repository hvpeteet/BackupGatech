#include <stdlib.h>
#include <stdio.h>
#include "threadsafeLinkedList.h"

linked_list* initLinkedList() {
    linked_list* list = calloc(sizeof(linked_list), 1);
    if (NULL == list) {
        printf("MALLOC_FAILURE");
        exit(MALLOC_FAILURE);
    }
    list -> list_not_empty = malloc(sizeof(pthread_cond_t));
    list -> lock = malloc(sizeof(pthread_mutex_t));
    if (list->lock == NULL || list->list_not_empty == NULL) {
        printf("MALLOC_FAILURE");
        exit(MALLOC_FAILURE);
    }
    pthread_cond_init(list->list_not_empty, NULL);
    pthread_mutex_init(list->lock, NULL);
    return list;
}

// Does not free the pointed to data.
void freeLinkedList(linked_list* list) {
    node* current = list -> head;
    node* old = NULL;
    while(current != NULL) {
        old = current;
        current = current->next;
        free(old);
    }
    pthread_mutex_destroy(list->lock);
    pthread_cond_destroy(list->list_not_empty);
    free(list->list_not_empty);
    free(list->lock);
    free(list);
}

int createNode(node** ppnode, void* data) {
    node* newNode = malloc(sizeof(node));
    if (newNode == NULL) {
        return MALLOC_FAILURE;
    }
    newNode -> next = NULL;
    newNode -> prev = NULL;
    newNode -> data = data;
    *ppnode = newNode;
    return SUCCESS;
}

void applyFunc(linked_list* list, data_processor func) {
    lock_list(list);
    node* current = list -> head;
    while(current != NULL) {
        func(current -> data);
        current = current->next;
    }
    unlock_list(list);
}

int addToHead(linked_list* list, void* data) {
    lock_list(list);
    node* newNode;
    int could_not_create = createNode(&newNode, data);
    if (could_not_create) {
        // Could not create node (probably malloc failure).
    } else if (list -> head == NULL) {
        list -> head = newNode;
        list -> tail = newNode;
        list -> size = 1;
    } else {
        newNode -> next = list -> head;
        list -> head -> prev = newNode;
        list -> head = newNode;
        list -> size++;
    }
    printf("Thread %ld added element %d to head\n", (long int) pthread_self(), *((int*)data));
    unlock_list(list);
    pthread_cond_signal(list->list_not_empty);
    return SUCCESS;
}

int addToTail(linked_list* list, void* data) {
    lock_list(list);
    node* newNode;
    int could_not_create = createNode(&newNode, data);
    if (could_not_create) {
        // Could not create node (probably malloc failure).
    } else if (list -> head == NULL) {
        list -> head = newNode;
        list -> tail = newNode;
        list -> size = 1;
    } else {
        newNode -> prev = list -> tail;
        list -> tail -> next = newNode;
        list -> tail = newNode;
        list -> size++;
    }
    printf("Thread %ld added element %d to tail\n", (long int) pthread_self(), *((int*)data));
    unlock_list(list);
    pthread_cond_signal(list->list_not_empty);
    return SUCCESS;
}

void* removeFromHead(linked_list* list) {
    lock_list(list);
    while(list->size == 0) {
        pthread_cond_wait(list->list_not_empty, list->lock);
    }
    node* oldHead = list -> head;
    list -> head = oldHead -> next;
    if (list -> head == NULL) {
        list -> tail = NULL;
    } else {
        list -> head -> prev = NULL;
    }
    list -> size--;
    printf("Thread %ld removed element %d from head\n\n", (long int) pthread_self(), *((int*)oldHead->data));
    unlock_list(list);
    void* data = oldHead -> data;
    free(oldHead);
    return data;
}

void* removeFromTail(linked_list* list) {
    lock_list(list);
    while(list->size == 0) {
        pthread_cond_wait(list->list_not_empty, list->lock);
    }
    node* oldTail = list -> tail;
    list -> tail = oldTail -> prev;
    if (list -> tail == NULL) {
        list -> head = NULL;
    } else {
        list -> tail -> next = NULL;
    }

    list -> size--;
    printf("Thread %ld removed element %d from tail\n", (long int) pthread_self(), *((int*)oldTail->data));
    unlock_list(list);
    void* data = oldTail -> data;
    free(oldTail);
    return data;
}

void lock_list(linked_list* list) {
    pthread_mutex_lock(list->lock);
}

void unlock_list(linked_list* list) {
    pthread_mutex_unlock(list->lock);
}