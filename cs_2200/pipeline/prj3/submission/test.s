            add $a0, $zero, $zero
            addi $a1, $zero, 10

REG_LOOP:   beq $a0, $a1, MEM_LOOP
            addi $a0, $a0, 1
            beq $zero, $zero, REG_LOOP

MEM_LOOP:   lw $a0, 0($zero)
            beq $a0, $a1, DONE
            addi $a0, $a0, 1
            sw $a0, 0($zero)
            beq $zero, $zero, MEM_LOOP

DONE:       beq $zero, $zero, DONE
