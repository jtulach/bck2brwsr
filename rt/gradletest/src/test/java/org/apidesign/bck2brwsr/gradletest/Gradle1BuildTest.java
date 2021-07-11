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
package org.apidesign.bck2brwsr.gradletest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.netbeans.api.scripting.Scripting;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class Gradle1BuildTest {

    @Test
    public void verifyMainJS() throws Exception {
        URL bck2brwsrJs = Gradle1BuildTest.class.getResource("gradle1/build/web/bck2brwsr.js");
        assertNotNull(bck2brwsrJs, "bck2brwsr.js has been generated");
        URL mainJs = Gradle1BuildTest.class.getResource("gradle1/build/web/main.js");
        assertNotNull(mainJs, "main.js has been generated");
        String text = readStream(mainJs);
        assertEquals(text.indexOf("Failed to obfuscate"), -1, "The code should be obfuscated " + mainJs);
        assertClasspath(text, "lib/net.java.html.boot-[0-9\\.\\-SNAPSHOT]*.js", 3);
        assertClasspath(text, "lib/emul-[0-9\\.\\-SNAPSHOT]*-rt.js", 3);

        ScriptEngineManager sem = Scripting.createManager();
        ScriptEngine js = sem.getEngineByMimeType("text/javascript");
        assertNotNull(js, "JavaScript engine has been found");
        Object rawDocument = js.eval("""
            (function(g) {
                var elements = {};

                g.document = {};
                g.document.createElement = function(tagName) {
                    var tag = {};
                    tag.name = tagName;
                    return tag;
                };
                g.document.appendChild = function(tag) {
                    var tagName = tag.name;
                    if (!elements[tagName]) {
                        elements[tagName] = [];
                    }
                    elements[tagName].push(tag);
                };
                g.document.getElementsByTagName = function(tagName) {
                    if (tagName === "head") return [ g.document ];
                    var arr = elements[tagName];
                    return arr ? arr : [];
                };
                return g.document;
            })(this);
            """
        );
        Document p = ((Invocable)js).getInterface(rawDocument, Document.class);
        js.eval(readStream(bck2brwsrJs));
        js.eval(text);
        for (ScriptTag tag : p.getElementsByTagName("script")) {
            URL tagLocation = bck2brwsrJs.toURI().resolve(tag.src()).toURL();
            js.eval(readStream(tagLocation));
            tag.onload(new LoadedEvent(tag));
        }

        Base64Convert.defineAtoB(js);

        Object result = js.eval("""
                (() => {
                    var vm = bck2brwsr();
                    return vm.loadClass('Gradle1Check').invoke('formulate');
                })();
                """);

        assertEquals(result, "Gradle1Check value: 42");
    }

    interface ScriptTag {
        String name();
        String type();
        String src();
        void onload(LoadedEvent ev);
    }

    public static class LoadedEvent {
        public final ScriptTag target;

        LoadedEvent(ScriptTag target) {
            this.target = target;
        }
    }

    interface Document {
        List<ScriptTag> getElementsByTagName(String name);
    }

    private void assertClasspath(String text, String imprt, int expElements) {
        int cp = text.indexOf("classpath");
        assertTrue(cp > 0, "classpath found in\n" + text);
        int begin = text.indexOf("[", cp);
        int end = text.indexOf("]", cp);

        assertTrue(end > begin, "end is after begin: " + end + " > " + begin + "\n" + text);

        String section = text.substring(begin + 1, end);

        String[] elements = section.split(",");
        assertEquals(elements.length, expElements, "Expecting " + expElements + " classpath elements in\n" + section);

        for (String e : elements) {
            e = e.replace('"', ' ').replace('\'', ' ').trim();
            Pattern p = Pattern.compile(imprt);
            Matcher m = p.matcher(e);
            if (m.matches()) {
                return;
            }
        }
        fail("Not found " + imprt + " in\n" + section);
    }

    private static String readStream(URL u) throws IOException {
        InputStream is = u.openStream();
        int len = is.available();
        byte[] arr = new byte[len];
        int read = is.read(arr);
        assertEquals(read, len, "Whole stream read");
        String text = new String(arr);
        return text;
    }

}
