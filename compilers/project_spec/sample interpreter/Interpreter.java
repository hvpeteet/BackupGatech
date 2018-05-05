import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This interpreter is a proof of concept meant to be used as an example
// The interpreter is shoddy. The IR design is shoddy as well. Do your own thing.
// But it demonstrates how you might structure your interpreter.

// YOUR INTERPRETER MUST SUPPORT RECURSIVE FUNCTIONS
// AND MULTI-DIMENSIONAL ARRAYS

public class Interpreter {

    private State state = new State();
    private Labels labels = new Labels();
    private boolean done = false;
    private String[] IR;

    public Interpreter(String[] IR) {
        this.IR = IR;
    }

    private Pattern labelPattern = Pattern.compile("(.*):");
    private String getLabel(String instruction) {
        Matcher m = labelPattern.matcher(instruction);
        return (m.find())
            ? m.group(1)
            : null;
    }

    private void processArgs(String label, String[] argVals) {
        String[] args = label.split(":")[1].trim().split(",");
        int index = 0;
        for (String arg : args) {
            String symbol = arg.split("=")[0];
            String type =arg.split("=")[1];
            if (type.equals("int")) {
                state.setInt(false, symbol, state.getInt(argVals[index++]));
            } else if (type.equals("float")) {
                state.setFloat(false, symbol, state.getFloat(argVals[index++]));
            }
        }
    }

    private void preprocess() {
        // construct a complete label map of the program
        // mapping labels to their line numbers
        for (int line = 0; line < IR.length; line++) {
            String label = getLabel(IR[line]);
            if (label != null) {
                labels.set(label, line);
            }
        }
        // Perform data intialization
        // until a new label is found
        Integer line = labels.get("data");
        if (line != null) {
            while (getLabel(IR[++line]) == null) {
                execute(IR[line]);
            }
        }
        // set PC execute starting at main label
        Integer start = labels.get("main");
        if (start == null) {
            System.out.println("Error: no main label");
            System.exit(1);
        } else {
            state.setInt(false, "PC", start);
        }
    }

    public void run() {
        preprocess();

        while (!done) {
            int PC = state.getInt("PC");
            state.setInt(false, "PC", PC + 1);
            execute(IR[PC]);
        }

        System.out.println("Finished Execution");
    }

    private void execute(String instruction) {

        try {
            String[] ir = instruction.split(",");
            for (int i = 0; i < ir.length; i++) {
                ir[i] = ir[i].trim();
            }
            switch (ir[0]) {
                case "debug":
                    System.out.println(ir[1]);
                    break;
                case "call":
                    switch (ir[1]) {
                        case "printi":
                            state.libPrinti(ir[2]);
                            break;
                        case "printf":
                            state.libPrintf(ir[2]);
                            break;
                        default:
                            state.setInt(true, "returnAddress", state.getInt("PC"));
                            state.push();
                            state.setInt(false, "PC", labels.get(ir[1]));
                            break;
                    }
                    break;
                case "call_ret":
                    switch (ir[1]) {
                        case "readi":
                            state.setInt(false, ir[2], state.libReadi());
                            break;
                        case "readf":
                            state.setFloat(false, ir[2], state.libReadf());
                            break;
                        default:
                            state.setString(true, "returnArgument", ir[2]);
                            state.setInt(true, "returnAddress", state.getInt("PC"));
                            state.push();
                            state.setInt(false, "PC", labels.get(ir[1]));
                            processArgs(IR[labels.get(ir[1])], Arrays.copyOfRange(ir, 3, ir.length));
                            break;
                    }
                    break;
                case "addi":
                    state.setInt(false, ir[1],
                            state.getInt(ir[2]) + state.getInt(ir[3]));
                    break;
                case "addf":
                    state.setFloat(false, ir[1],
                            state.getFloat(ir[2]) + state.getFloat(ir[3]));
                    break;
                case "loadi":
                    state.setInt(false, ir[2], state.getInt(ir[1]));
                    break;
                case "loadf":
                    state.setFloat(false, ir[2], state.getFloat(ir[1]));
                    break;
                case "storei":
                    state.setInt(false, ir[2], state.getInt(ir[1]));
                    break;
                case "storef":
                    state.setFloat(false, ir[2], state.getFloat(ir[1]));
                    break;
                case "decint":
                    int storei_value = (ir.length == 3)
                            ? Integer.parseInt(ir[2])
                            : 0;
                    state.setInt(false, ir[1], storei_value);
                    break;

                case "decfloat":
                    float storef_value = (ir.length == 3)
                            ? Float.parseFloat(ir[2])
                            : 0;
                    state.setFloat(false, ir[1], storef_value);
                    break;
                case "decintarr":
                    int arr_value = (ir.length == 4)
                            ? Integer.parseInt(ir[3])
                            : 0;
                    state.initIntArray(false, ir[2], Integer.parseInt(ir[1]), arr_value);
                    break;
                case "loadarri":
                    Integer val = state.loadIntArray(ir[1], state.getInt(ir[2]));
                    state.setInt(false, ir[3], val);
                    break;
                case "ret":
                    if (state.inGlobalScope()) {
                        done = true;
                    } else {
                        state.pop();
                        state.setInt(false, "PC", state.getInt("returnAddress"));
                    }
                    break;
                case "reti":
                    int retval = state.getInt(ir[1]);
                    state.pop();
                    state.setInt(false, state.getString("returnArgument"), retval);
                    state.setInt(false, "PC", state.getInt("returnAddress"));
                    break;
                default:

                    break;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
