package compiler.scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestRegEx {
    @Test
    public void testSingleChar() {
        RegEx regex = new RegEx("a");

        assertEquals(true, regex.matches("a"));

        assertEquals(false, regex.matches("b"));

        assertEquals(false, regex.matches("aa"));

        assertEquals(false, regex.matches(""));

        assertEquals(false, regex.matches("woijdfa"));
    }

    @Test
    public void testSingleOr() {
        RegEx regex = new RegEx("a|b");

        assertEquals(true, regex.matches("a"));

        assertEquals(true, regex.matches("b"));

        assertEquals(false, regex.matches(""));

        assertEquals(false, regex.matches("aa"));

        assertEquals(false, regex.matches("ab"));

        assertEquals(false, regex.matches("woijdfa"));
    }

    @Test
    public void testCharacterClass() {
        RegEx regex = new RegEx("[?_c-fB-D5-9Z]");

        assertEquals(true, regex.matches("c"));

        assertEquals(true, regex.matches("e"));

        assertEquals(true, regex.matches("f"));

        assertEquals(true, regex.matches("B"));

        assertEquals(true, regex.matches("D"));

        assertEquals(true, regex.matches("C"));

        assertEquals(true, regex.matches("5"));

        assertEquals(true, regex.matches("7"));

        assertEquals(true, regex.matches("9"));

        assertEquals(true, regex.matches("_"));

        assertEquals(true, regex.matches("?"));

        assertEquals(true, regex.matches("Z"));

        assertEquals(false, regex.matches(""));

        assertEquals(false, regex.matches("+"));

        assertEquals(false, regex.matches("a"));

        assertEquals(false, regex.matches("b"));

        assertEquals(false, regex.matches("g"));

        assertEquals(false, regex.matches("t"));

        assertEquals(false, regex.matches("z"));

        assertEquals(false, regex.matches("A"));

        assertEquals(false, regex.matches("E"));

        assertEquals(false, regex.matches("Y"));

        assertEquals(false, regex.matches("1"));

        assertEquals(false, regex.matches("4"));

        assertEquals(false, regex.matches("0"));

        assertEquals(false, regex.matches("BD"));

        assertEquals(false, regex.matches("57"));

        assertEquals(false, regex.matches("a8bds8"));   
    }

    @Test
    public void testRepetition() {
        RegEx regex = new RegEx("a*");

        assertEquals(true, regex.matches("a"));

        assertEquals(true, regex.matches("aaa"));

        assertEquals(true, regex.matches("aa"));

        assertEquals(true, regex.matches(""));

        assertEquals(false, regex.matches("b"));

        assertEquals(false, regex.matches("bdaaa"));

        assertEquals(false, regex.matches("aaacaaaa"));
    }

    @Test
    public void testEscapeSlash() {
        RegEx regex = new RegEx("\\[");

        assertEquals(true, regex.matches("["));

        assertEquals(false, regex.matches("b"));

        assertEquals(false, regex.matches(""));

        regex = new RegEx("a\\*");

        assertEquals(true, regex.matches("a*"));

        assertEquals(false, regex.matches("a"));

        assertEquals(false, regex.matches(""));

        assertEquals(false, regex.matches("aaaa"));

        assertEquals(false, regex.matches("b*"));

        regex = new RegEx("a\\\\*");

        assertEquals(true, regex.matches("a\\\\\\\\"));

        assertEquals(true, regex.matches("a\\"));

        assertEquals(true, regex.matches("a"));

        assertEquals(false, regex.matches(""));

        assertEquals(false, regex.matches("aaaa\\*"));

        assertEquals(false, regex.matches("b*"));
    }

    @Test
    public void testAllOps() {
        RegEx regex = new RegEx("__*[a-zA-Z0-9][_a-zA-Z0-9]*|[a-zA-Z][_a-zA-Z0-9]*");

        assertEquals(true, regex.matches("a"));

        assertEquals(true, regex.matches("ace"));

        assertEquals(true, regex.matches("bobby1"));

        assertEquals(true, regex.matches("fu8724a"));

        assertEquals(true, regex.matches("bar142"));

        assertEquals(true, regex.matches("_interface1"));

        assertEquals(true, regex.matches("__DEADPOOL__"));

        assertEquals(true, regex.matches("HenryIsSmart7"));

        assertEquals(true, regex.matches("______f"));

        assertEquals(true, regex.matches("______1"));

        assertEquals(true, regex.matches("_87"));

        assertEquals(true, regex.matches("_f_fwe_fsf342_f23"));

        assertEquals(true, regex.matches("b23f2____1"));

        assertEquals(false, regex.matches("_"));

        assertEquals(false, regex.matches(""));

        assertEquals(false, regex.matches("_____"));

        assertEquals(false, regex.matches("57"));
    }
}