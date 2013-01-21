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
package org.apidesign.bck2brwsr.htmlpage;

import org.apidesign.bck2brwsr.core.ExtraJavaScript;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/** Provides binding between models and 
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ExtraJavaScript(resource = "org/apidesign/bck2brwsr/htmlpage/knockout-2.2.1.js")
public class Knockout {
    /** used by tests */
    static Knockout next;
    
    Knockout() {
    }
    
    public static <M> Knockout applyBindings(
        Class<M> modelClass, M model, String[] propsGettersAndSetters
    ) {
        Knockout bindings = next;
        next = null;
        if (bindings == null) {
            bindings = new Knockout();
        }
        for (int i = 0; i < propsGettersAndSetters.length; i += 3) {
            bind(bindings, model, propsGettersAndSetters[i],
                propsGettersAndSetters[i + 1],
                propsGettersAndSetters[i + 2]
            );
        }
        applyBindings(bindings);
        return bindings;
    }

    @JavaScriptBody(args = { "prop" }, body =
        "this[prop].valueHasMutated();"
    )
    public void valueHasMutated(String prop) {
    }
    
    @JavaScriptBody(args = { "bindings", "model", "prop", "getter", "setter" }, body =
          "var bnd = {\n"
        + "  read: function() {\n"
        + "    var v = model[getter]();\n"
        + "    return v;\n"
        + "  },\n"
        + "  owner: bindings\n"
        + "};\n"
        + "if (setter != null) {\n"
        + "  bnd.write = function(val) {\n"
        + "    model[setter](new Number(val));\n"
        + "  };\n"
        + "}\n"
        + "bindings[prop] = ko.computed(bnd);"
    )
    private static void bind(
        Object bindings, Object model, String prop, String getter, String setter
    ) {
    }
    
    @JavaScriptBody(args = { "bindings" }, body = "ko.applyBindings(bindings);")
    private static void applyBindings(Object bindings) {}
}
