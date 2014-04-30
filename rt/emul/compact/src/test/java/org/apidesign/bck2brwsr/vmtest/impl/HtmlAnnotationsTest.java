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
package org.apidesign.bck2brwsr.vmtest.impl;

import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/** Verify cooperation with net.java.html.js annotations.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class HtmlAnnotationsTest {
    @BrwsrTest public void fourtyTwo() throws Exception {
        assertEquals(HtmlAnnotations.fourtyTwo(), 42);
    }
    
    @BrwsrTest public void externalMul() throws Exception {
        assertEquals(HtmlAnnotations.useExternalMul(7, 6), 42);
    }

    @BrwsrTest public void callRunnableFromJS() throws Exception {
        assertEquals(HtmlAnnotations.callback(), 1);
    }

    @BrwsrTest public void callStaticMethodFromJS() throws Exception {
        assertEquals(HtmlAnnotations.staticCallback(), 1);
    }

    @BrwsrTest public void callbackWithFourParamsAndReturnType() throws Exception {
        Object instance = HtmlAnnotations.create();
        assertNotNull(instance, "Instance created");
        assertEquals(HtmlAnnotations.first(instance, 42, 31), 42);
    }

    @BrwsrTest public void callbackWithObjectParamsAndReturnType() throws Exception {
        Object instance = HtmlAnnotations.create();
        assertNotNull(instance, "Instance created");
        assertEquals(HtmlAnnotations.onError(instance, 42.0), 42.0);
    }
    
    private static void assertEquals(double real, double exp) {
        if (real - exp < 0.01) {
            return;
        }
        assert false : "Expecting " + exp + " but was " + real;
    }

    private static void assertNotNull(Object obj, String msg) {
        assert obj != null : msg;
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(HtmlAnnotationsTest.class);
    }
}
