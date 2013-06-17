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
package org.apidesign.bck2brwsr.launcher.fximpl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;
import javafx.beans.value.ChangeListener;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class JVMBridge {
    private final WebEngine engine;
    private final WebClassLoader cl;
    
    private static ClassLoader[] ldrs;
    private static ChangeListener<Void> onBck2BrwsrLoad;
    
    JVMBridge(WebEngine eng) {
        this.engine = eng;
        this.cl = new WebClassLoader(JVMBridge.class.getClassLoader().getParent());
    }
        
    public static void registerClassLoaders(ClassLoader[] loaders) {
        ldrs = loaders.clone();
    }
    
    public static void addBck2BrwsrLoad(ChangeListener<Void> l) throws TooManyListenersException {
        if (onBck2BrwsrLoad != null) {
            throw new TooManyListenersException();
        }
        onBck2BrwsrLoad = l;
    }

    public static void onBck2BrwsrLoad() {
        ChangeListener<Void> l = onBck2BrwsrLoad;
        if (l != null) {
            l.changed(null, null, null);
        }
    }
    
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return Class.forName(name, true, cl);
    }
    
    private final class WebClassLoader extends JsClassLoader {
        public WebClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected URL findResource(String name) {
            if (ldrs != null) for (ClassLoader l : ldrs) {
                URL u = l.getResource(name);
                if (u != null) {
                    return u;
                }
            }
            return null;
        }
        
        @Override
        protected Enumeration<URL> findResources(String name) {
            List<URL> arr = new ArrayList<URL>();
            if (ldrs != null) {
                for (ClassLoader l : ldrs) {
                    URL u = l.getResource(name);
                    if (u != null) {
                        arr.add(u);
                    }
                }
            }
            return Collections.enumeration(arr);
        }

        @Override
        protected Fn defineFn(String code, String... names) {
            StringBuilder sb = new StringBuilder();
            sb.append("(function() {");
            sb.append("  var x = {};");
            sb.append("  x.fn = function(");
            String sep = "";
            for (String n : names) {
                sb.append(sep).append(n);
                sep = ",";
            }
            sb.append(") {\n");
            sb.append(code);
            sb.append("};");
            sb.append("return x;");
            sb.append("})()");
            
            JSObject x = (JSObject) engine.executeScript(sb.toString());
            return new JSFn(x);
        }
    }
    
    private static final class JSFn extends Fn {
        private final JSObject fn;

        public JSFn(JSObject fn) {
            this.fn = fn;
        }
        
        @Override
        public Object invoke(Object... args) throws Exception {
            try {
                return fn.call("fn", args); // NOI18N
            } catch (Error t) {
                t.printStackTrace();
                throw t;
            } catch (Exception t) {
                t.printStackTrace();
                throw t;
            }
        }
    }
}
