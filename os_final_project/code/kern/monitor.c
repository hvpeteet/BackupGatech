// Simple command-line kernel monitor useful for
// controlling the kernel and exploring the system interactively.

#include <inc/stdio.h>
#include <inc/string.h>
#include <inc/memlayout.h>
#include <inc/assert.h>
#include <inc/x86.h>

#include <kern/console.h>
#include <kern/monitor.h>
#include <kern/kdebug.h>
#include <kern/trap.h>
#include <kern/pmap.h>

#define CMDBUF_SIZE 80 // enough for one VGA text line


struct Command {
  const char *name;
  const char *desc;
  // return -1 to force monitor to exit
  int (*func)(int argc, char **argv, struct Trapframe * tf);
};

static struct Command commands[] = {
  { "help",      "Display this list of commands",        mon_help       },
  { "info-kern", "Display information about the kernel", mon_infokern   },
  { "backtrace", "Backtrace through the stack (fun for ages 13-87)", mon_backtrace   },
  { "showmappings", "Show memmory mappings, \n\tuse:showmappings start-addr end-addr", showmappings   },
  { "permset", "Change user/kernel permissions, \n\tuse:permset start-addr end-addr [pwu]", permset   },
  { "memdump", "Dump memory, \n\tuse:memdump start-addr end-addr", memdump   },
  { "cont", "continue execution\n\tonly available if the monitor was entered via a int3 (debug)", cont},
  { "step", "step one instruction", step },
};
#define NCOMMANDS (sizeof(commands)/sizeof(commands[0]))

/***** Implementations of basic kernel monitor commands *****/
int 
showmappings(int argc, char** argv, struct Trapframe *tf) {
  if (argc != 3) {
    cprintf("Must have exactly 2 arguments\n");
    return 0;
  }
  uintptr_t start = (uintptr_t) strtol(argv[1], NULL, 0);
  uintptr_t end = (uintptr_t) strtol(argv[2], NULL, 0);
  cprintf("Showing addresses 0x%lx to 0x%lx\n", start, end);
  int virt_color, phys_color;
  virt_color = 0x05;
  phys_color = 0x03;
  cprintf("VIRT   --> PHYS\n", virt_color, phys_color);
  for( ; start <= end; start += PGSIZE) {
    cprintf("0x%lx --> ", virt_color, start);
    struct PageInfo* page = page_lookup(kern_pgdir, (void*) start, NULL);
    if (NULL == page) {
      cprintf("No mapping\n", phys_color);
    } else {
      physaddr_t physical = page2pa(page) + (start & (PGSIZE-1));
      cprintf("0x%lx\n", phys_color, physical);
    }
  }
  return 0;
}

int 
permset(int argc, char** argv, struct Trapframe *tf) {
  if (argc != 4) {
    cprintf("Must have exactly 3 arguments\n");
    return 0;
  }
  uintptr_t start = (uintptr_t) strtol(argv[1], NULL, 0);
  uintptr_t end = (uintptr_t) strtol(argv[2], NULL, 0);
  unsigned short perms = 0;
  // set perms from string
  char next_perm = argv[3][0];
  int num_perms = 0;
  while(next_perm && num_perms < 3) {
    next_perm = argv[3][num_perms];
    switch(next_perm){
      case 'p':
        perms |= PTE_P;
        break;
      case 'w':
        perms |= PTE_W;
        break;
      case 'u':
        perms |= PTE_U;
        break;
      default:
        cprintf("unrecognized permission %c please use p|w|u\n", next_perm);
        return 0;
    }
    num_perms++;
  }
  if(num_perms == 3) {
    argv[3][3] = 0;
  }
  cprintf("start=0x%lx, end=0x%lx, perms=0x%x\n", start, end, perms);
  for( ; start <= end; start += PGSIZE) {
    pte_t* entry = pgdir_walk(kern_pgdir, (void*) start, 0);
    if (NULL != entry) {
      char old_perms[4] = {0, 0, 0, 0};
      int old_perm_num = 0;
      if (*entry & 0x1) {
        old_perms[old_perm_num++] = 'p';
      }
      if (*entry & 0x2) {
        old_perms[old_perm_num++] = 'w';
      }
      if (*entry & 0x4) {
        old_perms[old_perm_num++] = 'u';
      }
      *entry = (*entry & ~0xFFF) | perms; 
      cprintf("changing permissions for address %lx from %s to %s\n", start, old_perms, argv[3]);
    }
  }
  return 0;
}

int 
memdump(int argc, char** argv, struct Trapframe *tf) {
  if (argc != 3) {
    cprintf("Must have exactly 2 arguments\n");
    return 0;
  }
  uintptr_t start = (uintptr_t) strtol(argv[1], NULL, 0);
  uintptr_t end = (uintptr_t) strtol(argv[2], NULL, 0);
  cprintf("Showing virtual addresses 0x%lx to 0x%lx\n", start, end);
  cprintf("addr --> value\n");
  for( ; start <= end; start += 4) {
    cprintf("0x%lx --> 0x%lx\n", start, *((unsigned long*) start));
  }
  
  return 0;
}

