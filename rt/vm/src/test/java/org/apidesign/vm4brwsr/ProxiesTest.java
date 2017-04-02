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
package org.apidesign.vm4brwsr;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ProxiesTest {
    @Test public void runViaProxy() throws Exception {
        assertExec("Runnable proxy", Proxies.class, "runViaProxy__Ljava_lang_String_2Z",
            "run", true
        );
    }

    @Test public void countViaProxy() throws Exception {
        assertExec("Primitive type proxy", Proxies.class, "countViaProxy__IZ",
            42, true
        );
    }

    @Test public void runViaProxyWithClass() throws Exception {
        assertExec("Runnable proxy", Proxies.class, "runViaProxy__Ljava_lang_String_2Z",
            "run", false
        );
    }

    @Test public void countViaProxyWithClass() throws Exception {
        assertExec("Primitive type proxy", Proxies.class, "countViaProxy__IZ",
            42, false
        );
    }

    @Test public void countViaProxies() throws Exception {
        assertExec("Two proxy interfaces", Proxies.class, "countViaProxies__I",
            42
        );
    }

    private static TestVM code;

    @BeforeClass
    public static void compileTheCode() throws Exception {
        code = TestVM.compileClass("org/apidesign/vm4brwsr/Proxies");
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }

    private static void assertExec(
        String msg, Class<?> clazz, String method, Object expRes, Object... args) throws Exception
    {
        code.assertExec(msg, clazz, method, expRes, args);
    }

}
