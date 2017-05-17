/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.truffle;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.BeforeClass;
import org.junit.Test;

public class Bck2BrwsrLanguageTest {

    private static PolyglotEngine engine;
    private static ByteArrayOutputStream out;

    public Bck2BrwsrLanguageTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        out = new ByteArrayOutputStream();
        engine = PolyglotEngine.newBuilder().setErr(out).setOut(out).build();
    }

    @AfterClass
    public static void tearDownClass() {
        engine.dispose();
    }

    @Test
    public void testHelloWorldFromAJar() throws Exception {
        File jar = createHelloJar(false, true);
        String mime = Files.probeContentType(jar.toPath());

        Source src = Source.newBuilder(jar.toURI().toURL()).mimeType(mime).build();
        engine.eval(src);

        PolyglotEngine.Value nonExistingClass;
        try {
            nonExistingClass = engine.findGlobalSymbol(Hello.class.getCanonicalName() + '2');
        } catch (Exception ex) {
            nonExistingClass = null;
        }
        assertNull(nonExistingClass);

        PolyglotEngine.Value in = engine.findGlobalSymbol(Hello.class.getCanonicalName());
        assertNotNull(in);
        Invoke invoke = in.as(Invoke.class);

        out.reset();
        invoke.sayHello();
        assertEquals("Hello from Java!", out.toString("UTF-8").trim());
    }

    @Test
    public void testHelloWorldFromASource() throws Exception {
        Source src = Source.newBuilder(
            "package test; class Hello { static { System.out.println(\"Hello from Code!\"); } }"
        ).mimeType("text/java").name("Hello.java").build();
        engine.eval(src);

        out.reset();
        PolyglotEngine.Value in = engine.findGlobalSymbol("test.Hello");
        assertNotNull(in);
        assertEquals("Hello from Code!", out.toString("UTF-8").trim());
    }

    @Test
    public void testHelloWorldFromMainClass() throws Exception {
        File jar = createHelloJar(true, false);
        String mime = Files.probeContentType(jar.toPath());

        out.reset();
        Source src = Source.newBuilder(jar.toURI().toURL()).mimeType(mime).build();
        engine.eval(src);
        assertEquals("Hello from Main!", out.toString("UTF-8").trim());
    }

    @Test
    public void testHelloWorldFromMainClassAndOsgi() throws Exception {
        File jar = createHelloJar(true, true);
        String mime = Files.probeContentType(jar.toPath());

        out.reset();
        Source src = Source.newBuilder(jar.toURI().toURL()).mimeType(mime).build();
        engine.eval(src);
        assertEquals("Hello from Main!", out.toString("UTF-8").trim());
    }

    private File createHelloJar(boolean mainClass, boolean osgi) throws IOException {
        URL u = Bck2BrwsrLanguageTest.class.getResource("Hello.class");
        assertNotNull("Hello.class found", u);

        File jar = File.createTempFile("hello", ".jar");
        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        if (osgi) {
            mf.getMainAttributes().putValue("Bundle-SymbolicName", "test");
            mf.getMainAttributes().putValue("Export-Package", Hello.class.getPackage().getName());
        }
        if (mainClass) {
            mf.getMainAttributes().putValue("Main-Class", Hello.class.getName());
        }
        jar.deleteOnExit();
        JarOutputStream os = new JarOutputStream(new FileOutputStream(jar), mf);
        os.putNextEntry(new JarEntry(Hello.class.getCanonicalName().replace('.', '/') + ".class"));
        InputStream is = u.openStream();
        byte[] arr = new byte[4096];
        for (;;) {
            int len = is.read(arr);
            if (len == -1) {
                break;
            }
            os.write(arr, 0, len);
        }
        is.close();
        os.closeEntry();
        os.close();
        return jar;
    }

    @FunctionalInterface
    private static interface Invoke {
        void sayHello(String... args) throws Exception;
    }
}
