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
package org.apidesign.bck2brwsr.compact.tck;

import java.io.IOException;
import java.util.ServiceLoader;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.openide.util.lookup.ServiceProvider;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ServiceLoaderTest {
    @Compare//(scripting = false) 
    public Object findsIOException() {
//      delayStart();
        for (IOException e : ServiceLoader.load(IOException.class)) {
            return "Found service: " + e.getClass().getName();
        }
        return null;
    }
/*    
    @org.apidesign.bck2brwsr.core.JavaScriptBody(args = { "a" }, body = "alert(a);")
    private static void alert(String a) {
    }
    private void delayStart() {
        for (int i = 0; i < 10; i++) {
            alert("State: " + i);
            for (int j = 0; j < 493208409; j++) ;
        }
    }
*/
    
    @Factory
    public static Object[] create() {
        return VMTest.create(ServiceLoaderTest.class);
    }

    
    @ServiceProvider(service = IOException.class)
    public static class MyException extends IOException {
    }
}
