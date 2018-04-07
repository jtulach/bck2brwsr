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

import java.io.IOException;
import java.io.InputStream;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NoStringCnstntsTest {
    private static String code;

    
    @Test public void dontGeneratePrimitiveFinalConstants() {
        assertEquals(code.indexOf("HELLO"), -1, "MISSING_CONSTANT field should not be generated");
    }
    
    @BeforeClass 
    public static void compileTheCode() throws Exception {
        final String res = "org/apidesign/vm4brwsr/StringSample";
        StringBuilder sb = new StringBuilder();
        class JustStaticMethod implements Bck2Brwsr.Resources {
            @Override
            public InputStream get(String resource) throws IOException {
                final String cn = res + ".class";
                if (resource.equals(cn)) {
                    return getClass().getClassLoader().getResourceAsStream(cn);
                }
                return null;
            }
        }
        Bck2Brwsr.generate(sb, new JustStaticMethod(), res);
        code = sb.toString();
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }
    
}
