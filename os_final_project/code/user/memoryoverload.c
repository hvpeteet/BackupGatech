// Take up at least MEGS_TO_USE megabytes of memory
// Do this with NUM_PROCS processes
// TODO: make this a more comprehensive test, right now it just allocates, memsets and exits.
// Should emulate real large programs

#include <inc/string.h>
#include <inc/lib.h>


#define MEGS_TO_USE 64
#define NUM_PAGES ((MEGS_TO_USE << 20) / 4096)
#define NUM_PROCS 4
#define PAGES_PER_PROC (NUM_PAGES / NUM_PROCS)
#define BYTES_PER_PROC (PAGES_PER_PROC * 4096)


envid_t dumbfork(void);

char space[BYTES_PER_PROC];

void
umain(int argc, char **argv)
{
  envid_t who;
  int i;
  int to_be_parent_id = 0;
  int root_id = sys_getenvid();
  int child_id = 0;
  int parent_id = 0;
  // cprintf("root_id = %x\n", root_id);
  // fork a child process
  for (i = 0; (i < NUM_PROCS - 1) && !child_id; i++) { // each child runs this once
    int par = sys_getenvid();
    child_id = dumbfork();
    if (child_id == 0) {
      parent_id = par;
    }
  }
  cprintf("beginning writes\n");
  long j = 0;
  for (j = 0; j < BYTES_PER_PROC; j++) {
    space[j] = (char) j;
  }
  // cprintf("writes complete\n");
  if (child_id == 0) {
    cprintf("base case done in env %x\n", sys_getenvid());
    ipc_send(parent_id, 0, 0, 0);
  } else {
    // cprintf("%x waiting on %x\n", sys_getenvid(), child_id);
    uint32_t i = ipc_recv(&child_id, 0, 0);
    // cprintf("checking memory persisted\n");
    for (j = 0; j < BYTES_PER_PROC; j++) {
      if (space[j] != (char) j) {
        panic("memory was not consistant at block %d\n", j);
      }
    }
    // cprintf("memory persistance passed\n");
    if (sys_getenvid() != root_id) {
      cprintf("%x sending to %x\n", sys_getenvid(), parent_id);
      ipc_send(parent_id, 0, 0, 0);
    }
  }
}

void
duppage(envid_t dstenv, void *addr)
{
  int r;
  // cprintf("{");
  // This is NOT what you should do in your fork.
  if ((r = sys_page_alloc(dstenv, addr, PTE_P|PTE_U|PTE_W)) < 0)
    panic("sys_page_alloc: %e", r);
  if ((r = sys_page_map(dstenv, addr, 0, UTEMP, PTE_P|PTE_U|PTE_W)) < 0)
    panic("sys_page_map: %e", r);
  memmove(UTEMP, addr, PGSIZE);
  if ((r = sys_page_unmap(0, UTEMP)) < 0)
    panic("sys_page_unmap: %e", r);
  // cprintf("}");
}

envid_t
dumbfork(void)
{
  envid_t envid;
  uint8_t *addr;
  int r;
  extern unsigned char end[];

  // Allocate a new child environment.
  // The kernel will initialize it with a copy of our register state,
  // so that the child will appear to have called sys_exofork() too -
  // except that in the child, this "fake" call to sys_exofork()
  // will return 0 instead of the envid of the child.
  envid = sys_exofork();
  if (envid < 0)
    panic("sys_exofork: %e", envid);
  if (envid == 0) {
    // We're the child.
    // The copied value of the global variable 'thisenv'
    // is no longer valid (it refers to the parent!).
    // Fix it and return 0.
    thisenv = &envs[ENVX(sys_getenvid())];
    return 0;
  }

  // We're the parent.
  // Eagerly copy our entire address space into the child.
  // This is NOT what you should do in your fork implementation.
  for (addr = (uint8_t*)UTEXT; addr < end; addr += PGSIZE)
    duppage(envid, addr);

  // Also copy the stack we are currently running on.
  duppage(envid, ROUNDDOWN(&addr, PGSIZE));

  // Start the child environment running
  if ((r = sys_env_set_status(envid, ENV_RUNNABLE)) < 0)
    panic("sys_env_set_status: %e", r);

  return envid;
}

