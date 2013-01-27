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
package org.apidesign.bck2brwsr.emul.reflect;

import java.lang.annotation.Annotation;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class AnnotationImpl implements Annotation {
    public Class<? extends Annotation> annotationType() {
        return getClass();
    }

    @JavaScriptBody(args = { "a", "n", "values" }, body = ""
        + "function f(v, p) {\n"
        + "  var val = v;\n"
        + "  var prop = p;\n"
        + "  return function() {\n"
        + "    return val[prop];\n"
        + "  };\n"
        + "}\n"
        + "var props = Object.getOwnPropertyNames(values);\n"
        + "for (var i = 0; i < props.length; i++) {\n"
        + "  var p = props[i];\n"
        + "  a[p] = new f(values, p);\n"
        + "}\n"
        + "a['$instOf_' + n] = true;\n"
        + "return a;"
    )
    private static <T extends Annotation> T create(AnnotationImpl a, String n, Object values) {
        return null;
    }
    public static <T extends Annotation> T create(Class<T> annoClass, Object values) {
        return create(new AnnotationImpl(), annoClass.getName().replace('.', '_'), values);
    }

    public static Annotation[] create(Object anno) {
        String[] names = findNames(anno);
        Annotation[] ret = new Annotation[names.length];
        for (int i = 0; i < names.length; i++) {
            String n = names[i].substring(1, names[i].length() - 1).replace('/', '_');
            ret[i] = create(new AnnotationImpl(), n, findData(anno, names[i]));
        }
        return ret;
    }
    @JavaScriptBody(args = "anno", body =
          "var arr = new Array();"
        + "var props = Object.getOwnPropertyNames(anno);\n"
        + "for (var i = 0; i < props.length; i++) {\n"
        + "  var p = props[i];\n"
        + "  arr.push(p);"
        + "}"
        + "return arr;"
    )
    private static String[] findNames(Object anno) {
        throw new UnsupportedOperationException();
    }

    @JavaScriptBody(args={ "anno", "p"}, body="return anno[p];")
    private static Object findData(Object anno, String p) {
        throw new UnsupportedOperationException();
    }
}
