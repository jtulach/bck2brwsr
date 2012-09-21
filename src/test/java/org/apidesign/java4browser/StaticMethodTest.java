/*
Java 4 Browser Bytecode Translator
Copyright (C) 2012-2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. Look for COPYING file in the top folder.
If not, see http://opensource.org/licenses/GPL-2.0.
*/
package org.apidesign.java4browser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/** Checks the basic behavior of the translator.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class StaticMethodTest {
    @Test public void threePlusFour() throws Exception {
        assertExec(
            "Should be seven", 
            "org_apidesign_java4browser_StaticMethod_sumIII", 
            Double.valueOf(7), 
            3, 4
        );
    }

    @Test public void powerOfThree() throws Exception {
        assertExec(
            "Should be nine", 
            "org_apidesign_java4browser_StaticMethod_powerFF", 
            Double.valueOf(9),
            3.0f
        );
    }

    @Test public void doubleWithoutLong() throws Exception {
        assertExec(
            "Should be two",
            "org_apidesign_java4browser_StaticMethod_minusDDJ", 
            Double.valueOf(2),
            3.0d, 1l
        );
    }

    @Test public void divAndRound() throws Exception {
        assertExec(
            "Should be rounded to one",
            "org_apidesign_java4browser_StaticMethod_divIBD", 
            Double.valueOf(1),
            3, 3.75
        );
    }
    @Test public void mixedMethodFourParams() throws Exception {
        assertExec(
            "Should be two",
            "org_apidesign_java4browser_StaticMethod_mixIIJBD", 
            Double.valueOf(20),
            2, 10l, 5, 2.0
        );
    }
    @Test public void factRec() throws Exception {
        assertExec(
            "Factorial of 5 is 120",
            "org_apidesign_java4browser_StaticMethod_factRecJI", 
            Double.valueOf(120),
            5
        );
    }
    @Test public void factIter() throws Exception {
        assertExec(
            "Factorial of 5 is 120",
            "org_apidesign_java4browser_StaticMethod_factIterJI", 
            Double.valueOf(120),
            5
        );
    }
    
    @Test public void xor() throws Exception {
        assertExec(
            "Xor is 4",
            "org_apidesign_java4browser_StaticMethod_xorJIJ",
            Double.valueOf(4),
            7,
            3
        );
    }
    
    @Test public void or() throws Exception {
        assertExec(
            "Or will be 7",
            "org_apidesign_java4browser_StaticMethod_orOrAndJZII",
            Double.valueOf(7),
            true,
            4,
            3
        );
    }
    @Test public void and() throws Exception {
        assertExec(
            "And will be 3",
            "org_apidesign_java4browser_StaticMethod_orOrAndJZII",
            Double.valueOf(3),
            false,
            7,
            3
        );
    }
    @Test public void inc4() throws Exception {
        assertExec(
            "It will be 4",
            "org_apidesign_java4browser_StaticMethod_inc4I",
            Double.valueOf(4)
        );
    }
    
    private static void assertExec(String msg, String methodName, Object expRes, Object... args) throws Exception {
        StringBuilder sb = new StringBuilder();
        Invocable i = compileClass(sb, "org/apidesign/java4browser/StaticMethod");
        
        Object ret = null;
        try {
            ret = i.invokeFunction(methodName, args);
        } catch (ScriptException ex) {
            fail("Execution failed in " + sb, ex);
        } catch (NoSuchMethodException ex) {
            fail("Cannot find method in " + sb, ex);
        }
        if (ret == null && expRes == null) {
            return;
        }
        if (expRes.equals(ret)) {
            return;
        }
        assertEquals(ret, expRes, msg + "was: " + ret + "\n" + sb);
        
    }

    static Invocable compileClass(StringBuilder sb, String... names) throws ScriptException, IOException {
        if (sb == null) {
            sb = new StringBuilder();
        }
        Set<String> processed = new HashSet<String>();

        LinkedList<String> toProcess = new LinkedList<String>(Arrays.asList(names));
        for (;;) {
            toProcess.removeAll(processed);
            if (toProcess.isEmpty()) {
                break;
            }
            String name = toProcess.getFirst();
            processed.add(name);
            if (name.startsWith("java/") && !name.equals("java/lang/Object")) {
                continue;
            }
            InputStream is = StaticMethodTest.class.getClassLoader().getResourceAsStream(name + ".class");
            assertNotNull(is, "Class file found");
            try {
                ByteCodeToJavaScript.compile(is, sb, toProcess);
            } catch (RuntimeException ex) {
                int lastBlock = sb.lastIndexOf("{");
                throw new IllegalStateException(
                    "Error while compiling " + name + "\n" + 
                    sb.substring(lastBlock + 1, sb.length()), 
                    ex
                );
            }
        }
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine js = sem.getEngineByExtension("js");
        try {
            Object res = js.eval(sb.toString());
            assertTrue(js instanceof Invocable, "It is invocable object: " + res);
            return (Invocable)js;
        } catch (ScriptException ex) {
            fail("Could not compile:\n" + sb, ex);
            return null;
        }
    }
}
