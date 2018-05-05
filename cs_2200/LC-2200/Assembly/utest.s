! Computes fib(A) (fibonacci number) and stores it in B
! NOTE this will also leave fib(A) in $v0 which is easier to check.

! Details:
!   1) This uses a stack that starts at 0xF000 and grows into high memory.
!   2) The code starts at 0x0000 so no need to set the PC (since 0 by default)

! Fibonacci numbers:
!   A : DEC:  HEX
!   0 : 1  :  0x1
!   1 : 1  :  0x1
!   2 : 2  :  0x2
!   3 : 3  :  0x3
!   4 : 5  :  0x5
!   5 : 8  :  0x8
!   6 : 13 :  0xD
!   7 : 21 :  0x15
!   8 : 34 :  0x22

        la $t0, STACK               ! Set stack location
        lw $sp, 0($t0)              !

        la $t0, A                   ! \  
        lw $a0, 0($t0)              ! | Call FIB
        la $t0, FIB                 ! |
        jalr $t0, $ra               ! /

        la $t0, B                   ! fib(A) --> B
        sw $v0, 0($t0)              !
        halt                        ! DONE

STACK:  .word 0xF000
A:      .word 0x8
B:      .word 0x0


FIB:    addi $sp, $sp, 3            ! \ 
        sw $s0, -2($sp)             ! | save registers
        sw $s1, -1($sp)             ! |
        sw $s2, 0($sp)              ! /

                                    ! designate what the saved registers are being used for
        add $s0, $a0, $zero         ! s0 <- a0
        add $s1, $zero, $zero       ! s1 <- fib(n-1) (just making space)
                                    ! s2 UNUSED for now

        beq $a0, $zero, BASE        ! if arg == 0 goto BASE

        addi $t0, $zero, 1          ! else if arg == 1 goto BASE
        beq $a0, $t0, BASE          ! 

        beq $zero, $zero, REC       ! else return fib(n-1) + fib(n-2)

BASE:   addi $v0, $zero, 1          ! return 1
        la $t0, DONE                ! 
        jalr $t0, $zero             ! goto DONE

REC:    addi $sp, $sp, 1            ! build up stack to call with n-1
        sw $ra, 0($sp)              !   * save return address

        addi $a0, $s0, -1           ! \
        la $t0, FIB                 ! | call fib(n-1)
        jalr $t0, $ra               ! /

        add $s1, $v0, $zero         ! save result in s1

        lw $ra, 0($sp)              ! restore ra
        addi $sp, $sp, -1           ! 

        addi $sp, $sp, 1            ! build up stack to call with n-2
        sw $ra, 0($sp)              !   * save return address

        addi $a0, $s0, -2           ! \
        la $t0, FIB                 ! | call fib(n-2)
        jalr $t0, $ra               ! /

        add $v0, $v0, $s1           ! v0 = return value = fib(n-1) + fib(n-2)

        lw $ra, 0($sp)              ! restore ra
        addi $sp, $sp, -1           ! 

        beq $zero, $zero, DONE      ! goto DONE

DONE:   lw $s0, -2($sp)             ! restore saved registers
        lw $s1, -1($sp)             !
        lw $s2, 0($sp)              !

        addi $sp, $sp, -3           ! tear down stack and return
        jalr $ra, $zero             ! assumes v0 is set