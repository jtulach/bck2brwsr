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
package org.apidesign.bck2brwsr.htmlpage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apidesign.vm4brwsr.Bck2Brwsr;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ProcessPageTest {
    
    
    @Test public void findsThreeIds() throws IOException {
        InputStream is = ProcessPageTest.class.getResourceAsStream("TestPage.html");
        assertNotNull(is, "Sample HTML page found");
        ProcessPage res = ProcessPage.readPage(is);
        final Set<String> ids = res.ids();
        assertEquals(ids.size(), 3, "Three ids found: " + ids);
        
        assertEquals(res.tagNameForId("pg.title"), "title");
        assertEquals(res.tagNameForId("pg.button"), "button");
        assertEquals(res.tagNameForId("pg.text"), "input");
    }
    
    @Test public void testCompileAndRunPageController() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(
              "var window = new Object();\n"
            + "var doc = new Object();\n"
            + "doc.button = new Object();\n"
            + "doc.title = new Object();\n"
            + "doc.title.innerHTML = 'nothing';\n"
            + "doc.text = new Object();\n"
            + "doc.text.value = 'something';\n"
            + "doc.getElementById = function(id) {\n"
            + "    switch(id) {\n"
            + "      case 'pg.button': return doc.button;\n"
            + "      case 'pg.title': return doc.title;\n"
            + "      case 'pg.text': return doc.text;\n"
            + "    }\n"
            + "    throw id;\n"
            + "  }\n"
            + "\n"
            + "function clickAndCheck() {\n"
            + "  doc.button.onclick();\n"
            + "  return doc.title.innerHTML.toString();\n"
            + "};\n"
            + "\n"
            + "window.document = doc;\n"
        );
        Invocable i = compileClass(sb, "org/apidesign/bck2brwsr/htmlpage/PageController");

        Object ret = null;
        try {
            ret = i.invokeFunction("clickAndCheck");
        } catch (ScriptException ex) {
            fail("Execution failed in " + sb, ex);
        } catch (NoSuchMethodException ex) {
            fail("Cannot find method in " + sb, ex);
        }
        assertEquals(ret, "You want this window to be named something", "We expect that the JavaCode performs all the wiring");
    }
    
    @Test public void clickWithArgumentCalled() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(
              "var window = new Object();\n"
            + "var doc = new Object();\n"
            + "doc.button = new Object();\n"
            + "doc.title = new Object();\n"
            + "doc.title.innerHTML = 'nothing';\n"
            + "doc.text = new Object();\n"
            + "doc.text.value = 'something';\n"
            + "doc.getElementById = function(id) {\n"
            + "    switch(id) {\n"
            + "      case 'pg.button': return doc.button;\n"
            + "      case 'pg.title': return doc.title;\n"
            + "      case 'pg.text': return doc.text;\n"
            + "    }\n"
            + "    throw id;\n"
            + "  }\n"
            + "\n"
            + "function clickAndCheck() {\n"
            + "  doc.title.onclick();\n"
            + "  return doc.title.innerHTML.toString();\n"
            + "};\n"
            + "\n"
            + "window.document = doc;\n"
        );
        Invocable i = compileClass(sb, 
            "org/apidesign/bck2brwsr/htmlpage/PageController"
        );

        Object ret = null;
        try {
            ret = i.invokeFunction("clickAndCheck");
        } catch (ScriptException ex) {
            fail("Execution failed in " + sb, ex);
        } catch (NoSuchMethodException ex) {
            fail("Cannot find method in " + sb, ex);
        }
        assertEquals(ret, "pg.title", "Title has been passed to the method argument");
    }

    static Invocable compileClass(StringBuilder sb, String... names) throws ScriptException, IOException {
        if (sb == null) {
            sb = new StringBuilder();
        }
        Bck2Brwsr.generate(sb, ProcessPageTest.class.getClassLoader(), names);
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine js = sem.getEngineByExtension("js");
        try {
            Object res = js.eval(sb.toString());
            assertTrue(js instanceof Invocable, "It is invocable object: " + res);
            return (Invocable) js;
        } catch (ScriptException ex) {
            fail("Could not compile:\n" + sb, ex);
            return null;
        }
    }
}
