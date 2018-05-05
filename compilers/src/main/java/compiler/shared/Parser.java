package compiler.shared;

import compiler.parser.ParseException;

public interface Parser {
    /**
     * Parses an input from a scanner.
     * @param scanner The scanner to take input from.
     * @return The root symbol of the parse tree.
    */
    public Symbol parse(Scanner scanner) throws ParseException;

    public Symbol parse(Scanner scanner, String debugFile) throws ParseException;
}