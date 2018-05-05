// implement fork from user space

#include <inc/string.h>
#include <inc/lib.h>

// PTE_COW marks copy-on-write page table entries.
// It is one of the bits explicitly allocated to user processes (PTE_AVAIL).
#define PTE_COW         0x800

//
// Custom page fault handler - if faulting page is copy-on-write,
// map in our own private writable copy.
//
static void
pgfault(struct UTrapframe *utf)
{
  void *addr = (void*)utf->utf_fault_va;
  uint32_t err = utf->utf_err;
  int r;
  envid_t e_id = sys_getenvid();

  // // Check that the faulting access was (1) a write, and (2) to a
  // // copy-on-write page.  If not, panic.
  // // Hint:
  // //   Use the read-only page table mappings at uvpt
  // //   (see <inc/memlayout.h>).

  // // LAB 4: Your code here.
  //cprintf("Calling pgfault");
  if (!(err & FEC_WR)) {
    panic("user page fault called on read (should be write)");
  }
  // if (!(uvpt[PGNUM(addr)] & PTE_COW)) {
  //   panic("\n----------------------------------\nuser page fault called on non COW page with: \nAddress %p \nEnvironment=%d", addr, e_id);
  // }

  // Allocate a new page, map it at a temporary location (PFTEMP),
  // copy the data from the old page to the new page, then move the new
  // page to the old page's address.
  // Hint:
  //   You should make three system calls.

  // LAB 4: Your code here.

  void* page_addr = ROUNDDOWN(addr, PGSIZE);
  
  int alloc_err = sys_page_alloc(e_id, PFTEMP, PTE_P | PTE_U | PTE_W);
  if (alloc_err) {
    panic("sys_page_alloc failed");
  }
  memcpy(PFTEMP, page_addr, PGSIZE);
  int map_err = sys_page_map(e_id, PFTEMP, 
                             e_id, page_addr, 
                             PTE_P | PTE_U | PTE_W);
  if (map_err) {
    panic("sys_page_map failed");
  }
  int umap_err = sys_page_unmap(e_id, PFTEMP);
  if (umap_err) {
    panic("sys_page_unmap failed");
  }
  return;
}

//
// Map our virtual page pn (address pn*PGSIZE) into the target envid
// at the same virtual address.  If the page is writable or copy-on-write,
// the new mapping must be created copy-on-write, and then our mapping must be
// marked copy-on-write as well.  (Exercise: Why do we need to mark ours
// copy-on-write again if it was already copy-on-write at the beginning of
// this function?)
//
// Returns: 0 on success, < 0 on error.
// It is also OK to panic on error.
//
static int
duppage(envid_t envid, unsigned pn)
{
  
  int r;

  // LAB 4: Your code here.
  envid_t e_id = sys_getenvid();
  //cprintf("in duppage(srcenv=%d, dstenv=%d, page=%x, addr=0x%x", e_id, envid, pn, pn*PGSIZE);

  void *addr = (void*) (pn * PGSIZE);
  if (((uvpt[pn] & PTE_W) || (uvpt[pn] & PTE_COW)) && !(uvpt[pn] & PTE_SHARE)) {
    // Make the mapping COW
    //cprintf("-------COW\n");
    int mapping_err;
    mapping_err = sys_page_map(e_id, addr, envid, addr, PTE_P | PTE_COW | PTE_U);
    if (mapping_err) {
      panic("mapping other environment's page to current env failed");
    }
    // mapping_err = sys_page_unmap(e_id, addr);
    // if (mapping_err) {
    //   panic("unmapping environment's page to self failed");
    // }
    mapping_err = sys_page_map(envid, addr, e_id, addr, PTE_P | PTE_COW | PTE_U);
    if (mapping_err) {
      panic("dst mapping back to src failed with error %e", mapping_err);
    }
  } else {
    // Normal mapping
    //cprintf("-------NORMAL\n");
    sys_page_map(0, PGADDR(0, pn, 0),
                 envid, PGADDR(0, pn, 0),
                 (uvpt[pn] & PTE_SYSCALL));
    // sys_page_map(e_id, addr, envid, addr, PTE_P | PTE_U);
  }
  return 0;
}

//
// User-level fork with copy-on-write.
// Set up our page fault handler appropriately.
// Create a child.
// Copy our address space and page fault handler setup to the child.
// Then mark the child as runnable and return.
//
// Returns: child's envid to the parent, 0 to the child, < 0 on error.
// It is also OK to panic on error.
//
// Hint:
//   Use uvpd, uvpt, and duppage.
//   Remember to fix "thisenv" in the child process.
//   Neither user exception stack should ever be marked copy-on-write,
//   so you must allocate a new page for the child's user exception stack.
//
envid_t
fork(void)
{
  extern void _pgfault_upcall();
  // LAB 4: Your code here.
  set_pgfault_handler(pgfault);

  envid_t envid = sys_exofork();
  if (envid == 0) {
    thisenv = &envs[ENVX(sys_getenvid())];
    return 0;
  }
  if (envid < 0) {
    panic("sys_exofork failed: %e", envid);
  }

  uint32_t addr;
  for (addr = 0; addr < USTACKTOP; addr += PGSIZE) {
    if ((uvpd[PDX(addr)] & PTE_P) && (uvpt[PGNUM(addr)] & PTE_P) && (uvpt[PGNUM(addr)] & PTE_U)) {
      duppage(envid, PGNUM(addr));
    }
  }
  int alloc_err = sys_page_alloc(envid, (void *)(UXSTACKTOP-PGSIZE), PTE_U|PTE_W|PTE_P);
  if (alloc_err) {
    panic("sys_page_alloc failed with error: %e", alloc_err);
  }
  sys_env_set_pgfault_upcall(envid, _pgfault_upcall);
  int set_status_err = sys_env_set_status(envid, ENV_RUNNABLE);
  if (set_status_err) {
    panic("sys_env_set_status");
  }
  return envid;
}

// Challenge!
int
sfork(void)
{
  panic("sfork not implemented");
  return -E_INVAL;
}
