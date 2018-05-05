/* See COPYRIGHT for copyright information. */

#include <inc/mmu.h>
#include <inc/memlayout.h>
#include <inc/trap.h>

#include <kern/picirq.h>


###################################################################
# exceptions/interrupts
###################################################################

/* TRAPHANDLER defines a globally-visible function for handling a trap.
 * It pushes a trap number onto the stack, then jumps to _alltraps.
 * Use TRAPHANDLER for traps where the CPU automatically pushes an error code.
 *
 * You shouldn't call a TRAPHANDLER function from C, but you may
 * need to _declare_ one in C (for instance, to get a function pointer
 * during IDT setup).  You can declare the function with
 *   void NAME();
 * where NAME is the argument passed to TRAPHANDLER.
 */
#define TRAPHANDLER(name, num)                                          \
        .globl name;            /* define global symbol for 'name' */   \
        .type name, @function;  /* symbol type is function */           \
        .align 2;               /* align function definition */         \
        name:                   /* function starts here */              \
        pushl $(num);                                                   \
        jmp _alltraps

/* Use TRAPHANDLER_NOEC for traps where the CPU doesn't push an error code.
 * It pushes a 0 in place of the error code, so the trap frame has the same
 * format in either case.
 */
#define TRAPHANDLER_NOEC(name, num)                                     \
        .globl name;                                                    \
        .type name, @function;                                          \
        .align 2;                                                       \
        name:                                                           \
        pushl $0;                                                       \
        pushl $(num);                                                   \
        jmp _alltraps

.text

/*
 * Lab 3: Your code here for generating entry points for the different traps.
 */

TRAPHANDLER_NOEC(int0, 0)
TRAPHANDLER_NOEC(int1, 1)
TRAPHANDLER_NOEC(int2, 2)
TRAPHANDLER_NOEC(int3, 3)
TRAPHANDLER_NOEC(int4, 4)
TRAPHANDLER_NOEC(int5, 5)
TRAPHANDLER_NOEC(int6, 6)
TRAPHANDLER_NOEC(int7, 7)
TRAPHANDLER(int8, 8)
TRAPHANDLER_NOEC(int9, 9)
TRAPHANDLER(int10, 10)
TRAPHANDLER(int11, 11)
TRAPHANDLER(int12, 12)
TRAPHANDLER(int13, 13)
TRAPHANDLER(int14, 14)
TRAPHANDLER_NOEC(int16, 16)
.data
    .space 60
.text
TRAPHANDLER_NOEC(int32, 32)
TRAPHANDLER_NOEC(int33, 33)
TRAPHANDLER_NOEC(int34, 34)
TRAPHANDLER_NOEC(int35, 35)
TRAPHANDLER_NOEC(int36, 36)
TRAPHANDLER_NOEC(int37, 37)
TRAPHANDLER_NOEC(int38, 38)
TRAPHANDLER_NOEC(int39, 39)
TRAPHANDLER_NOEC(int40, 40)
TRAPHANDLER_NOEC(int41, 41)
TRAPHANDLER_NOEC(int42, 42)
TRAPHANDLER_NOEC(int43, 43)
TRAPHANDLER_NOEC(int44, 44)
TRAPHANDLER_NOEC(int45, 45)
TRAPHANDLER_NOEC(int46, 46)
TRAPHANDLER_NOEC(int47, 47)
TRAPHANDLER_NOEC(int48, 48)
.data
    .space ((T_SYSCALL - 48 - 1) * 4)
.text
TRAPHANDLER_NOEC(intSyscall, T_SYSCALL)

/*
 * Lab 3: Your code here for _alltraps
 */

_alltraps:
    pushl %ds
    pushl %es
    pushal
    pushl $GD_KD
    popl %ds
    pushl $GD_KD
    popl %es
    pushl %esp
    call trap
