package compiler.shared;

import compiler.ilgenerator.ILInstruction;
import java.util.List;

public interface ILGenerator {
	/**
	 * Generates Intermediate Language code from a given AST
	 * @param AST output from the parser
	 * @return ILInstructions sequential list of instructions in the intermediate language
	 */
	public List<ILInstruction> generate();
}