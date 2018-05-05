package compiler.ilinterpreter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import compiler.ilgenerator.ILInstruction;
import compiler.ilgenerator.ILRegister;
import compiler.ilgenerator.InstructionType;
import compiler.ilgenerator.ILGeneratorImpl;
import compiler.shared.ILInterpreter;
import compiler.analyzer.Type;

public class ILInterpreterImpl implements ILInterpreter {
    private int PC;
    // private boolean done;
    private Map<String,Integer> labelLocations;
    private State state;
    private List<String> args;
    private int localPutCount = 0;
    private int localPopCount = 0;

    public void execute(List<ILInstruction> instructions) {
        // System.out.println(instructions);
        PC = -1;
        // done = false;
        state = new State();
        args = new ArrayList<>();
        preprocess(instructions);
        // PC = labelLocations.get(ILGeneratorImpl.ENTRY_POINT);
        while (++PC < instructions.size()) {
            execute(instructions.get(PC));
        }
    }

    private void preprocess(List<ILInstruction> instructions) {
        labelLocations = new HashMap<>();
        int pcCounter = 0;
        for (ILInstruction instruction : instructions) {
            if (instruction.getType() == InstructionType.LABEL) {
                labelLocations.put(instruction.getLabelName(), pcCounter);
            }
            pcCounter++;
        }
    }

    private String funcnameToLabelName(String funcName) {
        return "func:" + funcName + ":start";
    }

