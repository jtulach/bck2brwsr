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
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

public class JavaJSInteropTest {
    private static PolyglotEngine engine;

    @BeforeClass
    public static void setUpClass() {
        engine = PolyglotEngine.newBuilder().build();
    }

    @AfterClass
    public static void tearDownClass() {
        engine.dispose();
    }

    @Test
    public void accessStaticMethods() throws Exception {
        Source src = Source.newBuilder(
              "package test;\n"
            + "final class Sum {\n"
            + "  public static int add(int a, int b) {\n"
            + "    return a + b;\n"
            + "  }\n"
            + "  public static int all(int[] a) {\n"
            + "    int sum = 0;\n"
            + "    for (int v : a) { sum += v; };\n"
            + "    return sum;\n"
            + "  }\n"
            + "}\n"
        ).mimeType("text/java").name("Sum.java").build();
        engine.eval(src);

        Source js = Source.newBuilder(
                "var Sum = Interop.import('test.Sum');\n"
              + "Sum.add(1, 6) + Sum.all([3, 3]);"
        ).mimeType("text/javascript").name("thirteen.js").build();

        int value = engine.eval(js).as(Number.class).intValue();
        assertEquals(13, value);
    }

    @Test
    public void accessInstanceMethods() throws Exception {
        Source src = Source.newBuilder(
              "package testinst;\n"
            + "public final class Sum {\n"
            + "  public int add(int a, int b) {\n"
            + "    return a + b;\n"
            + "  }\n"
            + "  public int all(int[] a) {\n"
            + "    int sum = 0;\n"
            + "    for (int v : a) { sum += v; };\n"
            + "    return sum;\n"
            + "  }\n"
            + "}\n"
        ).mimeType("text/java").name("Sum.java").build();
        engine.eval(src);

        Source js = Source.newBuilder(
                "var Sum = Interop.import('testinst.Sum');\n"
              + "var sum = new Sum();"
              + "sum.add(1, 6) + sum.all([3, 3]);"
        ).mimeType("text/javascript").name("thirteen.js").build();

        int value = engine.eval(js).as(Number.class).intValue();
        assertEquals(13, value);
    }

    @Test
    public void instanceArgument() throws Exception {
        Source src = Source.newBuilder(
              "package instarg;\n"
            + "public final class Sum {\n"
            + "  public int add(int a, int b) {\n"
            + "    return a + b;\n"
            + "  }\n"
            + "  public static int all(Sum s, int[] a) {\n"
            + "    int sum = 0;\n"
            + "    for (int v : a) { sum = s.add(sum, v); };\n"
            + "    return sum;\n"
            + "  }\n"
            + "}\n"
        ).mimeType("text/java").name("Sum.java").build();
        engine.eval(src);

        Source js = Source.newBuilder(
                "var Sum = Interop.import('instarg.Sum');\n"
              + "var sum = new Sum();"
              + "Sum.all(sum, [3, 1, 3, 6]);"
        ).mimeType("text/javascript").name("thirteen.js").build();

        int value = engine.eval(js).as(Number.class).intValue();
        assertEquals(13, value);
    }

}
