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
package org.apidesign.bck2brwsr.ide.editor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * JNI Helper.
 * To facilitate lookup of methods by name and signature, instead of manually parsing signatures,
 * constructs the map of all methods and uses Class.getName() to generate almost-correct signatures.
 */
class JNIHelper {

    static Method method(String clazz, String method, String signature) {
        final Map<String, Method> methods = methodMap(JNIHelper.clazz(clazz));
        return methods.get(methodKey(method, signature));
    }

    static Class<?> clazz(String clazz) {
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static Map<String, Method> methodMap(final Class<?> clazz) {
        final Map<String, Method> map = new HashMap<String, Method>();
        final Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            final Method method = methods[i];
            map.put(methodKey(method.getName(), signature(method)), method);
        }
        return map;
    }

    static String methodKey(String method, String signature) {
        return method + '@' + signature;
    }

    static String signature(final Method method) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final StringBuilder b = new StringBuilder();
        for (int j = 0; j < parameterTypes.length; j++) {
            b.append(signature(parameterTypes[j]));
        }
        return b.toString();
    }

    static String signature(final Class<?> clazz) {
        if (clazz == boolean.class) return "Z";
        else if (clazz == byte.class) return "B";
        else if (clazz == char.class) return "C";
        else if (clazz == double.class) return "D";
        else if (clazz == float.class) return "F";
        else if (clazz == int.class) return "I";
        else if (clazz == long.class) return "J";
        else if (clazz == short.class) return "S";
        else if (clazz == void.class) return "V";
        else if (clazz.isArray()) return clazz.getName().replace('.','/');
        else return "L" + clazz.getName().replace('.','/') + ";";
    }
}
