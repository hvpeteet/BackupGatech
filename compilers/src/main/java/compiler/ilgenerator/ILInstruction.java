package compiler.ilgenerator;

import java.util.List;

public class ILInstruction {
    private InstructionType type;
    private ILRegister r1;
    private ILRegister r2;
    private ILRegister r3;
    private String labelName;
    private int immInt;
    private float immFloat;

    public ILInstruction(InstructionType type, ILRegister r1, ILRegister r2, ILRegister r3, String labelName, int immInt, float immFloat) {
        this.type = type;
        this.r1 = r1;
        this.r2 = r2;
        this.r3 = r3;
        this.labelName = labelName;
        this.immInt = immInt;
        this.immFloat = immFloat;
    }

    // Constructor for label, goto, and call
    public ILInstruction(InstructionType type, String labelName) {
        this.type = type;
        this.labelName = labelName;
    }

    // For ret
    public ILInstruction(InstructionType type) {
        this.type = type;
    }

    // Constructor for control statements
    public ILInstruction(InstructionType type, ILRegister r1, ILRegister r2, String labelName) {
        this.type = type;
        this.r1 = r1;
        this.r2 = r2;
        this.labelName = labelName;
    }

    // Constructor for arithmetic, LDR, and STR
    public ILInstruction(InstructionType type, ILRegister r1, ILRegister r2, ILRegister r3) {
        this.type = type;
        this.r1 = r1;
        this.r2 = r2;
        this.r3 = r3;
    }

    // For the move instruction
    public ILInstruction(InstructionType type, ILRegister r1, ILRegister r2) {
        this.type = type;
        this.r1 = r1;
        this.r2 = r2;
    }

    // Push, Pop, ret vals and args
    public ILInstruction(InstructionType type, ILRegister r1) {
        this.type = type;
        this.r1 = r1;
    }

    // Load int
    public ILInstruction(InstructionType type, ILRegister r1, int immInt) {
        this.type = type;
        this.r1 = r1;
        if (type != InstructionType.LDI) {
            throw new RuntimeException("Tried to create load_int without type = LDI");
        }
        this.immInt = immInt;
    }

    // Load float
    public ILInstruction(InstructionType type, ILRegister r1, float immFloat) {
        this.type = type;
        this.r1 = r1;
        if (type != InstructionType.LDF) {
            throw new RuntimeException("Tried to create load_float without type = LDF");
        }
        this.immFloat = immFloat;
    }

    public InstructionType getType() {
        return type;
    }

    public ILRegister getR1() {
        return r1;
    }

    public ILRegister getR2() {
        return r2;
    }

    public ILRegister getR3() {
        return r3;
    }

    public String getLabelName() {
        return labelName;
    }

    public int getImmInt() {
        return immInt;
    }

    public float getImmFloat() {
        return immFloat;
    }

    public String toString() {
        String to_return = "";
        to_return += this.type;
        if (this.r1 != null) {
            to_return += " " + this.r1;
        }
        if (this.r2 != null) {
            to_return += " " + this.r2;
        }
        if (this.r3 != null) {
            to_return += " " + this.r3;
        }
        if (this.labelName != null) {
            to_return += " " + this.labelName;
        }
        if (this.type == InstructionType.LDI) {
            to_return += " " + this.immInt;
        }
        if (this.type == InstructionType.LDF) {
            to_return += " " + this.immFloat;
        }
        return to_return;
    }

}