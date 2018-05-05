package compiler.parser;

import compiler.shared.Parser;
import compiler.shared.Scanner;
import compiler.shared.Symbol;
import compiler.shared.SymbolType;
import compiler.shared.Token;
import compiler.parser.ParseException;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class LrParser implements Parser {

    Stack<Set<ParseItem>> stateStack;
    Stack<Symbol> symbolStack;
    Scanner scanner;
    Symbol nextSymbol;
    private boolean debug;
    private Writer debugWriter;
    private int numTokensConsumed = 0;

    public LrParser() {
        this.stateStack = new Stack<>();
        this.symbolStack = new Stack<>();
    }

    /**
     * Parses an input from a scanner.
     * @param scanner The scanner to take input from.
     * @return The root symbol of the parse tree.
    */
    public Symbol parse(Scanner scanner) throws ParseException {
        Set<ParseItem> startState = new HashSet<>();
        startState.add(new ParseItem(SymbolType.START, SymbolType.START.derivations[0], 0, SymbolType.EOF));
        this.stateStack.push(closure(startState));

        this.scanner = scanner;
        this.nextSymbol = new Symbol(scanner.peek());
        while (!this.isAccepting()) {
            debugLog("\n--------------ACTION--------------\n");
            takeAction();
        }
        Symbol ast = symbolStack.pop();
        ast.fix();
        return ast;
    }

    /**
     * Parses an input from a scanner.
     * @param scanner The scanner to take input from.
     * @param debugFileName the file to print debug info to.
     * @return The root symbol of the parse tree.
    */
    public Symbol parse(Scanner scanner, String debugFileName) throws ParseException {
        try {
            this.debugWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(debugFileName), "utf-8"));
            this.debug = true;
        } catch (Exception e) {
            System.err.println("Error creating debug file: " + e.toString() + "\nContinuing without debug file");
        }
        return this.parse(scanner);
    }

    private void debugLog(String toLog) {
        if (debug) {
            try {
                debugWriter.write(toLog);
            } catch (Exception e) {
                System.err.println("Error writing to debug file: " + e.toString());
            }
        }
    }

    /**
     * Test if the stack is in an accepting state.
     * @return if the parser accepts the entire input (if it is done).
    */
    public boolean isAccepting() {
        ParseItem finalItem = new ParseItem(SymbolType.START, SymbolType.START.derivations[0], SymbolType.START.derivations[0].length, SymbolType.EOF);
        return stateStack.peek().contains(finalItem);
    }

    /**
     * Checks if a state has a complete item (time for a reduction).
     * @param state the state to check if it has a complete parse item. Note this uses the next symbol from the scanner.
    */
    public boolean hasCompleteItem(Set<ParseItem> state) {
        for (ParseItem item : state) {
            if (item.isComplete(this.nextSymbol)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the next state based on the current state and the next symbol.
     * @param currentState the current state on top of the stack.
     * @param nextSymbol the next symbol that is going to be read in.
     * @return the next state that would be reached from reading in nextSymbol
    */
    public Set<ParseItem> goTo(Set<ParseItem> currentState, Symbol nextSymbol) {
        Set<ParseItem> newItems = new HashSet<>();
        for (ParseItem item : currentState) {
            ParseItem successor = item.getSuccessor(nextSymbol);
            if (successor != null) {
                newItems.add(successor);
            }
        }
        return newItems;
    }

    /**
     * Returns the closure of a state.
     * @param state the state to take the closure of.
     * @return the closure of the state.
    */
    public Set<ParseItem> closure(Set<ParseItem> state) {
        // Copy the state
        Set<ParseItem> newState = new HashSet<>();
        newState.addAll(state);

        boolean changed = true;
        while (changed) {
            changed = false;
            Set<ParseItem> toAdd = new HashSet<>();
            for (ParseItem item : newState) {
                if (!item.needsNext().isTerm()) {
                    for (SymbolType[] production : item.needsNext().derivations) {
                        Set<SymbolType> extendedFirsts = item.closureExtendedFirsts();
                        for (SymbolType first : extendedFirsts) {
                            ParseItem newItem = new ParseItem(item.needsNext(), production, 0, first);
                            toAdd.add(newItem);
                        }
                    }
                }
            }
            int oldSize = newState.size();
            newState.addAll(toAdd);
            if (newState.size() > oldSize) {
                changed = true;
            }
        }
        return newState;
    }

    /**
     * takeAction applies the next action to the parser.
     * this plays the part of the action table.
    */
    public void takeAction() throws ParseException {
        // Decide which action to take: reduce or consumeToken and shift
        if (hasCompleteItem(stateStack.peek())) {
            reduce(nextSymbol);
        } else {
            Symbol toShift = nextSymbol;
            scanner.pop();
            nextSymbol = new Symbol(scanner.peek());
            shift(toShift);
        }
    }

    /**
     * Reduce the top of the stack.
     * @param next given a parse item [A --> B C *, EOF] this would be the EOF that completed the parse item.
    */
    public void reduce(Symbol next) throws ParseException {
        Set<ParseItem> stateToReduce = this.stateStack.peek();
        ParseItem itemToReduce = null;
        for (ParseItem item : stateToReduce) {
            if (item.isComplete(this.nextSymbol)) {
                itemToReduce = item;
            }
        }
        if (itemToReduce == null) {
            throw new RuntimeException("reduce called on state that cannot be reduced: " + stateToReduce);
        }
        debugLog("\nReducing rule" + itemToReduce + "\n\n");
        Symbol newSymbol = new Symbol("", null, itemToReduce.getLeft());
        ArrayList<Symbol> parsedSymbols = new ArrayList<>();
        for (SymbolType symbol : itemToReduce.getRight()) {
            if (symbol != SymbolType.EPSILON) {
                stateStack.pop();
                parsedSymbols.add(symbolStack.pop());
            }
        }
        Collections.reverse(parsedSymbols);
        for (Symbol symbol : parsedSymbols) {
            newSymbol.addChild(symbol);
        }
        shift(newSymbol);
    }

    /**
     * Shift on the top of the stack.
     * @param next the symbol to consume.
    */
    public void shift(Symbol next) throws ParseException {
        debugLog("\nShifting \"" + next + "\" onto the symbol stack: current state : " + stateStack.peek() + "\n\n");
        Set<ParseItem> nextState = goTo(stateStack.peek(), next);
        if (nextState.isEmpty()) {
            try {
                this.debugWriter.close();
            } catch (Exception e) {
                // do nothing
            }
            debugLog("Bad parse or illegal string. Got unexpected symbol: " + next + "\nExisting symbols:\n" + symbolStack + "\nExisting states:\n" + stateStack + "\ncurrentState:\n" + stateStack.pop());
            throw new ParseException("Parser failure: unexpected token: \"" + next + "\" in token stream at index " + numTokensConsumed);
        }
        symbolStack.push(next);
        if (next.getType().isTerm()) {
            numTokensConsumed++;
        }
        stateStack.push(closure(nextState));
    }
}