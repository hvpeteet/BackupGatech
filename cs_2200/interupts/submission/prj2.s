! NAME: Henry Peteet
! GTLogin: hpeteet3
!=================================================================
! General conventions:
!   1) Stack grows from high addresses to low addresses, and the
!      top of the stack points to valid data
!
!   2) Register usage is as implied by assembler names and manual
!
!   3) Function Calling Convention:
!
!       Setup)
!       * Immediately upon entering a function, push the RA on the stack.
!       * Next, push all the registers used by the function on the stack.
!
!       Teardown)
!       * Load the return value in $v0.
!       * Pop any saved registers from the stack back into the registers.
!       * Pop the RA back into $ra.
!       * Return by executing jalr $ra, $zero.
!=================================================================

!vector table
vector0:    .fill 0x00000000 !0
            .fill 0x00000000 !1
            .fill 0x00000000 !2
            .fill 0x00000000
            .fill 0x00000000 !4
            .fill 0x00000000
            .fill 0x00000000
            .fill 0x00000000
            .fill 0x00000000 !8
            .fill 0x00000000
            .fill 0x00000000
            .fill 0x00000000
            .fill 0x00000000
            .fill 0x00000000
            .fill 0x00000000
            .fill 0x00000000 !15
!end vector table

main:           la $sp, stack           ! Initialize stack pointer
                lw $sp, 0($sp)          
                
                ! Install timer interrupt handler into vector table
                la $a0, ti_inthandler
                sw $a0, 1($zero)
                ei

                la $at, factorial       ! load address of factorial label into $at
                addi $a0, $zero, 5      ! $a0 = 5, the number to factorialize
                jalr $at, $ra           ! jump to factorial, set $ra to return addr
                halt                    ! when we return, just halt

factorial:      addi    $sp, $sp, -1    ! push RA
                sw      $ra, 0($sp)
                addi    $sp, $sp, -1    ! push a0
                sw      $a0, 0($sp)
                addi    $sp, $sp, -1    ! push s0
                sw      $s0, 0($sp)
                addi    $sp, $sp, -1    ! push s1
                sw      $s1, 0($sp)

                beq     $a0, $zero, base_zero
                addi    $s1, $zero, 1
                beq     $a0, $s1, base_one
                beq     $zero, $zero, recurse
                
    base_zero:  addi    $v0, $zero, 1   ! 0! = 1
                beq     $zero, $zero, done

    base_one:   addi    $v0, $zero, 1   ! 1! = 1
                beq     $zero, $zero, done

    recurse:    add     $s1, $a0, $zero     ! save n in s1
                addi    $a0, $a0, -1        ! n! = n * (n-1)!
                la      $at, factorial
                jalr    $at, $ra

                add     $s0, $v0, $zero     ! use s0 to store (n-1)!
                add     $v0, $zero, $zero   ! use v0 as sum register
        mul:    beq     $s1, $zero, done    ! use s1 as counter (from n to 0)
                add     $v0, $v0, $s0
                addi    $s1, $s1, -1
                beq     $zero, $zero, mul

    done:       lw      $s1, 0($sp)     ! pop s1
                addi    $sp, $sp, 1
                lw      $s0, 0($sp)     ! pop s0
                addi    $sp, $sp, 1
                lw      $a0, 0($sp)     ! pop a0
                addi    $sp, $sp, 1
                lw      $ra, 0($sp)     ! pop RA
                addi    $sp, $sp, 1
                jalr    $ra, $zero

ti_inthandler:
            ! save k0 on stack
            addi $sp, $sp, -1
            sw $k0, 0($sp)

            ! enable interupts
            ei

            ! save registers
            addi $sp, $sp, -3
            sw $a0, 2($sp)
            sw $a1, 1($sp)
            sw $a2, 0($sp)

            addi $sp, $sp, -3
            sw $s0, 2($sp)
            sw $s1, 1($sp)
            sw $s2, 0($sp)

                                        ! inrement seconds
addSeconds: la $a0, seconds
            lw $a1, 0($a0)                  ! *seconds -> a1
            lw $a2, 0($a1)                  ! seconds -> a2
            addi $s0, $a2, -59
            beq $s0, $zero, addMinutes      ! if 59 seconds go to minutes

            addi $a2, $a2, 1
            sw $a2, 0($a1)
            beq $zero, $zero, intDone

addMinutes: sw $zero, 0($a1)
            la $a0, minutes
            lw $a1, 0($a0)
            lw $a2, 0($a1)
            addi $s0, $a2, -59
            beq $s0, $zero, addHours

            addi $a2, $a2, 1
            sw $a2, 0($a1)
            beq $zero, $zero, intDone

addHours:   sw $zero, 0($a1)
            la $a0, hours
            lw $a1, 0($a0)
            lw $a2, 0($a1)
            addi $a2, $a2, 1
            sw $a2, 0($a1)
            beq $zero, $zero, intDone

            ! restore registers
intDone:    lw $s0, 2($sp)
            lw $s1, 1($sp)
            lw $s2, 0($sp)
            addi $sp, $sp, 3

            lw $a0, 2($sp)
            lw $a1, 1($sp)
            lw $a2, 0($sp)
            addi $sp, $sp, 3
            di
            lw $k0, 0($sp)
            addi $sp, $sp, 1

            ! return from interupt
            reti











stack:      .fill 0xA00000
seconds:    .fill 0xFFFFFC
minutes:    .fill 0xFFFFFD
hours:      .fill 0xFFFFFE
