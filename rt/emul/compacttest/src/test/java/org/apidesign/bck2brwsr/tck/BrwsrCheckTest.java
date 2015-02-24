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
package org.apidesign.bck2brwsr.tck;

import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.HtmlFragment;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class BrwsrCheckTest {

    @BrwsrTest public void assertWindowObjectIsDefined() {
        assert window() != null : "No window object found!";
    }

    
    
    
    @HtmlFragment("<h1 id='hello'>\n"
        + "Hello!\n"
        + "</h1>\n")
    @BrwsrTest public void accessProvidedFragment() {
        assert getElementById("hello") != null : "Element with 'hello' ID found";
    }
    
    @Factory
    public static Object[] create() {
        return VMTest.create(BrwsrCheckTest.class);
    }
    

    @JavaScriptBody(args = {}, body = "return window;")
    private static native Object window();

    @JavaScriptBody(args = { "id" }, body = "return window.document.getElementById(id);")
    private static native Object getElementById(String id);
}
