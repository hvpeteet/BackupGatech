package compiler.parser;

import compiler.shared.Symbol;
import compiler.shared.SymbolType;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

public class TestParseItem {
    @Test
    public void testToString() {
        ParseItem item;
        String expected;

        item = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 0, SymbolType.EOF);
        expected = "(program --> * let declseg in stmts end, eof)";
        assertEquals(expected, item.toString());

        item = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 1, SymbolType.EOF);
        expected = "(program --> let * declseg in stmts end, eof)";
        assertEquals(expected, item.toString());

        item = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 5, SymbolType.EOF);
        expected = "(program --> let declseg in stmts end *, eof)";
        assertEquals(expected, item.toString());
    }

    @Test
    public void testEquals() {
        ParseItem one;
        ParseItem two;

        one = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 0, SymbolType.EOF);
        two = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 0, SymbolType.EOF);
        assertEquals(one, two);

        one = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 0, SymbolType.EOF);
        two = new ParseItem(SymbolType.PROGRAM, new SymbolType[] {SymbolType.LET, SymbolType.DECLSEG, SymbolType.IN, SymbolType.STMTS, SymbolType.END}, 0, SymbolType.EOF);
        assertEquals(one, two);

        one = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 0, SymbolType.EOF);
        two = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 1, SymbolType.EOF);
        assertNotEquals(one, two);

        one = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 0, SymbolType.EOF);
        two = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 0, SymbolType.COLON);
        assertNotEquals(one, two);

        one = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 0, SymbolType.EOF);
        two = new ParseItem(SymbolType.COLON, SymbolType.PROGRAM.derivations[0], 0, SymbolType.EOF);
        assertNotEquals(one, two);

        one = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 0, SymbolType.EOF);
        two = new ParseItem(SymbolType.PROGRAM, new SymbolType[] {SymbolType.LET, SymbolType.DECLSEG, SymbolType.IN, SymbolType.STMTS, SymbolType.END, SymbolType.COLON}, 0, SymbolType.EOF);
        assertNotEquals(one, two);

        one = new ParseItem(SymbolType.PROGRAM, new SymbolType[] {SymbolType.LET, SymbolType.DECLSEG, SymbolType.IN, SymbolType.STMTS, SymbolType.END, SymbolType.COLON}, 0, SymbolType.EOF);
        two = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 0, SymbolType.EOF);
        assertNotEquals(one, two);
    }

    @Test
    public void testIsComplete() {
        Symbol eof = new Symbol("", null, SymbolType.EOF);

        ParseItem start_placeholder = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 0, SymbolType.EOF);
        if (start_placeholder.isComplete(eof)) {
            fail("(start_placeholder) is not complete but method said it was");
        }

        ParseItem one_placeholder = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 1, SymbolType.EOF);
        if (one_placeholder.isComplete(eof)) {
            fail("(one_placeholder) is not complete but method said it was");
        }

        ParseItem bad_end = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], SymbolType.PROGRAM.derivations[0].length, SymbolType.LET);
        if (bad_end.isComplete(eof)) {
            fail("(bad_end) is not complete but method said it was");
        }

        ParseItem good = new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], SymbolType.PROGRAM.derivations[0].length, SymbolType.EOF);
        if (!good.isComplete(eof)) {
            fail("complete parse item did not return true");
        }
    }

    @Test
    public void testFirsts() {
        // First item is the key, the rest are the expected first set.
        SymbolType[][] goodCases = {
            {SymbolType.PLUS, SymbolType.PLUS},
            {SymbolType.BOOLOP, SymbolType.EQUALS, SymbolType.NOT_EQUALS, SymbolType.LESS_THAN_EQUAL, SymbolType.GREATER_THAN_EQUAL, SymbolType.LESS_THAN, SymbolType.GREATER_THAN},
            {SymbolType.FACTOR, SymbolType.ID, SymbolType.OPEN_PAREN, SymbolType.INT_LIT, SymbolType.FLOAT_LIT},
            {SymbolType.TERM, SymbolType.ID, SymbolType.OPEN_PAREN, SymbolType.INT_LIT, SymbolType.FLOAT_LIT}
        };

        for (SymbolType[] testCase : goodCases) {
            Set<SymbolType> expected = new HashSet<>();
            for (int i = 1; i < testCase.length; i++) {
                expected.add(testCase[i]);
            }
            Set<SymbolType> got = testCase[0].firsts();
            assertEquals(expected, got);
        }
    }
}