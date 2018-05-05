package compiler.scanner;

public class RegEx {
    private String regex;
    private NFA nfa;

    public RegEx(String regex) {
        this.regex = regex;
        this.nfa = new NFA(regex);
    }

    public boolean matches(String str) {
        nfa.clearState();
        char[] characters = str.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            nfa.inputChar(characters[i]);
        }
        return nfa.isAccepting();
    }

    public boolean couldMatch(String str) {
        nfa.clearState();
        char[] characters = str.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            nfa.inputChar(characters[i]);
        }
        return nfa.couldAccept();
    }

    public String toString() {
        return nfa.toString();
    }
}