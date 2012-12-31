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
import static org.testng.Assert.*;
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
    
    private static CharSequence codeSeq;
    private static Invocable code;
    
    @BeforeClass 
    public void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        code = StaticMethodTest.compileClass(sb, 
            "org/apidesign/vm4brwsr/Exceptions"
        );
        codeSeq = sb;
    }
    private static void assertExec(String msg, Class clazz, String method, Object expRes, Object... args) throws Exception {
        StaticMethodTest.assertExec(code, codeSeq, msg, clazz, method, expRes, args);
    }
}
