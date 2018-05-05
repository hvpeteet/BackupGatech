package compiler.shared;

import java.util.List;
import compiler.ilgenerator.ILInstruction;

public interface ILInterpreter {
    public void execute(List<ILInstruction> instructions);
}