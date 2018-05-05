package compiler.parser;

import compiler.shared.Scanner;
import compiler.shared.Token;

import java.util.List;

public class MockScanner implements Scanner {

    List<Token> list;

    /**
     * Creates a mock scanner.
    */
    public MockScanner(List<Token> llist) {
        this.list = llist;
    }

    public Token peek() {
        return list.get(0);
    }

    public Token pop() {
        return list.remove(0);
    }
}