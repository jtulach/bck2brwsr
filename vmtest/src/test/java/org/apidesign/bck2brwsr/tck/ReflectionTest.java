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
package org.apidesign.bck2brwsr.tck;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ReflectionTest {
    @Compare public boolean nonNullThis() {
        return this == null;
    }
    
    @Compare public String intType() {
        return Integer.TYPE.toString();
    }

    @Compare public String voidType() throws Exception {
        return void.class.toString();
    }

    @Compare public String longClass() {
        return long.class.toString();
    }
    
    @Compare public boolean isRunnableInterface() {
        return Runnable.class.isInterface();
    }

    @Compare public String isRunnableHasRunMethod() throws NoSuchMethodException {
        return Runnable.class.getMethod("run").getName();
    }
    
    @Compare public String namesOfMethods() {
        StringBuilder sb = new StringBuilder();
        String[] arr = new String[20];
        int i = 0;
        for (Method m : StaticUse.class.getMethods()) {
            arr[i++] = m.getName();
        }
        for (String s : sort(arr, i)) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    @Compare public String namesOfDeclaringClassesOfMethods() {
        StringBuilder sb = new StringBuilder();
        String[] arr = new String[20];
        int i = 0;
        for (Method m : StaticUse.class.getMethods()) {
            arr[i++] = m.getName() + "@" + m.getDeclaringClass().getName();
        }
        for (String s : sort(arr, i)) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }
    
    @Compare public String cannotCallNonStaticMethodWithNull() throws Exception {
        StaticUse.class.getMethod("instanceMethod").invoke(null);
        return "should not happen";
    }

    @Compare public Object voidReturnType() throws Exception {
        return StaticUse.class.getMethod("instanceMethod").getReturnType();
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @interface Ann {
    }
    
    @Compare public String annoClass() throws Exception {
        Retention r = Ann.class.getAnnotation(Retention.class);
        assert r != null : "Annotation is present";
        assert r.value() == RetentionPolicy.RUNTIME : "Policy value is OK: " + r.value();
        return r.annotationType().getName();
    }
    
    @Compare public boolean isAnnotation() {
        return Ann.class.isAnnotation();
    }
    @Compare public boolean isNotAnnotation() {
        return String.class.isAnnotation();
    }
    @Compare public boolean isNotAnnotationEnum() {
        return E.class.isAnnotation();
    }
    enum E { A, B };
    @Compare public boolean isEnum() {
        return E.A.getClass().isEnum();
    }

    @Compare public boolean isNotEnum() {
        return "".getClass().isEnum();
    }
    
    @Compare public String newInstanceFails() throws InstantiationException {
        try {
            return "success: " + StaticUse.class.newInstance();
        } catch (IllegalAccessException ex) {
            return ex.getClass().getName();
        }
    }
    
    @Compare public String paramTypes() throws Exception {
        Method plus = StaticUse.class.getMethod("plus", int.class, Integer.TYPE);
        final Class[] pt = plus.getParameterTypes();
        return pt[0].getName();
    }
    @Compare public String paramTypesNotFound() throws Exception {
        return StaticUse.class.getMethod("plus", int.class, double.class).toString();
    }
    @Compare public int methodWithArgs() throws Exception {
        Method plus = StaticUse.class.getMethod("plus", int.class, Integer.TYPE);
        return (Integer)plus.invoke(null, 2, 3);
    }
    
    @Compare public String classGetNameForByte() {
         return byte.class.getName();
    }
    @Compare public String classGetNameForBaseObject() {
        return newObject().getClass().getName();
    }
    @Compare public String classGetNameForJavaObject() {
        return new Object().getClass().getName();
    }
    @Compare public String classGetNameForObjectArray() {
        return (new Object[3]).getClass().getName();
    }
    @Compare public String classGetNameForSimpleIntArray() {
        return (new int[3]).getClass().getName();
    }
    @Compare public boolean sameClassGetNameForSimpleCharArray() {
        return (new char[3]).getClass() == (new char[34]).getClass();
    }
    @Compare public String classGetNameForMultiIntArray() {
        return (new int[3][4][5][6][7][8][9]).getClass().getName();
    }
    @Compare public String classGetNameForMultiIntArrayInner() {
        final int[][][][][][][] arr = new int[3][4][5][6][7][8][9];
        int[][][][][][] subarr = arr[0];
        int[][][][][] subsubarr = subarr[0];
        return subsubarr.getClass().getName();
    }
    @Compare public String classGetNameForMultiStringArray() {
        return (new String[3][4][5][6][7][8][9]).getClass().getName();
    }
    
    @Compare public String classForByte() throws Exception {
        return Class.forName("[Z").getName();
    }

    @Compare public String classForUnknownArray() {
        try {
            return Class.forName("[W").getName();
        } catch (Exception ex) {
            return ex.getClass().getName();
        }
    }
    
    @Compare public String classForUnknownDeepArray() {
        try {
            return Class.forName("[[[[[W").getName();
        } catch (Exception ex) {
            return ex.getClass().getName();
        }
    }
    
    @Compare public String componentGetNameForObjectArray() {
        return (new Object[3]).getClass().getComponentType().getName();
    }
    @Compare public boolean sameComponentGetNameForObjectArray() {
        return (new Object[3]).getClass().getComponentType() == Object.class;
    }
    @Compare public String componentGetNameForSimpleIntArray() {
        return (new int[3]).getClass().getComponentType().getName();
    }
    @Compare public String componentGetNameForMultiIntArray() {
        return (new int[3][4][5][6][7][8][9]).getClass().getComponentType().getName();
    }
    @Compare public String componentGetNameForMultiStringArray() {
        Class<?> c = (new String[3][4][5][6][7][8][9]).getClass();
        StringBuilder sb = new StringBuilder();
        for (;;) {
            sb.append(c.getName()).append("\n");
            c = c.getComponentType();
            if (c == null) {
                break;
            }
        }
        return sb.toString();
    }
    
    @Compare public boolean isArray() {
        return new Object[0].getClass().isArray();
    }
    
    @JavaScriptBody(args = { "arr", "len" }, body="var a = arr.slice(0, len); a.sort(); return a;")
    private static String[] sort(String[] arr, int len) {
        List<String> list = Arrays.asList(arr).subList(0, len);
        Collections.sort(list);
        return list.toArray(new String[0]);
    }
    
    @JavaScriptBody(args = {}, body = "return new Object();")
    private static Object newObject() {
        return new Object();
    }
    
    @Factory
    public static Object[] create() {
        return VMTest.create(ReflectionTest.class);
    }
    
}
