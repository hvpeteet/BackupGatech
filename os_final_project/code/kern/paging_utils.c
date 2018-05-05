#include <inc/string.h>
#include <inc/partition.h>
#include <inc/env.h>
// remove the page mapping from env.h
#include <inc/stdio.h>
#include <kern/cpu.h>
#include <kern/ide.h>
#include <kern/paging_utils.h>
#include <inc/memlayout.h>
#include <kern/pmap.h>
#include <inc/x86.h>

#define curenv (thiscpu->cpu_env)
#define PHYS_MEM (64 << 20)
#define PHYS_FRAMES (PHYS_MEM / 4096)
#define PA_2_POOL_INDEX(pa) (pa >> 12)

int	envid2env(envid_t envid, struct Env **env_store, bool checkperm);


RLT_ENTRY reverse_lookup_pool[PHYS_FRAMES];
RLT_ENTRY* in_use_frames = NULL;

unsigned char paging_utils_initialized = 0;
uint32_t free_block_bitmap[NBLOCKS/32 + 1];

// Initialize the file system
void util_init(void) {
	memset(free_block_bitmap, -1, sizeof(free_block_bitmap));
   // Find a JOS disk.  Use the second IDE disk (number 1) if availabl
   if (ide_probe_disk1()) {
   	cprintf("using disk 1\n");
           ide_set_disk(1);
   } else {
           ide_set_disk(0);
   }
   cprintf("NBLOCKS=%d\n", NBLOCKS);
   cprintf("free_block_bitmap size = %d\n", sizeof(free_block_bitmap) << 3);
   paging_utils_initialized = true;
}

void on_page_alloc_success(void* va, uint32_t envid, uint32_t pa) {
	//cprintf("Allocated pa=0x%x, va=%p, envid=%x\n", pa, va, envid);
	RLT_ENTRY* new = &reverse_lookup_pool[PA_2_POOL_INDEX(pa)];
	new->va = va;
	new->envid = envid;

	// insert at tail
	new->next = NULL;
	if (in_use_frames == NULL) {
		in_use_frames = new;
	} else {
		RLT_ENTRY* tail = in_use_frames;
		while(tail->next != NULL) {
			tail = tail->next;
		}
		tail->next = new;
	}
}
// Is only called if the faulting va is on disk
// the sector is stored in the highest 24 bits of the pte
// 1) get a new physical frame (page_alloc) 
// 2) swap the page back in from disk
void page_fault_intercept(pde_t* pgdir, uint32_t fault_va) {
	//cprintf("intercepted page fault\n");
	fault_va = ROUNDDOWN(fault_va, PGSIZE);
	struct PageInfo* new_frame = page_alloc(0);
	if (new_frame == NULL) {
		// TODO: replace this with killing the faulting process and removing its pages from disk
		panic("could not get free frame (disk and memory are full)");
	}
	//cprintf("got frame\n");
	uint32_t pa = page2pa(new_frame);
	pte_t* table = KADDR(PTE_ADDR(curenv->env_pgdir[PDX(fault_va)]));
    pte_t* pte = &(table[PTX(fault_va)]);
    uint32_t blockno = PTE_TO_BLOCKNO(*pte);
	*pte = pa | ((*pte & 0xEFF) & ~PTE_ON_DISK) | PTE_P;
	//cprintf("messing with pgdirs\n");
	uint32_t old = rcr3();
	lcr3(PADDR(pgdir));
	page_from_disk((void*)fault_va, blockno, pa);
	lcr3(old);
	//cprintf("done intercepting\n");
}

// Picks a page to evict, sends it to disk and then calls page alloc again.
// returns null if we are out of swap space.
// 1) Pick a victim page
// 2) Pick a free block on disk (if none then return null)
// 3) Swap the victim to the block
// 4) Store the (blockno << 12) | PTE_ON_DISK into the pte
// 5) Call page alloc 
struct PageInfo* handle_out_of_pages(int alloc_flags) {
	static int evicted = 0;
	RLT_ENTRY* victim = remove_victim_page();
	if ((evicted % 100) == 0) {
		cprintf("Evicting env=%x va=%p, evicted=%d\n", victim->envid, victim->va, evicted++);
	}
	uint32_t blockno;
	int ret = alloc_blockno(&blockno);
	if (ret < 0) {
		cprintf("alloc blockno failed with error code %d\n", ret);
		return NULL;
	}
	struct Env* e = NULL;
	//cprintf("about to get env\n");
	envid2env(victim->envid, &e, 0);
	//cprintf("got env %p\n", e);
	page_to_disk(e->env_pgdir, victim->va, blockno);
	return page_alloc(alloc_flags);
}

