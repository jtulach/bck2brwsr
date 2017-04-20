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
package org.apidesign.truffle;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import java.net.URL;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;

public class Bck2BrwsrLanguageTest {

    private static PolyglotEngine engine;

    public Bck2BrwsrLanguageTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        engine = PolyglotEngine.newBuilder().build();
    }

    @AfterClass
    public static void tearDownClass() {
        engine.dispose();
    }

    @Test
    public void testHelloWorld() throws Exception {
        URL u = Bck2BrwsrLanguageTest.class.getResource("Hello.class");
        assertNotNull("Hello.class found", u);
        Source src = Source.newBuilder(u).mimeType("application/x-java-class").build();
        engine.eval(src);
    }
}
