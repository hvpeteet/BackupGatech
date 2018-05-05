package compiler.scanner;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class State {
    
    private boolean accepting = false;
    private Map<Character,Set<State>> transitions = new HashMap<>();

    public void addTransition(char character, State state) {
        if (transitions.get(character) == null) {
            transitions.put(character, new HashSet<State>());
        }
        transitions.get(character).add(state);
    }

    public Set<State> getNextStates(char input) {
        Set<State> result = transitions.get(input);
        if (result == null) {
            result = new HashSet<>();
        }
        return result;
    }

    public boolean isAccepting() {
        return accepting;
    }

    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }

    public Map<Character, Set<State>> getTransitions() {
        return transitions;
    }

    // public String toString() {
    //  StringBuilder result = new StringBuilder();
    //  result.append("State:\n");
    //  for (Map.Entry<Character,Set<State>> transition : transitions.entrySet()) {
    //      result.append(transition.getKey());
    //      for (State state : transition.getValue()) {
    //          result.append(state.toString());
    //      }
    //  }
    //  return result.toString();
    // }
}