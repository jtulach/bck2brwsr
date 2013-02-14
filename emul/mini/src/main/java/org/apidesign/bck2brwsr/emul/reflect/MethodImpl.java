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

import java.lang.reflect.Method;
import java.util.Enumeration;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/** Utilities to work on methods.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class MethodImpl {
    public static MethodImpl INSTANCE;
    static {
        try {
            Class.forName(Method.class.getName());
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    protected abstract Method create(Class<?> declaringClass, String name, Object data, String sig);
    
    
    //
    // bck2brwsr implementation
    //

    @JavaScriptBody(args = {"clazz", "prefix"},
        body = ""
        + "var c = clazz.cnstr.prototype;"
        + "var arr = new Array();\n"
        + "for (m in c) {\n"
        + "  if (m.indexOf(prefix) === 0) {\n"
        + "     arr.push(m);\n"
        + "     arr.push(c[m]);\n"
        + "  }"
        + "}\n"
        + "return arr;")
    private static native Object[] findMethodData(
        Class<?> clazz, String prefix);

    public static Method findMethod(
        Class<?> clazz, String name, Class<?>... parameterTypes) {
        Object[] data = findMethodData(clazz, name + "__");
        BIG: for (int i = 0; i < data.length; i += 2) {
            String sig = ((String) data[0]).substring(name.length() + 2);
            Method tmp = INSTANCE.create(clazz, name, data[1], sig);
            Class<?>[] tmpParms = tmp.getParameterTypes();
            if (parameterTypes.length != tmpParms.length) {
                continue;
            }
            for (int j = 0; j < tmpParms.length; j++) {
                if (!parameterTypes[j].equals(tmpParms[j])) {
                    continue BIG;
                }
            }
            return tmp;
        }
        return null;
    }

    public static Method[] findMethods(Class<?> clazz, int mask) {
        Object[] namesAndData = findMethodData(clazz, "");
        int cnt = 0;
        for (int i = 0; i < namesAndData.length; i += 2) {
            String sig = (String) namesAndData[i];
            Object data = namesAndData[i + 1];
            int middle = sig.indexOf("__");
            if (middle == -1) {
                continue;
            }
            String name = sig.substring(0, middle);
            sig = sig.substring(middle + 2);
            final Method m = INSTANCE.create(clazz, name, data, sig);
            if ((m.getModifiers() & mask) == 0) {
                continue;
            }
            namesAndData[cnt++] = m;
        }
        Method[] arr = new Method[cnt];
        for (int i = 0; i < cnt; i++) {
            arr[i] = (Method) namesAndData[i];
        }
        return arr;
    }

    public static int signatureElements(String sig) {
        Enumeration<Class> en = signatureParser(sig);
        int cnt = 0;
        while (en.hasMoreElements()) {
            en.nextElement();
            cnt++;
        }
        return cnt;
    }
    
    public static Enumeration<Class> signatureParser(final String sig) {
        class E implements Enumeration<Class> {
            int pos;
            
            public boolean hasMoreElements() {
                return pos < sig.length();
            }

            public Class nextElement() {
                switch (sig.charAt(pos++)) {
                    case 'I':
                        return Integer.TYPE;
                    case 'J':
                        return Long.TYPE;
                    case 'D':
                        return Double.TYPE;
                    case 'F':
                        return Float.TYPE;
                    case 'B':
                        return Byte.TYPE;
                    case 'Z':
                        return Boolean.TYPE;
                    case 'S':
                        return Short.TYPE;
                    case 'V':
                        return Void.TYPE;
                    case 'C':
                        return Character.TYPE;
                    case 'L':
                        try {
                            int up = sig.indexOf("_2");
                            String type = sig.substring(1, up);
                            pos = up + 2;
                            return Class.forName(type);
                        } catch (ClassNotFoundException ex) {
                            // should not happen
                        }
                }
                throw new UnsupportedOperationException(sig + " at " + pos);
            }
        }
        return new E();
    }
}
