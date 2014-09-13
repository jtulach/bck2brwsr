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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Enumeration;
import org.apidesign.bck2brwsr.core.Exported;
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
    protected abstract Constructor create(Class<?> declaringClass, Object data, String sig);
    
    
    //
    // bck2brwsr implementation
    //

    @JavaScriptBody(args = {"clazz", "prefix", "cnstr"},
        body = ""
        + "var c = clazz.cnstr;\n"
        + "if (!cnstr) c = c.prototype;\n"
        + "var arr = new Array();\n"
        + "function check(m, verify) {\n"
        + "  if (m.indexOf(prefix) === 0) {\n"
        + "     if (!c[m] || !c[m].cls) return;\n"
        + "     if (verify) {\n"
        + "       for (var i = 0; i < arr.length; i += 3) {\n"
        + "         if (arr[i] === m) return;\n"
        + "       }\n"
        + "     }\n"
        + "     arr.push(m);\n"
        + "     arr.push(c[m]);\n"
        + "     arr.push(c[m].cls.$class);\n"
        + "  }\n"
        + "}\n"
        + "for (m in c) {\n"
        + "  check(m)\n"
        + "}\n"
        + "check('wait__V', true);\n"
        + "check('wait__VJ', true);\n"
        + "check('wait__VJI', true);\n"
        + "check('equals__ZLjava_lang_Object_2', true);\n"
        + "check('toString__Ljava_lang_String_2', true);\n"
        + "check('hashCode__I', true);\n"
        + "check('getClass__Ljava_lang_Class_2', true);\n"
        + "check('notify__V', true);\n"
        + "check('notifyAll__V', true);\n"
        + "return arr;\n")
    private static native Object[] findMethodData(
        Class<?> clazz, String prefix, boolean cnstr);

    public static Constructor findConstructor(
        Class<?> clazz, Class<?>... parameterTypes) {
        Object[] data = findMethodData(clazz, "cons__", true);
        BIG: for (int i = 0; i < data.length; i += 3) {
            String sig = ((String) data[i]).substring(6);
            Class<?> cls = (Class<?>) data[i + 2];
            Constructor tmp = INSTANCE.create(cls, data[i + 1], sig);
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

    public static Constructor[] findConstructors(Class<?> clazz, int mask) {
        Object[] namesAndData = findMethodData(clazz, "", true);
        int cnt = 0;
        for (int i = 0; i < namesAndData.length; i += 3) {
            String sig = (String) namesAndData[i];
            Object data = namesAndData[i + 1];
            if (!sig.startsWith("cons__")) {
                continue;
            }
            sig = sig.substring(6);
            Class<?> cls = (Class<?>) namesAndData[i + 2];
            final Constructor m = INSTANCE.create(cls, data, sig);
            if ((m.getModifiers() & mask) == 0) {
                continue;
            }
            namesAndData[cnt++] = m;
        }
        Constructor[] arr = new Constructor[cnt];
        for (int i = 0; i < cnt; i++) {
            arr[i] = (Constructor) namesAndData[i];
        }
        return arr;
    }
    public static Method findMethod(
        Class<?> clazz, String name, Class<?>... parameterTypes) {
        Object[] data = findMethodData(clazz, name + "__", false);
        BIG: for (int i = 0; i < data.length; i += 3) {
            String sig = ((String) data[i]).substring(name.length() + 2);
            Class<?> cls = (Class<?>) data[i + 2];
            Method tmp = INSTANCE.create(cls, name, data[i + 1], sig);
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
        Object[] namesAndData = findMethodData(clazz, "", false);
        int cnt = 0;
        for (int i = 0; i < namesAndData.length; i += 3) {
            String sig = (String) namesAndData[i];
            Object data = namesAndData[i + 1];
            int middle = sig.indexOf("__");
            if (middle == -1) {
                continue;
            }
            if (sig.startsWith("$") && sig.endsWith("$")) {
                // produced by Closure compiler in debug mode
                // needs to be ignored
                continue;
            }
            String name = sig.substring(0, middle);
            sig = sig.substring(middle + 2);
            Class<?> cls = (Class<?>) namesAndData[i + 2];
            final Method m = INSTANCE.create(cls, name, data, sig);
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
    
    @Exported static String toSignature(Method m) {
        StringBuilder sb = new StringBuilder();
        sb.append(m.getName()).append("__");
        appendType(sb, m.getReturnType());
        Class<?>[] arr = m.getParameterTypes();
        for (int i = 0; i < arr.length; i++) {
            appendType(sb, arr[i]);
        }
        return sb.toString();
    }
    
    private static void appendType(StringBuilder sb, Class<?> type) {
        if (type == Integer.TYPE) {
            sb.append('I');
            return;
        }
        if (type == Long.TYPE) {
            sb.append('J');
            return;
        }
        if (type == Double.TYPE) {
            sb.append('D');
            return;
        }
        if (type == Float.TYPE) {
            sb.append('F');
            return;
        }
        if (type == Byte.TYPE) {
            sb.append('B');
            return;
        }
        if (type == Boolean.TYPE) {
            sb.append('Z');
            return;
        }
        if (type == Short.TYPE) {
            sb.append('S');
            return;
        }
        if (type == Void.TYPE) {
            sb.append('V');
            return;
        }
        if (type == Character.TYPE) {
            sb.append('C');
            return;
        }
        if (type.isArray()) {
            sb.append("_3");
            appendType(sb, type.getComponentType());
            return;
        }
        sb.append('L').append(type.getName().replace('.', '_'));
        sb.append("_2");
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
            int len;
            
            E() {
                len = sig.length();
                while (sig.charAt(len - 1) == '$') {
                    len--;
                }
            }
            
            public boolean hasMoreElements() {
                return pos < len;
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
                            int up = sig.indexOf("_2", pos);
                            String type = sig.substring(pos, up);
                            pos = up + 2;
                            return Class.forName(type.replace('_', '.'));
                        } catch (ClassNotFoundException ex) {
                            throw new IllegalStateException(ex);
                        }
                    case '_': {
                        char nch = sig.charAt(pos++);
                        assert nch == '3' : "Can't find '3' at " + sig.substring(pos - 1);
                        final Class compType = nextElement();
                        return Array.newInstance(compType, 0).getClass();
                    }
                }
                throw new UnsupportedOperationException(sig + " at " + pos);
            }
        }
        return new E();
    }
}
