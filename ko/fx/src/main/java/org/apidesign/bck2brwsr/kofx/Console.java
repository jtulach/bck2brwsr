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
package org.apidesign.bck2brwsr.kofx;

import java.util.logging.Logger;
import net.java.html.js.JavaScriptBody;

/** This is an implementation package - just
 * include its JAR on classpath and use official {@link Context} API
 * to access the functionality.
 * <p>
 * Redirects JavaScript's messages to Java's {@link Logger}.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Console {
    private static final Logger LOG = Logger.getLogger(Console.class.getName());
    
    private Console() {
    }

    static void register() {
        registerImpl(new Console());
    }
    
    @JavaScriptBody(args = { "jconsole" }, body = 
        "console.log = function(m) { jconsole.log(m); };" +
        "console.info = function(m) { jconsole.log(m); };" +
        "console.error = function(m) { jconsole.log(m); };" +
        "console.warn = function(m) { jconsole.log(m); };"
    )
    private static native void registerImpl(Console c);
    
    public void log(String msg) {
        LOG.info(msg);
    }
}
