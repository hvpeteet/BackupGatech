package compiler.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestNFA {
    @Test
    public void testSingleChar() {
        NFA nfa = new NFA("a");

        assertEquals(false, nfa.isAccepting());

        nfa.inputChar('a');

        assertEquals(true, nfa.isAccepting());

        nfa.inputChar('b');

        assertEquals(false, nfa.isAccepting());

        nfa.inputChar('a');

        assertEquals(false, nfa.isAccepting());
    }

    @Test
    public void testSingleOr() {
        NFA nfa = new NFA("a|b");

        assertEquals(false, nfa.isAccepting());

        nfa.inputChar('a');

        assertEquals(true, nfa.isAccepting());

        nfa.inputChar('b');

        assertEquals(false, nfa.isAccepting());

        nfa.inputChar('a');

        assertEquals(false, nfa.isAccepting());

        nfa.clearState();

        assertEquals(false, nfa.isAccepting());

        nfa.inputChar('b');

        assertEquals(true, nfa.isAccepting());

        nfa.inputChar('b');

        assertEquals(false, nfa.isAccepting());

        nfa.inputChar('a');

        assertEquals(false, nfa.isAccepting());

        nfa.clearState();

        assertEquals(false, nfa.isAccepting());

        nfa.inputChar('c');

        assertEquals(false, nfa.isAccepting());

        nfa.inputChar('b');

        assertEquals(false, nfa.isAccepting());

        nfa.inputChar('a');

        assertEquals(false, nfa.isAccepting());
    }

    // @Test
    // public void testCharacterClass() {
    // 	NFA nfa = new NFA("[c-fB-D5-9]");

    	
    // }
}