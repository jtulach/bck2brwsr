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
package org.apidesign.vm4brwsr.api;

import java.io.IOException;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/** Utility methods to talk to the Bck2Brwsr virtual machine.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @since 0.9
 */
public final class VM {
    private VM() {
    }

    /** Takes an existing class and replaces its existing byte code 
     * with new one.
     * 
     * @param clazz existing class to reload
     * @param byteCode new bytecode
     * @throws IOException an exception is something goes wrong
     */
    public static void reload(Class<?> clazz, byte[] byteCode) throws IOException {
        reloadImpl(clazz.getName(), byteCode);
    }
    
    @JavaScriptBody(args = { "name", "byteCode" }, body = 
        "var r = vm._reload;"
      + "if (!r) r = exports._reload;"
      + "r(name, byteCode);"
    )
    private static void reloadImpl(String name, byte[] byteCode) throws IOException {
        throw new IOException();
    }
}
