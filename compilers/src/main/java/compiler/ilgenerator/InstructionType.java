package compiler.ilgenerator;

public enum InstructionType {
    ADD,
    SUB,
    MULT,
    DIV,
    AND,
    OR,

    MOV,

    BREQ,
    BRNEQ,
    BRLT,
    BRGT,
    BRGEQ,
    BRLEQ,

    LABEL,
    GOTO,

    CALL,
    RET,
    SAVE_RET_VAL,
    POP_RET_VAL,
    PUT_ARG,
    POP_ARG,
    PUT_LOCAL,
    POP_LOCAL,

    LDR,
    STR,
    LDI,
    LDF
}