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
package org.apidesign.bck2brwsr.launcher.fximpl;

import java.io.BufferedReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;
import org.netbeans.html.boot.impl.FindResources;
import org.netbeans.html.boot.impl.FnUtils;
import org.netbeans.html.boot.spi.Fn;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class JVMBridge {
    static final Logger LOG = Logger.getLogger(JVMBridge.class.getName());

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
        Fn.activate(presenter);
        return Class.forName(name, true, cl);
    }

    private final class WebPresenter implements Fn.Presenter,
    FindResources, Fn.ToJavaScript, Fn.FromJavaScript, Executor, Fn.KeepAlive {
        private final Set<Object> keep = new HashSet<Object>();
        final void keep(Object obj) {
            keep.add(obj);
        }

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
            return defineJSFn(code, names, null);
        }

        @Override
        public Fn defineFn(String code, String[] names, boolean[] keepAlive) {
            return defineJSFn(code, names, keepAlive);
        }

        private JSFn defineJSFn(String code, String[] names, boolean[] keepAlive) {
            StringBuilder sb = new StringBuilder();
            sb.append("(function() {");
            sb.append("  return function(");
            String sep = "";
            if (names != null) for (String n : names) {
                sb.append(sep).append(n);
                sep = ",";
            }
            sb.append(") {\n");
            sb.append(code);
            sb.append("};");
            sb.append("})()");

            JSObject x = (JSObject) engine.executeScript(sb.toString());
            return new JSFn(this, x, keepAlive);
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

        @Override
        public Object toJava(Object js) {
            if (js instanceof Weak) {
                js = ((Weak)js).get();
            }
            return checkArray(js);
        }

        @Override
        public Object toJavaScript(Object toReturn) {
            if (toReturn instanceof Object[]) {
                return convertArrays((Object[]) toReturn);
            }
            return toReturn;
        }

        @Override
        public void execute(Runnable command) {
            if (Platform.isFxApplicationThread()) {
                command.run();
            } else {
                Platform.runLater(command);
            }
        }

        final JSObject convertArrays(Object[] arr) {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] instanceof Object[]) {
                    arr[i] = convertArrays((Object[]) arr[i]);
                }
            }
            final JSObject wrapArr = (JSObject) wrapArrFn().call("array", arr); // NOI18N
            return wrapArr;
        }

        private JSObject wrapArrImpl;

        private final JSObject wrapArrFn() {
            if (wrapArrImpl == null) {
                try {
                    wrapArrImpl = (JSObject) defineJSFn("  var k = {};"
                        + "  k.array= function() {"
                        + "    return Array.prototype.slice.call(arguments);"
                        + "  };"
                        + "  return k;", null, null
                    ).invokeImpl(null, false);
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return wrapArrImpl;
        }

        final Object checkArray(Object val) {
            if (val instanceof String && val == undefined()) {
                return null;
            }
            int length = ((Number) arraySizeFn().call("array", val, null)).intValue();
            if (length == -1) {
                return val;
            }
            Object[] arr = new Object[length];
            arraySizeFn().call("array", val, arr);
            for (int i = 0; i < arr.length; i++) {
                arr[i] = checkArray(arr[i]);
            }
            return arr;
        }
        private JSObject arraySize;

        private final JSObject arraySizeFn() {
            if (arraySize == null) {
                try {
                    arraySize = (JSObject) defineJSFn("  var k = {};"
                        + "  k.array = function(arr, to) {"
                        + "    if (to === null) {"
                        + "      if (Object.prototype.toString.call(arr) === '[object Array]') return arr.length;"
                        + "      else return -1;"
                        + "    } else {"
                        + "      var l = arr.length;"
                        + "      for (var i = 0; i < l; i++) to[i] = arr[i];"
                        + "      return l;"
                        + "    }"
                        + "  };"
                        + "  return k;", null, null
                    ).invokeImpl(null, false);
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return arraySize;
        }

        private Object undefined;

        private final Object undefined() {
            if (undefined == null) {
                try {
                    undefined = defineJSFn(""
                        + "  return undefined;", null, null
                    ).invokeImpl(null, false);
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
            return undefined;
        }

    }

    private static final class JSFn extends Fn {
        private final JSObject fn;
        private final boolean[] keepAlive;

        private JSFn(WebPresenter cl, JSObject fn, boolean[] keepAlive) {
            super(cl);
            this.fn = fn;
            this.keepAlive = keepAlive;
        }

        @Override
        public Object invoke(Object thiz, Object... args) throws Exception {
            return invokeImpl(thiz, true, args);
        }

        final Object invokeImpl(Object thiz, boolean arrayChecks, Object... args) throws Exception {
            try {
                List<Object> all = new ArrayList<Object>(args.length + 1);
                all.add(thiz == null ? fn : thiz);
                for (int i = 0; i < args.length; i++) {
                    Object conv = args[i];
                    if (arrayChecks) {
                        if (args[i] instanceof Object[]) {
                            Object[] arr = (Object[]) args[i];
                            conv = ((WebPresenter) presenter()).convertArrays(arr);
                        }
                        if (args[i] instanceof Character) {
                            conv = (int)(Character)args[i];
                        }
                        if (conv != null && keepAlive != null
                            && !keepAlive[i] && !isJSReady(conv)
                            && !conv.getClass().getSimpleName().equals("$JsCallbacks$") // NOI18N
                            ) {
                            conv = new Weak(conv);
                        } else {
                            if (!isJSReady(conv)) {
                                ((WebPresenter) presenter()).keep(conv);
                            }
                        }
                    }
                    all.add(conv);
                }
                Object ret = fn.call("call", all.toArray()); // NOI18N
                if (ret instanceof Weak) {
                    ret = ((Weak) ret).get();
                }
                if (ret == fn) {
                    return null;
                }
                if (!arrayChecks) {
                    return ret;
                }
                return ((WebPresenter) presenter()).checkArray(ret);
            } catch (Error t) {
                t.printStackTrace();
                throw t;
            } catch (Exception t) {
                t.printStackTrace();
                throw t;
            }
        }
    }

    private static boolean isJSReady(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return true;
        }
        if (obj instanceof Number) {
            return true;
        }
        if (obj instanceof JSObject) {
            return true;
        }
        if (obj instanceof Character) {
            return true;
        }
        return false;
    }

    private static final class Weak extends WeakReference<Object> {
        public Weak(Object referent) {
            super(referent);
            assert !(referent instanceof Weak);
        }
    } // end of Weak
}
