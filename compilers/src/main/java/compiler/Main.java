package compiler;

import compiler.parser.LrParser;
import compiler.scanner.NFAScanner;
import compiler.scanner.ScanException;
import compiler.parser.ParseException;
import compiler.shared.Parser;
import compiler.shared.Scanner;
import compiler.shared.Symbol;
import compiler.analyzer.TypeAnalyzer;
import compiler.analyzer.TypeException;
import compiler.shared.ILGenerator;
import compiler.ilgenerator.ILGeneratorImpl;
import java.util.List;
import compiler.ilgenerator.ILInstruction;
import compiler.shared.ILInterpreter;
import compiler.ilinterpreter.ILInterpreterImpl;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    /**
     * Runs everything.
     * @param args not defined yet, but I expect it will take in the file(s) to compile
     */
    public static void main(String[] args) {
        boolean printTokens = false;
        boolean printAst = false;
        boolean debug = false;
        boolean runIL = false;

        int argNum = 0;
        while (argNum < (args.length - 1)) {
            if (args[argNum].equals("--tokens")) {
                printTokens = true;
            }
            if (args[argNum].equals("--ast")) {
                printAst = true;
            }
            if (args[argNum].equals("--debug")) {
                debug = true;
            }
            if (args[argNum].equals("--runil")) {
                runIL = true;
            }
            argNum++;
        }

        String fileName = args[argNum];
        Symbol ast;
        try {
            ast = run(fileName, printTokens, debug);
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return;
        } catch (IOException e) {
            System.out.println("Error reading file");
            return;
        } catch (ScanException e) {
            System.err.println(e.getMessage());
            return;
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            return;
        }

        if (printAst) {
            System.out.println(ast);
        }

        // try {
        //     TypeAnalyzer analyzer = new TypeAnalyzer(ast, debug);
        //     analyzer.assertWellTyped();
        // } catch (TypeException e) {
        //     System.out.println(e.getMessage());
        //     return;
        // }
        ILGenerator generator;
        try {
            generator = new ILGeneratorImpl(ast);
        } catch (TypeException e) {
            System.out.println(e.getMessage());
            return;
        }
        List<ILInstruction> instructions = generator.generate();
        // System.out.println(instructions);

        if (runIL) {
            ILInterpreter interpreter = new ILInterpreterImpl();
            interpreter.execute(instructions);
        }  
    }

    /**
     * Factors out the logic so it can be reused by the tester.
     * @param fileName the file to parse
     * @param printTokens whether or not to print the tokens from the scanner.
     * @param debug if debug mode should be enabled (will print actions to debugMain.txt)
    */
    public static Symbol run(String fileName, boolean printTokens, boolean debug) throws FileNotFoundException, IOException, ScanException, ParseException {
        Scanner scanner = null;
        scanner = new NFAScanner(fileName);

        if (printTokens) {
            System.out.println(scanner);
        }

        Parser parser = new LrParser();
        if (debug) {
            return parser.parse(scanner, "debugMain.txt");
        } else {
            return parser.parse(scanner);
        }
    }

}