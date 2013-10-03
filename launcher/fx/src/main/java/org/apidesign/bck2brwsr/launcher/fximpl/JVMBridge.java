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

import java.io.BufferedReader;
import java.io.Reader;
import org.apidesign.html.boot.spi.Fn;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TooManyListenersException;
import javafx.beans.value.ChangeListener;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import org.apidesign.html.boot.impl.FindResources;
import org.apidesign.html.boot.impl.FnUtils;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class JVMBridge {
    private final WebEngine engine;
    private final ClassLoader cl;
    private final WebPresenter presenter;
    
    private static ClassLoader[] ldrs;
    private static ChangeListener<Void> onBck2BrwsrLoad;
    
    JVMBridge(WebEngine eng) {
        this.engine = eng;
        final ClassLoader p = JVMBridge.class.getClassLoader().getParent();
        this.presenter = new WebPresenter();
        this.cl = FnUtils.newLoader(presenter, presenter, p);
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
        FnUtils.currentPresenter(presenter);
        return Class.forName(name, true, cl);
    }
    
    private final class WebPresenter implements FindResources, Fn.Presenter {
        @Override
        public void findResources(String name, Collection<? super URL> results, boolean oneIsEnough) {
            if (ldrs != null) for (ClassLoader l : ldrs) {
                URL u = l.getResource(name);
                if (u != null) {
                    results.add(u);
                }
            }
        }

        @Override
        public Fn defineFn(String code, String... names) {
            StringBuilder sb = new StringBuilder();
            sb.append("(function() {");
            sb.append("  return function(");
            String sep = "";
            for (String n : names) {
                sb.append(sep).append(n);
                sep = ",";
            }
            sb.append(") {\n");
            sb.append(code);
            sb.append("};");
            sb.append("})()");
            
            JSObject x = (JSObject) engine.executeScript(sb.toString());
            return new JSFn(this, x);
        }

        @Override
        public void displayPage(URL page, Runnable onPageLoad) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void loadScript(Reader code) throws Exception {
            BufferedReader r = new BufferedReader(code);
            StringBuilder sb = new StringBuilder();
            for (;;) {
                String l = r.readLine();
                if (l == null) {
                    break;
                }
                sb.append(l).append('\n');
            }
            engine.executeScript(sb.toString());
        }
    }
    
    private static final class JSFn extends Fn {
        private final JSObject fn;

        private JSFn(WebPresenter cl, JSObject fn) {
            super(cl);
            this.fn = fn;
        }

        @Override
        public Object invoke(Object thiz, Object... args) throws Exception {
            try {
                List<Object> all = new ArrayList<Object>(args.length + 1);
                all.add(thiz == null ? fn : thiz);
                all.addAll(Arrays.asList(args));
                Object ret = fn.call("call", all.toArray()); // NOI18N
                return ret == fn ? null : ret;
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