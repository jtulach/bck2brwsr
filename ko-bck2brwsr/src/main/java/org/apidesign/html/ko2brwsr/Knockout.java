/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */
package org.apidesign.html.ko2brwsr;

import java.lang.reflect.Method;
import java.util.List;
import org.apidesign.bck2brwsr.core.ExtraJavaScript;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/** Provides binding between models and 
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ExtraJavaScript(resource = "/org/apidesign/bck2brwsr/htmlpage/knockout-2.2.1.js")
public class Knockout {
    /** used by tests */
    static Knockout next;
    private final Object model;

    Knockout(Object model) {
        this.model = model == null ? this : model;
    }
    
    public static <M> Knockout applyBindings(
        Object model, String[] propsGettersAndSetters,
        String[] methodsAndSignatures
    ) {
        applyImpl(propsGettersAndSetters, model.getClass(), model, model, methodsAndSignatures);
        return new Knockout(model);
    }
    public static <M> Knockout applyBindings(
        Class<M> modelClass, M model, String[] propsGettersAndSetters,
        String[] methodsAndSignatures
    ) {
        Knockout bindings = next;
        next = null;
        if (bindings == null) {
            bindings = new Knockout(null);
        }
        applyImpl(propsGettersAndSetters, modelClass, bindings, model, methodsAndSignatures);
        applyBindings(bindings);
        return bindings;
    }

    public void valueHasMutated(String prop) {
        valueHasMutated(model, prop);
    }
    @JavaScriptBody(args = { "self", "prop" }, body =
        "var p = self[prop]; if (p) p.valueHasMutated();"
    )
    public static void valueHasMutated(Object self, String prop) {
    }
    

    @JavaScriptBody(args = { "id", "ev" }, body = "ko.utils.triggerEvent(window.document.getElementById(id), ev.substring(2));")
    public static void triggerEvent(String id, String ev) {
    }
    
    @JavaScriptBody(args = { "bindings", "model", "prop", "getter", "setter", "primitive", "array" }, body =
          "var bnd = {\n"
        + "  'read': function() {\n"
        + "    var v = model[getter]();\n"
        + "    if (array) v = v.koArray();\n"
        + "    return v;\n"
        + "  },\n"
        + "  'owner': bindings\n"
        + "};\n"
        + "if (setter != null) {\n"
        + "  bnd['write'] = function(val) {\n"
        + "    model[setter](primitive ? new Number(val) : val);\n"
        + "  };\n"
        + "}\n"
        + "bindings[prop] = ko['computed'](bnd);"
    )
    static void bind(
        Object bindings, Object model, String prop, String getter, String setter, boolean primitive, boolean array
    ) {
    }

    @JavaScriptBody(args = { "bindings", "model", "prop", "sig" }, body = 
        "bindings[prop] = function(data, ev) { model[sig](data, ev); };"
    )
    static void expose(
        Object bindings, Object model, String prop, String sig
    ) {
    }
    
    @JavaScriptBody(args = { "bindings" }, body = "ko.applyBindings(bindings);")
    static void applyBindings(Object bindings) {}
    
    private static void applyImpl(
        String[] propsGettersAndSetters,
        Class<?> modelClass,
        Object bindings,
        Object model,
        String[] methodsAndSignatures
    ) throws IllegalStateException, SecurityException {
        for (int i = 0; i < propsGettersAndSetters.length; i += 4) {
            try {
                Method getter = modelClass.getMethod(propsGettersAndSetters[i + 3]);
                bind(bindings, model, propsGettersAndSetters[i],
                    propsGettersAndSetters[i + 1],
                    propsGettersAndSetters[i + 2],
                    getter.getReturnType().isPrimitive(),
                    List.class.isAssignableFrom(getter.getReturnType()));
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException(ex.getMessage());
            }
        }
        for (int i = 0; i < methodsAndSignatures.length; i += 2) {
            expose(
                bindings, model, methodsAndSignatures[i], methodsAndSignatures[i + 1]);
        }
    }
}
