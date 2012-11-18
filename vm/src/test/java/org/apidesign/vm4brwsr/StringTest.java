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

import javax.script.Invocable;
import javax.script.ScriptException;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class StringTest {
    @Test public void firstChar() throws Exception {
        assertExec(
            "First char in Hello is H",
            "org_apidesign_vm4brwsr_StringSample_sayHelloCI",
            72, 0
        );
    }

    @Test public void fromChars() throws Exception {
        assertExec(
            "First char in Hello is ABC",
            "org_apidesign_vm4brwsr_StringSample_fromCharsLjava_lang_StringCCC",
            "ABC", 'A', 'B', 'C'
        );
    }

    @Test public void concatChars() throws Exception {
        assertExec(
            "Composing yields ABC",
            "org_apidesign_vm4brwsr_StringSample_charsLjava_lang_StringCCC",
            "ABC", 'A', 'B', 'C'
        );
    }

    @Test public void concatCharsFromInts() throws Exception {
        assertExec(
            "Composing yields ABC",
            "org_apidesign_vm4brwsr_StringSample_charsFromNumbersLjava_lang_String",
            "ABC"
        );
    }

    @Test public void concatCharsFromChars() throws Exception {
        assertExec(
            "Composing yields ABC",
            "org_apidesign_vm4brwsr_StringSample_charsFromCharsLjava_lang_String",
            "ABC"
        );
    }

    @Test(timeOut=10000) public void toStringConcatenation() throws Exception {
        assertExec(
            "Five executions should generate 5Hello World!",
            "org_apidesign_vm4brwsr_StringSample_toStringTestLjava_lang_StringI",
            "Hello World!5", 5
        );
    }
    @Test public void toStringConcatenationJava() throws Exception {
        assertEquals("Hello World!5", StringSample.toStringTest(5));
    }
    
    @Test(timeOut=10000) public void stringStringConcat() throws Exception {
        assertExec(
            "Composes strings OK",
            "org_apidesign_vm4brwsr_StringSample_concatStringsLjava_lang_String",
            "Hello World!1" + "\\\n\r\t"
        );
    }

    @Test public void equalsAndSubstring() throws Exception {
        assertExec(
            "Composes are OK",
            "org_apidesign_vm4brwsr_StringSample_equalToHelloZII",
            true, 0, 5
        );
    }
    @Test public void replaceChars() throws Exception {
        assertExec(
            "Can replace slashes by underscores",
            "org_apidesign_vm4brwsr_StringSample_replaceLjava_lang_StringLjava_lang_StringCC",
            "x_y_z", "x/y/z", '/', '_'
        );
    }
    @Test public void replaceIntChars() throws Exception {
        assertExec(
            "Can replace slashes by underscores",
            "org_apidesign_vm4brwsr_StringSample_replaceLjava_lang_StringLjava_lang_StringCC",
            "x_y_z", "x/y/z", (int)'/', (int)'_'
        );
    }

    @Test public void insertBuilder() throws Exception {
        assertExec(
            "Can insert something into a buffer?",
            "org_apidesign_vm4brwsr_StringSample_insertBufferLjava_lang_String",
            "Ahoj Jardo!"
        );
    }
    
    @Test public void countAB() throws Exception {
        assertEquals(StringSample.countAB("Ahoj Bedo!"), 3, "Verify Java code is sane");
        assertExec(
            "One A and one B adds to 3",
            "org_apidesign_vm4brwsr_StringSample_countABILjava_lang_String",
            Double.valueOf(3), "Ahoj Bedo!"
        );
        
    }
    
    private static CharSequence codeSeq;
    private static Invocable code;
    
    @BeforeClass 
    public void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        code = StaticMethodTest.compileClass(sb, 
            "org/apidesign/vm4brwsr/StringSample",
            "java/lang/String"
        );
        codeSeq = sb;
    }
    
    private static void assertExec(String msg, String methodName, Object expRes, Object... args) throws Exception {
        Object ret = null;
        try {
            ret = code.invokeFunction(methodName, args);
        } catch (ScriptException ex) {
            fail("Execution failed in\n" + codeSeq, ex);
        } catch (NoSuchMethodException ex) {
            fail("Cannot find method in\n" + codeSeq, ex);
        }
        if (ret == null && expRes == null) {
            return;
        }
        if (expRes.equals(ret)) {
            return;
        }
        assertEquals(ret, expRes, msg + "was: " + ret + "\n" + codeSeq);
        
    }
    
}
