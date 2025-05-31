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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import org.apidesign.bck2brwsr.core.Exported;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach
 */
public final class ProxiesImpl extends Proxy {
    public ProxiesImpl(InvocationHandler h) {
        super(h);
    }

    @Exported
    final Object proxyTo(Method m, Object[] args) throws Throwable {
        Class<?>[] types = m.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (types[i].isPrimitive()) {
                args[i] = MethodImpl.fromPrimitive(types[i], args[i]);
            }
        }
        return h.invoke(this, m, args);
    }


    @JavaScriptBody(args = { "a", "n", "arr" }, body = ""
        + "var pa = a.constructor.prototype;\n"
        + "function f(method) {\n"
        + "  return function() {\n"
        + "    return this['proxyTo__Ljava_lang_Object_2Ljava_lang_reflect_Method_2_3Ljava_lang_Object_2'](method, arguments);\n"
        + "  };\n"
        + "}\n"
        + "for (var i = 0; i < arr.length; i += 3) {\n"
        + "  var m = arr[i];\n"
        + "  var method = arr[i + 1];\n"
        + "  pa[m] = f(method);\n"
        + "}\n"
        + "pa['$instOf_' + n.replace__Ljava_lang_String_2CC('.', '_')] = true;\n"
    )
    private static native void implement(
        ProxiesImpl a, String n, Object[] methodsAndProps
    );

    @JavaScriptBody(args = {}, body = ""
        + "CLS.$class = null;\n"
    )
    private static native void resetClass();

    public static Proxy create(Class[] classes, InvocationHandler h) {
        resetClass();
        ProxiesImpl impl = new ProxiesImpl(h);
        implementMethods(classes, impl);
        return impl;
    }

    private static void implementMethods(Class[] classes, ProxiesImpl impl) {
        if (classes == null) {
            return;
        }
        for (Class c: classes) {
            Object[] info = findProps(c);
            implement(impl, c.getCanonicalName(), info);
            implementMethods(c.getInterfaces(), impl);
        }
    }

    private static Object[] findProps(Class<?> annoClass) {
        final Method[] marr = MethodImpl.findMethods(annoClass, Modifier.PUBLIC, Modifier.FINAL);
        Object[] arr = new Object[marr.length * 3];
        int pos = 0;
        for (Method m : marr) {
            arr[pos++] = MethodImpl.toSignature(m);
            arr[pos++] = m;
            arr[pos++] = m.getName();
        }
        return arr;
    }
}
