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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach
 */
public final class AnnotationImpl implements Annotation {
    private final Class<? extends Annotation> type;

    public AnnotationImpl(Class<? extends Annotation> type) {
        this.type = type;
    }
    
    public Class<? extends Annotation> annotationType() {
        return type;
    }

    @JavaScriptBody(args = { "a", "n", "arr", "values" }, body = ""
        + "function r(anno, val, prop, m) {\n"
        + "  var v = val[prop];\n"
        + "  if (typeof v === 'undefined') {\n"
        + "    var cls = anno['fld_org_apidesign_bck2brwsr_emul_reflect_AnnotationImpl_type'].cnstr;\n"
        + "    v = cls.prototype[m]();\n"
        + "  }\n"
        + "  return v;\n"
        + "}\n"
        + "function f(val, prop, clazz, m) {\n"
        + "  return function() {\n"
        + "    if (clazz == null) return r(this, val, prop, m);\n"
        + "    if (clazz.isArray__Z()) {\n"
        + "      var valarr = r(this, val, prop, m);\n"
        + "      var cmp = clazz.getComponentType__Ljava_lang_Class_2();\n"
        + "      var retarr = vm.java_lang_reflect_Array(false).newInstance__Ljava_lang_Object_2Ljava_lang_Class_2I(cmp, valarr.length);\n"
        + "      for (var i = 0; i < valarr.length; i++) {\n"
        + "        retarr[i] = CLS.prototype.c__Ljava_lang_Object_2Ljava_lang_Class_2Ljava_lang_Object_2(cmp, valarr[i]);\n"
        + "      }\n"
        + "      return retarr;\n"
        + "    }\n"
        + "    return CLS.prototype.c__Ljava_lang_Object_2Ljava_lang_Class_2Ljava_lang_Object_2(clazz, r(this, val, prop, m));\n"
        + "  };\n"
        + "}\n"
        + "for (var i = 0; i < arr.length; i += 3) {\n"
        + "  var m = arr[i];\n"
        + "  var p = arr[i + 1];\n"
        + "  var c = arr[i + 2];\n"
        + "  a[m] = f(values, p, c, m);\n"
        + "}\n"
        + "a['$instOf_' + n] = true;\n"
        + "return a;"
    )
    private static native <T extends Annotation> T create(
        AnnotationImpl a, String n, Object[] methodsAndProps, Object values
    );
    
    private static Object c(Class<? extends Annotation> a, Object v) {
        return create(a, v);
    }
    
    public static <T extends Annotation> T create(Class<T> annoClass, Object values) {
        return create(new AnnotationImpl(annoClass), 
            annoClass.getName().replace('.', '_'), 
            findProps(annoClass), values
        );
    }

    public static Annotation[] create(Object anno) {
        String[] names = findNames(anno);
        Annotation[] ret = new Annotation[names.length];
        for (int i = 0; i < names.length; i++) {
            String annoNameSlash = names[i].substring(1, names[i].length() - 1);
            Class<? extends Annotation> annoClass;
            try {
                annoClass = (Class<? extends Annotation>)Class.forName(annoNameSlash.replace('/', '.'));
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException("Can't find annotation class " + annoNameSlash);
            }
            ret[i] = create(
                new AnnotationImpl(annoClass), 
                annoNameSlash.replace('/', '_'),
                findProps(annoClass),
                findData(anno, names[i])
            );
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
    private static native String[] findNames(Object anno);

    @JavaScriptBody(args={ "anno", "p"}, body="return anno[p];")
    private static native Object findData(Object anno, String p);

    private static Object[] findProps(Class<?> annoClass) {
        final Method[] marr = MethodImpl.findMethods(annoClass, Modifier.PUBLIC);
        Object[] arr = new Object[marr.length * 3];
        int pos = 0;
        for (Method m : marr) {
            arr[pos++] = MethodImpl.toSignature(m);
            arr[pos++] = m.getName();
            final Class<?> rt = m.getReturnType();
            if (rt.isArray()) {
                arr[pos++] = rt.getComponentType().isAnnotation() ? rt : null;
            } else {
                arr[pos++] = rt.isAnnotation() ? rt : null;
            }
        }
        return arr;
    }
}
