        addi $s1, $zero, 0
        addi $s2, $zero, 0
        addi $a0, $zero, 0

        la $s0, FUN
        jalr $s0, $ra

        addi $s1, $s1, 1
        addi $s1, $s1, 1
        addi $s1, $s1, 1
        addi $s1, $s1, 1
HALT:   beq $zero, $zero, HALT
        addi $s2, $s2, 1
        addi $s2, $s2, 1
        addi $s2, $s2, 1


FUN:    addi $a0, $a0, 1
        addi $a0, $a0, 1
        addi $a0, $a0, 1
        jalr $ra, $zero
        addi $a0, $a0, 1
        addi $a0, $a0, 1
        addi $a0, $a0, 1

; Expect a0 == 3 --> s1 == 4 NO CHANGE TO s2