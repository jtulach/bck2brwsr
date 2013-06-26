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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.html.js.JavaScriptBody;
import net.java.html.json.Model;
import netscape.javascript.JSObject;
import org.apidesign.html.json.spi.FunctionBinding;
import org.apidesign.html.json.spi.PropertyBinding;

/** This is an implementation package - just
 * include its JAR on classpath and use official {@link Context} API
 * to access the functionality.
 * <p>
 * Provides binding between {@link Model models} and knockout.js running
 * inside a JavaFX WebView. 
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Knockout {
    private static final Logger LOG = Logger.getLogger(Knockout.class.getName());
    /** used by tests */
    static Knockout next;
    private final Object model;

    Knockout(Object model) {
        this.model = model == null ? this : model;
    }
    
    public Object koData() {
        return model;
    }

    static Object toArray(Object[] arr) {
        return InvokeJS.KObject.call("array", arr);
    }
    
    private static int cnt;
    public static <M> Knockout createBinding(Object model) {
        Object bindings = InvokeJS.create(model, ++cnt);
        return new Knockout(bindings);
    }

    public void valueHasMutated(String prop) {
        valueHasMutated((JSObject) model, prop);
    }
    public static void valueHasMutated(JSObject model, String prop) {
        LOG.log(Level.FINE, "property mutated: {0}", prop);
        try {
            Object koProp = model.getMember(prop);
            if (koProp instanceof JSObject) {
                ((JSObject)koProp).call("valueHasMutated");
            }
        } catch (Throwable t) {
            LOG.log(Level.WARNING, "valueHasMutated failed for " + model + " prop: " + prop, t);
        }
    }

    static void bind(
        Object bindings, Object model, PropertyBinding pb, boolean primitive, boolean array
    ) {
        final String prop = pb.getPropertyName();
        try {
            InvokeJS.bind(bindings, pb, prop, "getValue", pb.isReadOnly() ? null : "setValue", primitive, array);
            
            ((JSObject)bindings).setMember("ko-fx.model", model);
            LOG.log(Level.FINE, "binding defined for {0}: {1}", new Object[]{prop, ((JSObject)bindings).getMember(prop)});
        } catch (Throwable ex) {
            LOG.log(Level.WARNING, "binding failed for {0} on {1}", new Object[]{prop, bindings});
        }
    }
    static void expose(Object bindings, FunctionBinding f) {
        final String prop = f.getFunctionName();
        try {
            InvokeJS.expose(bindings, f, prop, "call");
        } catch (Throwable ex) {
            LOG.log(Level.SEVERE, "Cannot define binding for " + prop + " in model " + f, ex);
        }
    }
    
    static void applyBindings(Object bindings) {
        InvokeJS.applyBindings(bindings);
    }
    
    private static final class InvokeJS {
        static final JSObject KObject;

        static {
            final InputStream koScript = Knockout.class.getResourceAsStream("knockout-2.2.1.js");
            assert koScript != null : "Can't load knockout.js";
            BufferedReader r = new BufferedReader(new InputStreamReader(koScript));
            StringBuilder sb = new StringBuilder();
            for (;;) {
                try {
                    String l = r.readLine();
                    if (l == null) {
                        break;
                    }
                    sb.append(l).append('\n');
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
            exec(sb.toString());
            Object ko = exec("ko");
            assert ko != null : "Knockout library successfully defined 'ko'";

            Console.register();
            KObject = (JSObject) kObj();
        }
        
        @JavaScriptBody(args = { "s" }, body = "return eval(s);")
        private static native Object exec(String s);
        
        @JavaScriptBody(args = {}, body =
                  "  var k = {};"
                + "  k.array= function() {"
                + "    return Array.prototype.slice.call(arguments);"
                + "  };"
                + "  return k;"
        )
        private static native Object kObj();
        
        @JavaScriptBody(args = { "value", "cnt " }, body =
                  "    var ret = {};"
                + "    ret.toString = function() { return 'KObject' + cnt + ' value: ' + value + ' props: ' + Object.keys(this); };"
                + "    return ret;"
        )
        static native Object create(Object value, int cnt);
        
        @JavaScriptBody(args = { "bindings", "model", "prop", "sig" }, body = 
                "    bindings[prop] = function(data, ev) {"
              //            + "         console.log(\"  callback on prop: \" + prop);"
              + "      model[sig](data, ev);"
              + "    };"
        )
        static native Object expose(Object bindings, Object model, String prop, String sig);

        
        @JavaScriptBody(args = { "bindings", "model", "prop", "getter", "setter", "primitive", "array" }, body = 
                  "    var bnd = {"
                + "      read: function() {"
                + "      try {"
                + "        var v = model[getter]();"
        //        + "      console.log(\" getter value \" + v + \" for property \" + prop);"
        //        + "      try { v = v.koData(); } catch (ignore) {"
        //        + "        console.log(\"Cannot convert to koData: \" + ignore);"
        //        + "      };"
        //        + "      console.log(\" getter ret value \" + v);"
        //        + "      for (var pn in v) {"
        //        + "         console.log(\"  prop: \" + pn + \" + in + \" + v + \" = \" + v[pn]);"
        //        + "         if (typeof v[pn] == \"function\") console.log(\"  its function value:\" + v[pn]());"
        //        + "      }"
        //        + "      console.log(\" all props printed for \" + (typeof v));"
                + "        return v;"
                + "      } catch (e) {"
                + "        alert(\"Cannot call \" + getter + \" on \" + model + \" error: \" + e);"
                + "      }"
                + "    },"
                + "    owner: bindings"
        //        + "  ,deferEvaluation: true"
                + "    };"
                + "    if (setter != null) {"
                + "      bnd.write = function(val) {"
                + "        model[setter](primitive ? new Number(val) : val);"
                + "      };"
                + "    };"
                + "    bindings[prop] = ko.computed(bnd);"
        )
        static native void bind(Object binding, Object model, String prop, String getter, String setter, boolean primitive, boolean array);

        @JavaScriptBody(args = { "bindings" }, body = "ko.applyBindings(bindings);")
        private static native void applyBindings(Object bindings);
    }
}
