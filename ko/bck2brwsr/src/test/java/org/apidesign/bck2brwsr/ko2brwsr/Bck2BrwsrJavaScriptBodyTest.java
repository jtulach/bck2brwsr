/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.ko2brwsr;

import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.netbeans.html.json.tck.JavaScriptTCK;
import org.netbeans.html.json.tck.KOTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class Bck2BrwsrJavaScriptBodyTest extends JavaScriptTCK {
    @Factory public static Object[] create() {
        return VMTest.newTests().
            withClasses(tests()).
            withClasses(Bck2BrwsrKnockoutImpl.createClasses()).
            withLaunchers("bck2brwsr").
            withTestAnnotation(KOTest.class).
            build();
    }
    
    static Class[] tests() {
        final Class<?>[] arr = testClasses();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].getSimpleName().startsWith("GC")) {
                arr[i] = Object.class;
            }
        }
        return arr;
    }
}
