package compiler.scanner;

import compiler.shared.Scanner;
import compiler.shared.Token;
import compiler.shared.SymbolType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

import java.util.List;
import java.util.LinkedList;


public class NFAScanner implements Scanner {

    private List<Token> tokens;

    public NFAScanner(String fileName) throws IOException, ScanException {
        HashSet<SymbolType> tokenTypes = new HashSet<>();
        HashMap<SymbolType,NFA> tokenRegExMap = new HashMap<>();
        for (SymbolType symbolType : SymbolType.values()) {
            if (symbolType.isTerm()) {
                tokenTypes.add(symbolType);
                tokenRegExMap.put(symbolType, new NFA(symbolType.regex()));
            }
        }
        // StringBuilder tokenBuilder = StringBuilder();

        BufferedReader file = new BufferedReader(new FileReader(fileName));

        int character = file.read();

        HashSet<Integer> whitespace = new HashSet<>();
        whitespace.add(9);
        whitespace.add(10);
        whitespace.add(13);
        whitespace.add(32);

        tokens = new LinkedList<Token>();

        int lineNumber = 1;
        int columnNumber = 1;
        int lastMatchedLineNum = 1;
        int lastMatchedColumnNum = 1;

        while (character != -1) {
            while (whitespace.contains(character)) {
                if ((char)character == '\n') {
                    lineNumber++;
                    columnNumber = 1;
                }
                character = file.read();
                columnNumber++;
            }
            StringBuilder chars = new StringBuilder();
            for (SymbolType tokenType : tokenTypes) {
                NFA nfa = tokenRegExMap.get(tokenType);
                nfa.clearState();
            }

            Set<SymbolType> matchingTokenTypes = null;
            Set<SymbolType> livingTokenTypes = null;
            boolean stillMatching = true;

            while (stillMatching) {
                chars.append((char)character);
                matchingTokenTypes = new HashSet<>();
                livingTokenTypes = new HashSet<>();
                for (SymbolType tokenType : tokenTypes) {
                    NFA nfa = tokenRegExMap.get(tokenType);
                    nfa.inputChar((char)character);
                    if (nfa.isAccepting()) {
                        matchingTokenTypes.add(tokenType);
                    }
                    if (nfa.couldAccept()) {
                        livingTokenTypes.add(tokenType);
                    }
                }
                if (matchingTokenTypes.isEmpty() & livingTokenTypes.isEmpty()) {
                    stillMatching = false;
                } else {
                    character = file.read();
                    columnNumber++;
                }
            }
            chars.deleteCharAt(chars.length()-1);
            String tokenStr = chars.toString();
            char[] tokenChars = tokenStr.toCharArray();
            matchingTokenTypes = new HashSet<>();
            for (SymbolType tokenType : tokenTypes) {
                NFA nfa = tokenRegExMap.get(tokenType);
                nfa.clearState();
                for (int i = 0; i < tokenChars.length; i++) {
                    nfa.inputChar(tokenChars[i]);
                }
                if (nfa.isAccepting()) {
                    matchingTokenTypes.add(tokenType);
                }
            }
            if (matchingTokenTypes.size() == 2) {
                if (matchingTokenTypes.contains(SymbolType.ID)) {
                    matchingTokenTypes.remove(SymbolType.ID);
                } else {
                    throw new ScanException("2:Failed to match token at line " + lastMatchedLineNum + ", column " + lastMatchedColumnNum);
                }
            } else if (matchingTokenTypes.size() > 2) {
                throw new ScanException("2+:Failed to match token at line " + lastMatchedLineNum + ", column " + lastMatchedColumnNum);
            } else if (matchingTokenTypes.size() == 0) {
                if (!whitespace.contains(character) & (character != -1)) {
                    throw new ScanException("0:Failed to match token at line " + lastMatchedLineNum + ", column " + lastMatchedColumnNum);
                }
            }
            for (SymbolType tokenType : matchingTokenTypes) {
                Token newToken = null;
                newToken = new Token(tokenType, tokenStr);
                tokens.add(newToken);
                lastMatchedLineNum = lineNumber;
                lastMatchedColumnNum = columnNumber;
            }
        }
        tokens.add(new Token(SymbolType.EOF, null));
    }
    
    public Token peek() {
        return tokens.get(0);
    }

    public Token pop() {
        return tokens.remove(0);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Token token : tokens) {
            result.append(token + "  ");
        }
        return result.toString();
    }
}