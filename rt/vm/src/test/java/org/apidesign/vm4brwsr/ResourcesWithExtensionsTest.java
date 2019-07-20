/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2018 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import java.io.UnsupportedEncodingException;
import javax.script.ScriptEngine;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Tests related to loading resources from the VM.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ResourcesWithExtensionsTest {
    @Test public void checkHello() throws Exception {
        String exp = "Hello ";
        
        assertExec("Loading a precompiled resource:",
            Resources.class, "loadJustHello__Ljava_lang_String_2", 
            exp
        );
    }

    @Test public void checkHelloWorld() throws Exception {
        String exp = "Hello World!";
        exp = exp + exp.hashCode();
        
        assertExec("Loading precompiled resources:",
            Resources.class, "loadHello__Ljava_lang_String_2", 
            exp
        );
    }
    
    @Test public void objJSResourceIsNotFound() throws Exception {
        assertExec("Objects from @JavaScriptResource resources are available",
            Resources.class, "isObj__Z", 1.0
        );
    }

    @Test public void yellowHorseFound() throws Exception {
        assertExec("Non ASCII text properly transpiled",
            Resources.class, "getHorse__Ljava_lang_String_2", "Žluťoučký kůň"
        );
    }

    @Test public void objJSIsFound() throws Exception {
        assertExec("The resources used as @JavaScriptResource aren't available",
            Resources.class, "isResource__ZLjava_lang_String_2", 0.0, "obj"
        );
    }

    @Test public void undefinedIsNull() throws Exception {
        assertExec("undefined is converted to null on return",
            Resources.class, "unknown__Ljava_lang_String_2", null
        );
    }

    @Test public void varsAreOK() throws Exception {
        assertExec("Access to args",
            Resources.class, "addDirect__Ljava_lang_String_2", "HelloWorld"
        );
    }

    @Test public void thisObjJSIsFound() throws Exception {
        assertExec("The resources used as @JavaScriptResource aren't available",
            Resources.class, "isResource__ZLjava_lang_String_2", 0.0, "thisObj"
        );
    }

    private static TestVM code;
    
    @BeforeClass 
    public static void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        ScriptEngine[] eng = { null };
        code = TestVM.compileClassAsExtension(sb, eng, 
            "org/apidesign/vm4brwsr/Resources", 
            "META-INF/ahoj", "Hello "
        );
        code = TestVM.compileClassAsExtension(sb, eng, 
            "org/apidesign/vm4brwsr/Resources", 
            "META-INF/ahoj", "World!"
        );
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }

    private void assertExec(
        String msg, Class<?> clazz, String method, 
        Object ret, Object... args
    ) throws Exception {
        code.assertExec(msg, clazz, method, ret, args);
    }
    
    public static String parseBase64Binary(String s) throws UnsupportedEncodingException {
        return ResourcesTest.parseBase64Binary(s);
    }
}
