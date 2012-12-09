/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://opensource.org/licenses/GPL-2.0.
 */
package org.apidesign.vm4brwsr;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Checks the basic behavior of the translator.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class StaticMethodTest {
    @Test public void threePlusFour() throws Exception {
        assertExec(
            "Should be seven", 
            StaticMethod.class, "sum__III", 
            Double.valueOf(7), 
            3, 4
        );
    }

    @Test public void checkReallyInitializedValues() throws Exception {
        assertExec(
            "Return true", 
            StaticMethod.class, "isNull__Z", 
            Double.valueOf(1)
        );
    }

    @Test public void powerOfThree() throws Exception {
        assertExec(
            "Should be nine", 
            StaticMethod.class, "power__FF", 
            Double.valueOf(9),
            3.0f
        );
    }

    @Test public void minusOne() throws Exception {
        assertExec(
            "Should be minus one", 
            StaticMethod.class, "minusOne__I", 
            Double.valueOf(-1)
        );
    }

    @Test public void doubleWithoutLong() throws Exception {
        assertExec(
            "Should be two",
            StaticMethod.class, "minus__DDJ", 
            Double.valueOf(2),
            3.0d, 1l
        );
    }

    @Test public void divAndRound() throws Exception {
        assertExec(
            "Should be rounded to one",
            StaticMethod.class, "div__IBD", 
            Double.valueOf(1),
            3, 3.75
        );
    }
    @Test public void mixedMethodFourParams() throws Exception {
        assertExec(
            "Should be two",
            StaticMethod.class, "mix__IIJBD", 
            Double.valueOf(20),
            2, 10l, 5, 2.0
        );
    }
    @Test public void factRec() throws Exception {
        assertExec(
            "Factorial of 5 is 120",
            StaticMethod.class, "factRec__JI", 
            Double.valueOf(120),
            5
        );
    }
    @Test public void factIter() throws Exception {
        assertExec(
            "Factorial of 5 is 120",
            StaticMethod.class, "factIter__JI", 
            Double.valueOf(120),
            5
        );
    }
    
    @Test public void xor() throws Exception {
        assertExec(
            "Xor is 4",
            StaticMethod.class, "xor__JIJ",
            Double.valueOf(4),
            7,
            3
        );
    }
    
    @Test public void or() throws Exception {
        assertExec(
            "Or will be 7",
            StaticMethod.class, "orOrAnd__JZII",
            Double.valueOf(7),
            true,
            4,
            3
        );
    }
    @Test public void nullCheck() throws Exception {
        assertExec(
            "Returns nothing",
            StaticMethod.class, "none__Ljava_lang_Object_2II",
            null, 1, 3
        );
    }
    @Test public void and() throws Exception {
        assertExec(
            "And will be 3",
            StaticMethod.class, "orOrAnd__JZII",
            Double.valueOf(3),
            false,
            7,
            3
        );
    }
    @Test public void inc4() throws Exception {
        assertExec(
            "It will be 4",
            StaticMethod.class, "inc4__I",
            Double.valueOf(4)
        );
    }
    
    @Test public void shiftLeftInJava() throws Exception {
        int res = StaticMethod.shiftLeft(1, 8);
        assertEquals(res, 256);
    }

    @Test public void shiftLeftInJS() throws Exception {
        assertExec(
            "Setting 9th bit",
            StaticMethod.class, "shiftLeft__III",
            Double.valueOf(256),
            1, 8
        );
    }

    @Test public void shiftRightInJava() throws Exception {
        int res = StaticMethod.shiftArithmRight(-8, 3, true);
        assertEquals(res, -1);
    }

    @Test public void shiftRightInJS() throws Exception {
        assertExec(
            "Get -1",
            StaticMethod.class, "shiftArithmRight__IIIZ",
            Double.valueOf(-1),
            -8, 3, true
        );
    }
    @Test public void unsignedShiftRightInJava() throws Exception {
        int res = StaticMethod.shiftArithmRight(8, 3, false);
        assertEquals(res, 1);
    }

    @Test public void unsignedShiftRightInJS() throws Exception {
        assertExec(
            "Get -1",
            StaticMethod.class, "shiftArithmRight__IIIZ",
            Double.valueOf(1),
            8, 3, false
        );
    }
    
    @Test public void javaScriptBody() throws Exception {
        assertExec(
            "JavaScript string",
            StaticMethod.class, "i2s__Ljava_lang_String_2II",
            "333",
            330, 3
        );
    }
    
    @Test public void switchJarda() throws Exception {
        assertExec(
            "The expected value",
            StaticMethod.class, "swtch__Ljava_lang_String_2I",
            "Jarda",
            0
        );
    }
    
    @Test public void switchDarda() throws Exception {
        assertExec(
            "The expected value",
            StaticMethod.class, "swtch__Ljava_lang_String_2I",
            "Darda",
            1
        );
    }
    @Test public void switchParda() throws Exception {
        assertExec(
            "The expected value",
            StaticMethod.class, "swtch2__Ljava_lang_String_2I",
            "Parda",
            22
        );
    }
    @Test public void switchMarda() throws Exception {
        assertExec(
            "The expected value",
            StaticMethod.class, "swtch__Ljava_lang_String_2I",
            "Marda",
            -433
        );
    }
    
    @Test public void checkNullCast() throws Exception {
        assertExec("Null can be cast to any type",
            StaticMethod.class, "castNull__Ljava_lang_String_2Z", 
            null, true
        );
    }
    
    private static CharSequence codeSeq;
    private static Invocable code;
    
    @BeforeClass 
    public void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        code = compileClass(sb, "org/apidesign/vm4brwsr/StaticMethod");
        codeSeq = sb;
    }
    
    
    private static void assertExec(
        String msg, Class clazz, String method, 
        Object expRes, Object... args
    ) throws Exception {
        assertExec(code, codeSeq, msg, clazz, method, expRes, args);
    }
    static void assertExec(
        Invocable toRun, CharSequence theCode,
        String msg, Class clazz, String method, 
        Object expRes, Object... args
    ) throws Exception {
        Object ret = null;
        try {
            ret = toRun.invokeFunction(clazz.getName().replace('.', '_'), true);
            ret = toRun.invokeMethod(ret, method, args);
        } catch (ScriptException ex) {
            fail("Execution failed in\n" + dumpJS(theCode), ex);
        } catch (NoSuchMethodException ex) {
            fail("Cannot find method in\n" + dumpJS(theCode), ex);
        }
        if (ret == null && expRes == null) {
            return;
        }
        if (expRes != null && expRes.equals(ret)) {
            return;
        }
        assertEquals(ret, expRes, msg + "was: " + ret + "\n" + dumpJS(theCode));
        
    }

    static Invocable compileClass(StringBuilder sb, String... names) throws ScriptException, IOException {
        return compileClass(sb, null, names);
    }
    static Invocable compileClass(
        StringBuilder sb, ScriptEngine[] eng, String... names
    ) throws ScriptException, IOException {
        if (sb == null) {
            sb = new StringBuilder();
        }
        GenJS.compile(sb, names);
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine js = sem.getEngineByExtension("js");
        if (eng != null) {
            eng[0] = js;
        }
        try {
            Object res = js.eval(sb.toString());
            assertTrue(js instanceof Invocable, "It is invocable object: " + res);
            return (Invocable)js;
        } catch (Exception ex) {
            if (sb.length() > 2000) {
                sb = dumpJS(sb);
            }
            fail("Could not evaluate:\n" + sb, ex);
            return null;
        }
    }
    static StringBuilder dumpJS(CharSequence sb) throws IOException {
        File f = File.createTempFile("execution", ".js");
        FileWriter w = new FileWriter(f);
        w.append(sb);
        w.close();
        return new StringBuilder(f.getPath());
    }
}