// must pick a vitim that has only 1 reference (no paging out shared pages)
// returns a pointer to the beginning of the va for the page
RLT_ENTRY* remove_victim_page_fifo() {
	if (in_use_frames == NULL) {
		panic("no pages in memory but asking for victim, should never happen\n");
	}
	// FIFO implementation
	// TODO: check for multiple references

	RLT_ENTRY* head = in_use_frames;
	in_use_frames = head->next;
	head->next = NULL;
	return head;
}

// NOT WORKING YET
RLT_ENTRY* remove_victim_page_lru() {
	if (in_use_frames == NULL) {
		panic("no pages in memory but asking for victim, should never happen\n");
	}
	// LRU implementation
	// TODO: check for multiple references

	RLT_ENTRY* next = in_use_frames;
	RLT_ENTRY* lru_prev = NULL;
	RLT_ENTRY* lru = next;
	while (next->next != NULL) {
		// update lru count
		struct Env* e;
		envid2env(next->envid, &e, 0);
		pte_t* pte = pgdir_walk(e->env_pgdir, next->va, 0);
		if (*pte & PTE_A) {
			*pte = *pte & ~PTE_A;
			next->lru_count++;
		}
		if (next->next->lru_count < lru->lru_count) {
			lru = next->next;
			lru_prev = next;
		}
		next = next->next;
	}
	if (lru_prev == NULL) {
		in_use_frames = lru->next;
	} else {
		lru_prev->next = lru->next;
	}
	lru->next = NULL;
	return lru;

}

RLT_ENTRY* remove_victim_page()  {
	return remove_victim_page_fifo();
	// return remove_victim_page_lru();
}

void env_destroy_cleanup(uint32_t envid) {
	// cprintf("cleaning up after dead env\n");
	RLT_ENTRY* next = in_use_frames;
	while (next != NULL && next->envid == envid) {
		RLT_ENTRY* temp = next->next;
		next->next = NULL;
		next->envid = 0;
		next->va = NULL;
		next = temp;
	}
	if (next != NULL) {
		in_use_frames = next;
		RLT_ENTRY* prev = next;
		next = next->next;
		while (next != NULL) {
			if (next->envid == envid) {
				prev->next = next->next;
				// clear removed frame
				next->next = NULL;
				next->envid = 0;
				next->va = NULL;
				// iterate again with the new next value
				next = prev->next;
			} else {
				prev = next;
				next = next->next;
			}
		}
	}
	// cprintf("done cleaning up after dead env\n");
}

int alloc_blockno(uint32_t* blockno) {
	int i;
	unsigned char found = 0;
	for (i = 0; i < NBLOCKS; i++) {
		if (free_block_bitmap[i/32] & (1 << (i % 32))) {
			free_block_bitmap[i/32] &= ~(1 << (i % 32));
			found = 1;
			break;
		}
	}
	if (found) {
		*blockno = i;
		return 0;
	} else {
		return -1;
	}

}

void free_blockno(uint32_t blockno) {
	free_block_bitmap[blockno/32] |= (1 << (blockno % 32));
}

// Writes a page to the allocated block and
// sets the pte to (blockno << 12) | FLAGS | PTE_ON_DISK
// so we can later retrieve the page
void page_to_disk(pde_t* pgdir, void* va, uint32_t blockno) {	
	if (!paging_utils_initialized) {
		util_init();
	}
	//cprintf("in page_to_disk\n");
	va = ROUNDDOWN(va, PGSIZE);
	pte_t* pte = pgdir_walk(pgdir, va, 0);
	if (pte == NULL) {
		panic("page to disk called on non present page");
	}
	struct PageInfo* page_info = pa2page(PTE_ADDR(*pte));
	//cprintf("about to write to disk\n");
	uint32_t old = rcr3();
	lcr3(PADDR(pgdir));
	int ret = block_to_disk(va, blockno);
	lcr3(old);
	//cprintf("written to disk\n");
	if (ret < 0) {
		panic("page_to_disk failed with error: %e: %d on blockno=%x\n", ret, ret, blockno);
	}
	*pte = ((blockno << 12) | PTE_ON_DISK | (*pte & 0xEFF)) & ~(PTE_P);
	// cprintf("writing with flags = %x\n", *pte);
	if (page_info->pp_ref > 1) {
		panic("writing shared page to disk\n");
	}
	page_decref(page_info);
	//cprintf("page is now on disk\n");
}

