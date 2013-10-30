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

import javax.script.ScriptException;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Tomas Zezula <tzezula@netbeans.org>
 */
public class ExceptionsTest {
    @Test
    public void verifyMethodWithTryCatchNoThrow() throws Exception {
            assertExec(
                    "No throw",
                    Exceptions.class,
                    "methodWithTryCatchNoThrow__I",
                    new Double(1.0));
    }

    @Test
    public void catchJavaScriptStringAsThrowable() throws Exception {
        assertExec(
            "Throw hello!",
            Exceptions.class,
            "catchThrowableCatchesAll__Ljava_lang_String_2",
            "Hello!"
        );
    }

    @Test
    public void verifyMethodWithTryCatchThrow() throws Exception {
            assertExec(
                    "Throw",
                    Exceptions.class,
                    "methodWithTryCatchThrow__I",
                    new Double(2.0));
    }
    
    @Test public void createObject() throws Exception {
        assertExec("Object created", Exceptions.class, 
            "newInstance__Ljava_lang_String_2Ljava_lang_String_2",
            "java.lang.Object",
            "java.lang.Object"
        );
    }

    @Test public void createFloatFails() throws Exception {
        assertExec("Float not created", Exceptions.class, 
            "newInstance__Ljava_lang_String_2Ljava_lang_String_2",
            "java.lang.Float",
            "java.lang.Float"
        );
    }

    @Test public void createUnknownFails() throws Exception {
        assertExec("Object created", Exceptions.class, 
            "newInstance__Ljava_lang_String_2Ljava_lang_String_2",
            "CNFE:org.apidesign.Unknown",
            "org.apidesign.Unknown"
        );
    }
    
    @Test public void testThreeCalls() throws Exception {
        Object clazz = code.loadClass("loadClass", Exceptions.class.getName());
        
        String method = "readCounter__ILjava_lang_String_2";
        
        try {
            Object ret = code.invokeMethod(clazz, method, "org.apidesign.Unknown");
            fail("We expect an CNFE!");
        } catch (ScriptException scriptException) {
            // script exception should be OK
        }
        {
            // 2nd invocation
            Object ret = code.invokeMethod(clazz, method, "java.lang.String");
            assertTrue(ret instanceof Number, "Is number: " + ret);
            assertEquals(((Number)ret).doubleValue(), 2.0);
        }
        {
            // 3rd invocation
            Object ret = code.invokeMethod(clazz, method, "java.lang.Integer");
            assertTrue(ret instanceof Number, "Is number: " + ret);
            assertEquals(((Number)ret).doubleValue(), 3.0);
        }
    }
    
    private static TestVM code;
    
    @BeforeClass 
    public void compileTheCode() throws Exception {
        code = TestVM.compileClass("org/apidesign/vm4brwsr/Exceptions");
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }
    private static void assertExec(String msg, Class clazz, String method, Object expRes, Object... args) throws Exception {
        code.assertExec(msg, clazz, method, expRes, args);
    }
}
