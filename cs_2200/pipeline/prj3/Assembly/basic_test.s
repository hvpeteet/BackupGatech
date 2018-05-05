; addi $s0, $s0, 10
; addi $s0, $s0, 5
; add $s1, $s0, $zero
; addi $s1, $zero, 15
; nand $s2, $s1, $s1

; la $s0, X
; lw $s1, 0($s0)
; lw $s2, 1($s0)
; add $s3, $s2, $s2

; X: .fill 10
; Y: .fill 255

; la $s0, A0
; lw $s1, 1($s0)
; add $s1, $s1, $s1

; sw $s1, 2($s0) ; A2 = A1 * 2
; sw $zero, 3($s0) ; A3 = 0

; A0: .fill 255
; A1: .fill 8
; A2: .fill 255
; A3: .fill 255
; A4: .fill 255

; expect ff, 8, 10, 0, ff

;         addi $a0, $zero, 1
;         la $a1, CURRENT
; LOOP:   add $a0, $a0, $a0
;         sw $a0, 0($a1)
;         beq $zero, $zero, LOOP
;         addi $s0, $zero, 16

; CURRENT: .fill 0

la $s0, FUNC
jalr $s0, $ra
HALT: beq $zero, $zero, HALT

FUNC:   addi $s1, $zero, 16
        jalr $ra, $zero