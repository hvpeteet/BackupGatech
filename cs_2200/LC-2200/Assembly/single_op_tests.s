! load s0 with 0x8 if 0x10 is in s0 then this worked correctly
add $s0, $s0, $s0
halt

! load s0 with 0x3 and s2 with 0xFFFD and you should get s0==0x2 
nand $s0, $s0, $s1
halt

! load s0 with 0x5 and s0 should become 0xB
addi $s0, $s0, 6
halt

! s0 should be 0xABCD
lw $s0, 2($zero)
halt
.word 0xABCD

! load s0 with 0xA and the address 0x5 should have 0xA in it
sw $s0, 5($zero)
halt

! load s0 and s1 with the same thing and this should immediatly halt (s0 should still equal s1) otherwise s0 should be doubled.
beq $s0, $s1, 1
add $s0, $s0, $s0
halt

! this should set s0 to 7
jalr $s0, $s0
addi $s0, $s0, 6
halt

! this should set s0 to 0xFFFF and s1 to 2
addi $s0, $zero, 3
jalr $s0, $s1
halt
lw $s0, 5($zero)
halt
.word 0xFFFF