    private void execute(ILInstruction instruction) {
        float val1,val2;
        String argName;
        // if (instruction == null) {
        //     done = true;
        //     return;
        // }
        switch (instruction.getType()) {
            case ADD:
                if (instruction.getR1().getType().getType() == Type.FLOAT) {
                    float result = state.getFloat(instruction.getR2().getName()) + state.getFloat(instruction.getR3().getName());
                    state.setFloat(false, instruction.getR1().getName(), result);
                } else {
                    int result = state.getInt(instruction.getR2().getName()) + state.getInt(instruction.getR3().getName());
                    state.setInt(false, instruction.getR1().getName(), result);
                }
                break;
            case SUB:
                if (instruction.getR1().getType().getType() == Type.FLOAT) {
                    float result = state.getFloat(instruction.getR2().getName()) - state.getFloat(instruction.getR3().getName());
                    state.setFloat(false, instruction.getR1().getName(), result);
                } else {
                    int result = state.getInt(instruction.getR2().getName()) - state.getInt(instruction.getR3().getName());
                    state.setInt(false, instruction.getR1().getName(), result);
                }
                break;
            case MULT:
                if (instruction.getR1().getType().getType() == Type.FLOAT) {
                    float result = state.getFloat(instruction.getR2().getName()) * state.getFloat(instruction.getR3().getName());
                    state.setFloat(false, instruction.getR1().getName(), result);
                } else {
                    int result = state.getInt(instruction.getR2().getName()) * state.getInt(instruction.getR3().getName());
                    state.setInt(false, instruction.getR1().getName(), result);
                }
                break;
            case DIV:
                if (instruction.getR1().getType().getType() == Type.FLOAT) {
                    float result = state.getFloat(instruction.getR2().getName()) / state.getFloat(instruction.getR3().getName());
                    state.setFloat(false, instruction.getR1().getName(), result);
                } else {
                    int result = state.getInt(instruction.getR2().getName()) / state.getInt(instruction.getR3().getName());
                    state.setInt(false, instruction.getR1().getName(), result);
                }
                break;
            case AND:
                // System.out.println("AND");
                break;
            case OR:
                // System.out.println("OR");
                break;
            case MOV:
                if (instruction.getR1().getType().getType() == Type.FLOAT) {
                    float result = state.getFloat(instruction.getR2().getName());
                    state.setFloat(false, instruction.getR1().getName(), result);
                } else {
                    int result = state.getInt(instruction.getR2().getName());
                    state.setInt(false, instruction.getR1().getName(), result);
                }
                break;
            case BREQ:
                val1 = state.getFloat(instruction.getR1().getName());
                val2 = state.getFloat(instruction.getR2().getName());
                if (val1 == val2) {
                    PC = labelLocations.get(instruction.getLabelName());
                }
                break;
            case BRNEQ:
                val1 = state.getFloat(instruction.getR1().getName());
                val2 = state.getFloat(instruction.getR2().getName());
                if (val1 != val2) {
                    PC = labelLocations.get(instruction.getLabelName());
                }
                break;
            case BRLT:
                val1 = state.getFloat(instruction.getR1().getName());
                val2 = state.getFloat(instruction.getR2().getName());
                if (val1 < val2) {
                    PC = labelLocations.get(instruction.getLabelName());
                }
                break;
            case BRGT:
                val1 = state.getFloat(instruction.getR1().getName());
                val2 = state.getFloat(instruction.getR2().getName());
                if (val1 > val2) {
                    PC = labelLocations.get(instruction.getLabelName());
                }
                break;
            case BRGEQ:
                val1 = state.getFloat(instruction.getR1().getName());
                val2 = state.getFloat(instruction.getR2().getName());
                if (val1 >= val2) {
                    PC = labelLocations.get(instruction.getLabelName());
                }
                break;
            case BRLEQ:
                val1 = state.getFloat(instruction.getR1().getName());
                val2 = state.getFloat(instruction.getR2().getName());
                if (val1 <= val2) {
                    PC = labelLocations.get(instruction.getLabelName());
                }
                break;
            case LABEL:
                // System.out.println("LABEL");
                break;
            case GOTO:
                PC = labelLocations.get(instruction.getLabelName());
                break;
            case CALL:
                if (funcnameToLabelName("printi").equals(instruction.getLabelName())) {
                    state.libPrinti(args.remove(0));
                } else if (funcnameToLabelName("printf").equals(instruction.getLabelName())) {
                    state.libPrintf(args.remove(0));
                } else if (funcnameToLabelName("readi").equals(instruction.getLabelName())) {
                    state.setInt(false, "return_value", state.libReadi());
                } else if (funcnameToLabelName("readf").equals(instruction.getLabelName())) {
                    state.setFloat(false, "return_value", state.libReadf());
                } else {
                    state.setInt(true, "return_address", PC);
                    state.push();
                    // System.out.println("function:" + instruction.getLabelName());
                    localPutCount = 0;
                    PC = labelLocations.get(instruction.getLabelName());
                }
                break;
            case RET:
                state.pop();
                localPopCount = 0;
                PC = state.getInt("return_address");
                break;
            case SAVE_RET_VAL:

                if (instruction.getR1().getType().getType() == Type.FLOAT) {
                    // System.out.println("Saving return value: " + state.getFloat(instruction.getR1().getName()));
                    state.setFloat(false, "return_value", state.getFloat(instruction.getR1().getName()));
                } else {
                    // System.out.println("Saving return value: " + state.getInt(instruction.getR1().getName()));
                    state.setInt(false, "return_value", state.getInt(instruction.getR1().getName()));
                }
                break;
            case POP_RET_VAL:
                if (instruction.getR1().getType().getType() == Type.FLOAT) {
                    // System.out.println("Return value: " +  state.getFloat("return_value"));
                    state.setFloat(false, instruction.getR1().getName(), state.getFloat("return_value"));
                } else {
                    // System.out.println("Return value: " +  state.getInt("return_value"));
                    state.setInt(false, instruction.getR1().getName(), state.getInt("return_value"));
                }
                break;
            case PUT_ARG:
                args.add(instruction.getR1().getName());
                break;
            case POP_ARG:
                ILRegister r1 = instruction.getR1();
                String argReg = args.remove(0);
                if (r1.getType().getType() == Type.FLOAT) {
                    // System.out.println("Argument: " + argReg + ", value: " + state.getFloat(argReg));
                    state.setFloat(false, r1.getName(), state.getFloat(argReg));
                } else {
                    // System.out.println("Argument: " + argReg + ", value: " + state.getInt(argReg));
                    state.setInt(false, r1.getName(), state.getInt(argReg));
                }
                break;
            case PUT_LOCAL:
                argName = "arg" + localPutCount;
                localPutCount++;
                if (instruction.getR1().getType().getType() == Type.FLOAT) {
                    // System.out.println("Saving " + argName + ": " + state.getFloat(instruction.getR1().getName()));
                    state.setFloat(true, argName, state.getFloat(instruction.getR1().getName()));
                } else {
                    // System.out.println("Saving " + argName + ": " + state.getInt(instruction.getR1().getName()));
                    state.setInt(true, argName, state.getInt(instruction.getR1().getName()));
                }
                break;
            case POP_LOCAL:
                argName = "arg" + localPopCount;
                localPopCount++;
                if (instruction.getR1().getType().getType() == Type.FLOAT) {
                    // System.out.println("Restoring " + argName + ": " + state.getFloat(argName));
                    state.setFloat(false, instruction.getR1().getName(), state.getFloat(argName));
                } else {
                    // System.out.println("Restoring " + argName + ": " + state.getInt(argName));
                    state.setInt(false, instruction.getR1().getName(), state.getInt(argName));
                }
                break;
            case LDR:
                if (instruction.getR1().getType().getType() == Type.FLOAT) {
                    float result = state.loadFloat(state.getInt(instruction.getR2().getName()), state.getInt(instruction.getR3().getName()));
                    state.setFloat(false,instruction.getR1().getName(),result);
                } else {
                    int result = state.loadInt(state.getInt(instruction.getR2().getName()), state.getInt(instruction.getR3().getName()));
                    state.setInt(false,instruction.getR1().getName(),result);
                }
                break;
            case STR:
                if (instruction.getR1().getType().getType() == Type.FLOAT) {
                    float value = state.getFloat(instruction.getR1().getName());
                    state.storeFloat(state.getInt(instruction.getR2().getName()), state.getInt(instruction.getR3().getName()), value);
                } else {
                    int value = state.getInt(instruction.getR1().getName());
                    state.storeInt(state.getInt(instruction.getR2().getName()), state.getInt(instruction.getR3().getName()), value);
                }
                break;
            case LDI:
                state.setInt(false, instruction.getR1().getName(), instruction.getImmInt());
                break;
            case LDF:
                state.setFloat(false, instruction.getR1().getName(), instruction.getImmFloat());
                break;
            default:

                break;
        }
    }
}