package compiler.parser;

import compiler.shared.Parser;
import compiler.shared.Scanner;
import compiler.shared.Symbol;
import compiler.shared.SymbolType;
import compiler.shared.Token;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;


public class TestLrParser {

    /**
     * Tests a basic parse of a correct program.
    */
    @Test(timeout = 30000)
    public void testParseSimpleInput() {
        List<Token> list = new ArrayList<>();
        list.add(new Token(SymbolType.LET, "let"));
        list.add(new Token(SymbolType.VAR, "var"));
        list.add(new Token(SymbolType.ID, "_a"));
        list.add(new Token(SymbolType.COLON, ":"));
        list.add(new Token(SymbolType.INT, "int"));
        list.add(new Token(SymbolType.SEMI_COLON, ";"));

        list.add(new Token(SymbolType.IN, "in"));

        list.add(new Token(SymbolType.ID, "a"));
        list.add(new Token(SymbolType.COLON_EQ, ":="));
        list.add(new Token(SymbolType.INT_LIT, "1"));
        list.add(new Token(SymbolType.SEMI_COLON, ";"));

        list.add(new Token(SymbolType.ID, "printi"));
        list.add(new Token(SymbolType.OPEN_PAREN, "("));
        list.add(new Token(SymbolType.ID, "a"));
        list.add(new Token(SymbolType.CLOSE_PAREN, ")"));
        list.add(new Token(SymbolType.SEMI_COLON, ";"));

        list.add(new Token(SymbolType.END, "end"));
        list.add(new Token(SymbolType.EOF, ""));

        LrParser parser = new LrParser();
        Symbol ast;
        try {
            ast = parser.parse(new MockScanner(list), "build/test-results/testParseSimpleInput_debug.txt");
        } catch (Exception e) {
            fail("Parser threw exception" + e);
            return;
        }
        String gotString = ast.toString().toLowerCase().replaceAll("\\s","");
        String expectedString = "(program let (declseg typedecls (vardecls (vardecl var (ids _a) : (type int) optinit ;) vardecls) funcdecls) in (stmts (fullstmt (stmt (lvalue a optoffset) := (numexpr (term (factor (const 1))))) ;) (stmts (fullstmt (stmt optstore printi ( (numexprs (neexprs (numexpr (term (factor a))))) )) ;))) end)";
        expectedString = expectedString.toLowerCase().replaceAll("\\s","");
        assertEquals(expectedString, gotString);
    }

    @Test
    public void testGoTo() {
        LrParser parser;
        Symbol next_token;

        // test one start parse item
        parser = new LrParser();
        next_token = new Symbol("", null, SymbolType.LET);

        Set<ParseItem> start_state;
        start_state = new HashSet<>();
        start_state.add(new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 0, SymbolType.EOF));

        Set<ParseItem> expected;
        expected = new HashSet<>();
        expected.add(new ParseItem(SymbolType.PROGRAM, SymbolType.PROGRAM.derivations[0], 1, SymbolType.EOF));

        Set<ParseItem> got;
        got = parser.goTo(start_state, next_token);
        assertEquals(expected, got);

    }

    @Test
    public void testClosure() {
        // Test cases in the form {{{start items}, {expected new items}}
        ParseItem[][][] cases = {
            {
                {new ParseItem(SymbolType.PRED, SymbolType.PRED.derivations[0], 1, SymbolType.EOF)},
                {
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[0], 0, SymbolType.ID),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[0], 0, SymbolType.OPEN_PAREN),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[0], 0, SymbolType.INT_LIT),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[0], 0, SymbolType.FLOAT_LIT),

                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[1], 0, SymbolType.ID),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[1], 0, SymbolType.OPEN_PAREN),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[1], 0, SymbolType.INT_LIT),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[1], 0, SymbolType.FLOAT_LIT),

                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[2], 0, SymbolType.ID),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[2], 0, SymbolType.OPEN_PAREN),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[2], 0, SymbolType.INT_LIT),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[2], 0, SymbolType.FLOAT_LIT),

                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[3], 0, SymbolType.ID),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[3], 0, SymbolType.OPEN_PAREN),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[3], 0, SymbolType.INT_LIT),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[3], 0, SymbolType.FLOAT_LIT),

                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[4], 0, SymbolType.ID),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[4], 0, SymbolType.OPEN_PAREN),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[4], 0, SymbolType.INT_LIT),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[4], 0, SymbolType.FLOAT_LIT),

                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[5], 0, SymbolType.ID),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[5], 0, SymbolType.OPEN_PAREN),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[5], 0, SymbolType.INT_LIT),
                    new ParseItem(SymbolType.BOOLOP, SymbolType.BOOLOP.derivations[5], 0, SymbolType.FLOAT_LIT),
                }
            }
        };
        for (ParseItem[][] testCase : cases) {
            Set<ParseItem> start = new HashSet<>();
            start.addAll(new ArrayList<ParseItem>(Arrays.asList(testCase[0])));

            Set<ParseItem> expected = new HashSet<>();
            expected.addAll(new ArrayList<ParseItem>(Arrays.asList(testCase[1])));
            expected.addAll(new ArrayList<ParseItem>(Arrays.asList(testCase[0])));

            assertEquals(SymbolType.BOOLOP, testCase[0][0].needsNext());
            LrParser parser = new LrParser();
            Set<ParseItem> got = parser.closure(start);

            // Test expected is a subset of what we got
            if (!got.containsAll(expected)) {
                expected.removeAll(got);                                                    // get what was in expected but we didn't get
                fail("Missing expected elements " + expected);
            }

            // Test that expected is exactly what we got.
            if (!expected.containsAll(got)) {
                got.removeAll(expected);                                                    // get what was in expected but we didn't get
                fail("Extra elements gotten" + got );
            }
        }
    }
}