// Sets up the pte for virtual address va to be mapped 
// to the physical address pa, restores the flags and
// read the page back in from disk.
// assumes that the pte has not been modified since writing to disk
// AKA it should currently hold
// (blockno << 12) | FLAGS | PTE_ON_DISK
void page_from_disk(void* va, uint32_t blockno, uint32_t pa) {
	//cprintf("page from disk\n");
	if (!paging_utils_initialized) {
		util_init();
	}
	va = ROUNDDOWN(va, PGSIZE);
	int ret = block_from_disk(va, blockno);
	if (ret < 0) {
		panic("page_from_disk failed with error: %e: %d, va=%p\n", ret, ret, va);
	}
	//cprintf("done reading from disk\n");
}

int block_from_disk(void* va, uint32_t blockno) {
	uint32_t sector = BLOCKNO2SECTOR(blockno);
	int ret = ide_read(sector, va, BLOCK_SIZE / SECTOR_SIZE);
	if (ret < 0) {
		cprintf("block_from_disk failed with error: %e: %d\n", ret, ret);
	}
	return ret;
}

int block_to_disk(void* va, uint32_t blockno) {
	uint32_t sector = BLOCKNO2SECTOR(blockno);
	int ret = ide_write(sector, va, BLOCK_SIZE / SECTOR_SIZE);
	if (ret < 0) {
		cprintf("block_to_disk failed with error: %e: %d sector %d\n", ret, ret, sector);
	}
	return ret;
}

void test_paging_utils() {
	util_init();
	cprintf("---------------------\n");
	cprintf("running test_alloc_block\n");
	cprintf("---------------------\n");
	test_alloc_block();
	cprintf("---------------------\n");
	cprintf("test_alloc_block passed\n");
	cprintf("---------------------\n");
	util_init();
	cprintf("---------------------\n");
	cprintf("running test_block_io\n");
	cprintf("---------------------\n");
	test_block_io();
	cprintf("---------------------\n");
	cprintf("test_block_io passed\n");
	cprintf("---------------------\n");
	util_init();
}

char test_page[PGSIZE * 2];

void test_block_io() {
	char* va = ROUNDDOWN(test_page + PGSIZE, PGSIZE);
	uint32_t b;
	int ret = alloc_blockno(&b);
	if (ret < 0) {
		panic("no blocks to allocate\n");
	}

	// Write to the page
	memset(va, 'a', PGSIZE);
	cprintf("page values set to %c\n", va[0]);
	int i = 0;
	for (i = 0; i < PGSIZE; i += 16) {
		if (va[i] != 'a') {
			panic("not all memory was set to 'a' with memset, index %d, value=\"%c\"\n", i, va[i]);
		}
	}

	// Store the page
	block_to_disk(va, b);

	// Write before restore
	memset(va, 'b', PGSIZE);
	cprintf("page values set to %c\n", va[0]);
	for (i = 0; i < PGSIZE; i += 16) {
		if (va[i] != 'b') {
			panic("not all memory was set to 'b' with memset, index %d, value=\"%c\"\n", i, va[i]);
		}
	}

	// Restore the page to all 'a'
	block_from_disk(va, b);
	cprintf("page values restored to %c\n", va[0]);
	for (i = 0; i < PGSIZE; i += 16) {
		if (va[i] != 'a') {
			panic("not all memory was restored, index %d, value=\"%c\"\n", i, va[i]);
		}
	}
}

// TODO: make this with panics instead of prints
void test_alloc_block() {
	int i = 0;
	uint32_t b;
	int num_blocks_to_test = 500;
	for (i = 0; i < num_blocks_to_test; i++) {
		int ret = alloc_blockno(&b);
		if (ret != 0) {
			panic("alloc bock failed on attempt %d with code %d\n", i, ret);
		}
		if (b != i) {
			panic("alloc block not giving contiguous blocknos i=%d blockno=%d\n", i, b);
		}
	}
	cprintf("test block alloc: allocated the first time, about to free\n");
	for (i = 0; i < num_blocks_to_test; i++) {
		free_blockno(i);
	}
	cprintf("test block alloc: free successful about to alloc again\n");
	for (i = 0; i < num_blocks_to_test; i++) {
		int ret = alloc_blockno(&b);
		if (ret != 0) {
			panic("alloc bock failed on attempt (after free) %d with code %d\n", i, ret);
		}
		if (b != i) {
			panic("alloc block not giving contiguous blocknos (after free) i=%d blockno=%d\n", i, b);
		}
	}
}
