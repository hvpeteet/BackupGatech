#include <pthread.h>

#define SUCCESS 0
#define FAILURE 1
#define MALLOC_FAILURE 2

typedef struct NODE {
    struct NODE* next;
    struct NODE* prev;
    void* data;
} node;

typedef struct LINKED_LIST {
    struct NODE* head;
    struct NODE* tail;
    int size;
    pthread_cond_t* list_not_empty;
    pthread_mutex_t* lock;
} linked_list;

typedef void (*data_processor) (void* data);

linked_list* initLinkedList();
int addToHead(linked_list* list, void* data);
int addToTail(linked_list* list, void* data);
void freeLinkedList(linked_list* list);
void applyFunc(linked_list* list, data_processor func);
void freeLinkedList(linked_list* list);
void* removeFromHead(linked_list* list);
void* removeFromTail(linked_list* list);

void unlock_list(linked_list* list);
void lock_list(linked_list* list);
