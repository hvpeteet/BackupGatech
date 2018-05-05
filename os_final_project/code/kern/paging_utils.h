// 2GB
#define SWAPSIZE (128 << 20)
#define TESTING true
#define SECTOR_SIZE 512
#define PAGE_SIZE 4096

#define PTE_TO_BLOCKNO(pte) ((pte>>12) & ~(-1 << (32-12)))
#define PTE_ON_DISK 0x400
#define BLOCK_SIZE PAGE_SIZE
#define BLOCKNO2SECTOR(blockno) (blockno * BLOCK_SIZE/SECTOR_SIZE)
#define NBLOCKS (SWAPSIZE/BLOCK_SIZE)

#define PAGING_ENABLED 1

extern unsigned char paging_utils_initialized;

// Info to keep about each physical frame
typedef struct rlt_entry {
    void* va;
    uint32_t envid;
    struct rlt_entry* next;
    // more metadata
    char lru_count;
} RLT_ENTRY;

void util_init(void);
void page_fault_intercept(pde_t* pgdir, uint32_t fault_va);
struct PageInfo* handle_out_of_pages(int alloc_flags);
RLT_ENTRY* remove_victim_page();
int alloc_blockno(uint32_t* blockno);
void free_blockno(uint32_t blocno);
void page_to_disk(pde_t* pgdir, void* va, uint32_t blockno);
void page_from_disk(void* va, uint32_t blockno, uint32_t pa);
int block_from_disk(void* va, uint32_t blockno);
int block_to_disk(void* va, uint32_t blockno);
void test_paging_utils();
void test_alloc_block();
void test_block_io();
void env_destroy_cleanup(uint32_t envid);
void on_page_alloc_success(void* va, uint32_t envid, uint32_t pa);