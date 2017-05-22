/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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

    @Compare public boolean isAssignableToPrimitiveType() {
        return boolean.class.isAssignableFrom(Runnable.class);
    }

    @Compare public boolean isAssignableFromPrimitiveType() {
        return Runnable.class.isAssignableFrom(boolean.class);
    }

    @Compare public boolean isAssignableLongFromInt() {
        return long.class.isAssignableFrom(int.class);
    }

    @Compare public boolean isAssignableIntFromLong() {
        return int.class.isAssignableFrom(long.class);
    }

    @Compare public String isRunnableHasRunMethod() throws NoSuchMethodException {
        return Runnable.class.getMethod("run").getName();
    }

    @Compare public boolean RunnableRunReturnsVoid() throws NoSuchMethodException {
        return Runnable.class.getMethod("run").getReturnType() == Void.TYPE;
    }

    @Compare public String isRunnableDeclaresRunMethod() throws NoSuchMethodException {
        return Runnable.class.getDeclaredMethod("run").getName();
    }
    
    @Compare public String intValue() throws Exception {
        return Integer.class.getConstructor(int.class).newInstance(10).toString();
    }
    
    @Compare public String getMethodWithArray() throws Exception {
        return Proxy.class.getMethod("getProxyClass", ClassLoader.class, Class[].class).getName();
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

    @Compare
    public String namesOfDeclaredMethods() {
        StringBuilder sb = new StringBuilder();
        String[] arr = new String[20];
        Method[] methods = StaticUse.class.getDeclaredMethods();
        if (methods.length != 2) {
            throw new IllegalStateException("Expecting just two methods, was: " + methods.length);
        }
        int i = 0;
        for (Method m : methods) {
            arr[i++] = m.getName();
        }
        for (String s : sort(arr, i)) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    @Compare public String paramsOfConstructors() {
        StringBuilder sb = new StringBuilder();
        String[] arr = new String[20];
        int i = 0;
        for (Constructor<?> m : StaticUse.class.getConstructors()) {
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
    
    @Compare public String classCastException() {
        try {
            Integer i = (Integer)StaticUseSub.getNonNull();
            return "" + i.intValue();
        } catch (ClassCastException ex) {
            return ex.getClass().getName();
        }
    }

    @Compare public String methodThatThrowsException() throws Exception {
        StaticUse.class.getMethod("instanceMethod").invoke(new StaticUse());
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
    
    @Compare public String newInstanceFails() {
        try {
            return "success: " + StaticUseSub.class.newInstance();
        } catch (IllegalAccessException ex) {
            return "failure";
        } catch (InstantiationException ex) {
            return "failure";
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
    
    @Compare public int callAbst() throws Exception {
        class Impl extends Abst {
            @Override
            public int abst() {
                return 10;
            }
        }
        Abst impl = new Impl();
        return (int) Abst.class.getMethod("abst").invoke(impl);
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

    @Compare public String copyDataViaReflection() throws Exception {
        Data d = new Data();
        d.setName("Hello world!");

        Method getter = d.getClass().getMethod("getName");
        Object val = getter.invoke(d);

        Data clone = new Data();
        Method setter = d.getClass().getMethod("setName", String.class);
        setter.invoke(clone, val);

        return clone.getName();
    }

    @Compare public String copyStaticDataViaReflection() throws Exception {
        Data.setStatic("Hello world!");

        Method getter = Data.class.getMethod("getStatic");
        Object val = getter.invoke(null);

        Method setter = Data.class.getMethod("setStatic", String.class);
        setter.invoke(null, val);

        return Data.getStatic();
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
    
    public static abstract class Abst {
        public abstract int abst();
    }

    public static final class Data {
        private String name;
        private static String nameStatic;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public static String getStatic() {
            return nameStatic;
        }

        public static void setStatic(String n) {
            nameStatic = n;
        }
    }

    public static final class Co_Ty {
        public static Co_Ty fac_to_ry() {
            return new Co_Ty("Co ty na to?");
        }
        private final String msg;

        private Co_Ty(String msg) {
            this.msg = msg;
        }

        @Override
        public String toString() {
            return msg;
        }
    }

    @Compare
    public String underscoresInNames() throws Exception {
        Method factory = Co_Ty.class.getMethod("fac_to_ry");
        Co_Ty coTy = (Co_Ty) factory.invoke(null);
        return coTy.toString();
    }
}
