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
        Invocable i = compileClass("StaticMethod.class");
        
        Object ret = i.invokeFunction("org_apidesign_java4browser_StaticMethod_sumIII", 3, 4);
        assertEquals(ret, Double.valueOf(7), "Should be seven");
    }

    static Invocable compileClass(String name) throws ScriptException, IOException {
        InputStream is = StaticMethodTest.class.getResourceAsStream(name);
        assertNotNull(is, "Class file found");
        StringBuilder sb = new StringBuilder();
        ByteCodeToJavaScript.compile(name, is, sb);
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
