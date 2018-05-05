package compiler.scanner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;

public class NFA {

    private State initialState;
    private State finalState;
    private Set<State> currentStates;

    // public static void main(String[] args) {
    //     NFA nfa = new NFA("a");
    //     System.out.println(nfa);
    // }
    
    public NFA(String regex) {
        // initialState = new State();
        // finalState = initialState;
        // finalState.setAccepting(true);
        char[] characters = regex.toCharArray();
        int firstOr = -1;
        int index = 0;
        while ((firstOr == -1) & (index < characters.length)) {
            if (characters[index] == '|') {
                if ((index > 0) & (characters[index-1] != '\\'))
                firstOr = index;
            }
            index++;
        }
        if (firstOr != -1) {
            char[] beginning = Arrays.copyOfRange(characters, 0, firstOr);
            char[] end = Arrays.copyOfRange(characters, firstOr+1, characters.length);
            NFA beginningNFA = new NFA(String.valueOf(beginning));
            NFA endNFA = new NFA(String.valueOf(end));
            initialState = new State();
            finalState = new State();
            finalState.setAccepting(true);
            initialState.addTransition('~', beginningNFA.getInitialState());
            initialState.addTransition('~', endNFA.getInitialState());
            beginningNFA.getFinalState().setAccepting(false);
            beginningNFA.getFinalState().addTransition('~', finalState);
            endNFA.getFinalState().setAccepting(false);
            endNFA.getFinalState().addTransition('~', finalState);
        } else {
            initialState = new State();
            finalState = initialState;
            finalState.setAccepting(true);
            for (int i = 0; i < characters.length; i++) {
                char c = characters[i];
                Set<Character> acceptedChars = null;
                if (c == '\\') {
                    i++;
                    acceptedChars = new HashSet<Character>();
                    acceptedChars.add(characters[i]);
                } else if (c == '[') {
                    i++;
                    int startCharClass = i;
                    while (characters[i] != ']') {
                        i++;
                    }
                    int endCharClass = i;
                    acceptedChars = getCharacterClass(Arrays.copyOfRange(characters, startCharClass, endCharClass));
                } else {
                    acceptedChars = new HashSet<Character>();
                    acceptedChars.add(characters[i]);
                }
                // State newFinal = new State();
                State beforeChar = new State();
                State afterChar = new State();
                afterChar.setAccepting(true);
                for (Character transitionChar : acceptedChars) { 
                    beforeChar.addTransition(transitionChar, afterChar);
                    // finalState.addTransition(transitionChar, finalState);
                    // finalState = newFinal;
                }
                if ((i + 1 < characters.length) && (characters[i + 1] == '*')) {
                    i++;
                    State newInitialState = new State();
                    State newFinalState = new State();
                    newFinalState.setAccepting(true);
                    afterChar.setAccepting(false);
                    newInitialState.addTransition('~', beforeChar);
                    newInitialState.addTransition('~', newFinalState);
                    afterChar.addTransition('~', newFinalState);
                    afterChar.addTransition('~', beforeChar);
                    beforeChar = newInitialState;
                    afterChar = newFinalState;
                }
                finalState.setAccepting(false);
                finalState.addTransition('~', beforeChar);
                finalState = afterChar;
            }
        }
        clearState();
    }

    private Set<Character> getCharacterClass(char[] characters) {
        Set<Character> acceptedChars = new HashSet<>();
        for (int i = 0; i < characters.length; i++) {
            if (characters[i] == '-') {
                int start = (int)characters[i-1];
                int end = (int)characters[i+1];
                i++;
                for (int index = start + 1; index <= end; index++) {
                    acceptedChars.add((char)index);
                }
            } else {
                acceptedChars.add(characters[i]);
            }
        }
        return acceptedChars;
    }

    public State getInitialState() {
        return initialState;
    }

    public State getFinalState() {
        return finalState;
    }

    public void clearState() {
        currentStates = new HashSet<State>();
        currentStates.add(initialState);
        inputChar('~');
    }

    public void inputChar(char nextChar) {
        Set<State> nextStates = new HashSet<>();
        for (State eachState : currentStates) {
            for (State state : eachState.getNextStates(nextChar)) {
                nextStates.add(state);
            }
        }
        LinkedList<State> stateQueue = new LinkedList<>();
        for (State state : nextStates) {
            stateQueue.add(state);
        }
        while (stateQueue.peek() != null) {
            State eachState = stateQueue.poll();
            for (State state : eachState.getNextStates('~')) {
                if (!nextStates.contains(state)) {
                    nextStates.add(state);
                    stateQueue.add(state);
                }             
            }
        }
        currentStates = nextStates;
    }

    public boolean isAccepting() {
        boolean isAccepting = false;
        for (State eachState : currentStates) {
            if (eachState.isAccepting()) {
                isAccepting = true;
            }
        }
        return isAccepting;
    }

    public boolean couldAccept() {
        return !currentStates.isEmpty();
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        HashMap<State, Integer> stateNums = new HashMap<>();
        Set<State> searched = new HashSet<>();
        LinkedList<State> toSearch = new LinkedList<>();
        int counter = 0;
        stateNums.put(initialState, counter);
        counter++;
        toSearch.add(initialState);
        while (toSearch.peek() != null) {
            State currentState = toSearch.remove();
            result.append(stateNums.get(currentState) + "(" + currentState.isAccepting() + "): ");
            for (Map.Entry<Character,Set<State>> transition : currentState.getTransitions().entrySet()) {
                result.append(transition.getKey() + ": ");
                for (State state : transition.getValue()) {
                    if (!stateNums.containsKey(state)) {
                        stateNums.put(state, counter);
                        counter++;
                        toSearch.add(state);
                    }
                    result.append(stateNums.get(state) + ", ");
                }
            }
            result.append("\n");
            searched.add(currentState);
        }
        // Set<Integer> currStateNums = new HashSet<>();
        result.append("Current States: ");
        for (State currState : currentStates) {
            result.append(stateNums.get(currState)+ ", ");
        }
        return result.toString();
    }
}
