package compiler.shared;

import compiler.Main;
import compiler.shared.Symbol;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;



public class EndToEndTests {

    // TODO: could not figure out
    @Test(timeout = 3000)
    public void testFile0() {
    //     // TODO loop through all test files
    //     int testno = 0;
    //     String expectedAstString;
    //     expectedAstString = EndToEndTests.readFile("testCases/test" + testno + ".ast", StandardCharsets.UTF_8);
    //     if (expectedAstString == null) {
    //         fail("could not get file testCases/test" + testno + ".ast");
    //     }
    //     Symbol ast;
    //     try {
    //         ast = Main.run("/src/main/resources/testCases/test" + testno + ".tgr", false);
    //     } catch (Exception e) {
    //         fail("Could not run parser:\n" + e.toString());
    //         return;
    //     }
    //     if (ast == null) {
    //         fail("ast was null");
    //     }
    //     assertEquals(expectedAstString, ast.toString());
    }

    static String readFile(String path, Charset encoding) {
        InputStream input = EndToEndTests.class.getClassLoader().getResourceAsStream(path);
        if (input == null) {
            return null;
        }
        java.util.Scanner s = new java.util.Scanner(input).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}