int 
cont (int argc, char** argv, struct Trapframe *tf) {
  cprintf("continuing...\n");
  return -1;
}

int 
step (int argc, char** argv, struct Trapframe *tf) {
  tf->tf_eflags |= 0x100; // Turn on the TF flag
  return -1;
}


int
mon_help(int argc, char **argv, struct Trapframe *tf)
{
  int i;

  for (i = 0; i < NCOMMANDS; i++)
    cprintf("%s - %s\n", commands[i].name, commands[i].desc);
  return 0;
}

int
mon_infokern(int argc, char **argv, struct Trapframe *tf)
{
  extern char _start[], entry[], etext[], edata[], end[];

  cprintf("Special kernel symbols:\n");
  cprintf("  _start                  %08x (phys)\n", _start);
  cprintf("  entry  %08x (virt)  %08x (phys)\n", entry, entry - KERNBASE);
  cprintf("  etext  %08x (virt)  %08x (phys)\n", etext, etext - KERNBASE);
  cprintf("  edata  %08x (virt)  %08x (phys)\n", edata, edata - KERNBASE);
  cprintf("  end    %08x (virt)  %08x (phys)\n", end, end - KERNBASE);
  cprintf("Kernel executable memory footprint: %dKB\n",
          ROUNDUP(end - entry, 1024) / 1024);
  return 0;
}


int
mon_backtrace(int argc, char **argv, struct Trapframe *tf)
{
  uint32_t* ebp = (uint32_t*) read_ebp();
  while(ebp) {
    uint32_t eip = *(ebp + 1);
    struct Eipdebuginfo debug_info;

    int error = debuginfo_eip((uintptr_t) eip, &debug_info);
    if (error) {
      cprintf("could not find debug info");
    }

    uint32_t a1, a2, a3, a4;
    a1 = *(ebp + 2);
    a2 = *(ebp + 3);
    a3 = *(ebp + 4);
    a4 = *(ebp + 5);

    int line_diff = eip - debug_info.eip_fn_addr;
    cprintf("%<ebp%> %x %<eip%> %x args %08x %08x %08x %08x\n", 0x83, (uint32_t) ebp, 0x05, eip, a1, a2, a3, a4);
    cprintf("         ");
    cprintf("%s:%d: ", debug_info.eip_file, debug_info.eip_line);
    cprintf("%.*s+%d\n", 
      debug_info.eip_fn_namelen, debug_info.eip_fn_name, 
      eip - debug_info.eip_fn_addr);
    ebp = (uint32_t*) *ebp;
  }
  return 0;
}



/***** Kernel monitor command interpreter *****/

#define WHITESPACE "\t\r\n "
#define MAXARGS 16

static int
runcmd(char *buf, struct Trapframe *tf)
{
  int argc;
  char *argv[MAXARGS];
  int i;

  // Parse the command buffer into whitespace-separated arguments
  argc = 0;
  argv[argc] = 0;
  while (1) {
    // gobble whitespace
    while (*buf && strchr(WHITESPACE, *buf))
      *buf++ = 0;
    if (*buf == 0)
      break;

    // save and scan past next arg
    if (argc == MAXARGS-1) {
      cprintf("Too many arguments (max %d)\n", MAXARGS);
      return 0;
    }
    argv[argc++] = buf;
    while (*buf && !strchr(WHITESPACE, *buf))
      buf++;
  }
  argv[argc] = 0;

  // Lookup and invoke the command
  if (argc == 0)
    return 0;
  for (i = 0; i < NCOMMANDS; i++)
    if (strcmp(argv[0], commands[i].name) == 0)
      return commands[i].func(argc, argv, tf);
  cprintf("Unknown command '%s'\n", argv[0]);
  return 0;
}

void
monitor(struct Trapframe *tf)
{
  char *buf;

  cprintf("Welcome to the JOS kernel monitor!\n");
  cprintf("Type 'help' for a list of commands.\n");

  if (tf != NULL) {
    print_trapframe(tf);

    if (tf->tf_trapno == T_DEBUG || tf->tf_trapno == T_BRKPT) {
      cprintf("\n-----------------------\n");
      cprintf("EIP     : 0x%x\n", tf->tf_eip);
      cprintf("MEM[EIP]: 0x%x ", *((uint32_t*)(tf->tf_eip)));
      cprintf("\n-----------------------\n");
    }
  }

  while (1) {
    buf = readline("K> ");
    if (buf != NULL)
      if (runcmd(buf, tf) < 0)
        break;
  }
}
