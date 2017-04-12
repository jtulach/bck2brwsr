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
package org.apidesign.vm4brwsr;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ClassesMarker(number = 10, nicknames = { "Ten", "Deset" }, count = ClassesMarker.E.TWO, subs = {
    @ClassesMarker.Anno(Integer.SIZE),
    @ClassesMarker.Anno(Integer.MIN_VALUE)
})
@ClassesNamer(name = "my text", anno = @ClassesMarker.Anno(333))
public class Classes {
    public static String nameOfIO() {
        return nameFor(IOException.class);
    }
    
    private static String nameFor(Class<?> c) {
        return c.getName();
    }
    
    private static final Class<?> PRELOAD = Runnable.class;
    
    public static boolean isInterface(String s) throws ClassNotFoundException {
        return Class.forName(s).isInterface();
    }
    
    public static boolean equalsClassesOfExceptions() {
        return MalformedURLException.class.getSuperclass() == IOException.class;
    }
    public static boolean differenceInClasses() {
        Class<?> c1 = MalformedURLException.class;
        Class<?> c2 = IOException.class;
        return c1 != c2;
    }
    
    public static String classForInstance() {
        return new IOException().getClass().getName().toString();
    }
    
    @ClassesMarker(number = 1, nicknames = { "One", "Jedna" } )
    public static String name() {
        return IOException.class.getName().toString();
    }

    @ClassesMarker(self = Self.class, number = 42, nicknames = {})
    public static class Self {
    }

    @ClassesMarker(number = 42, nicknames = {})
    public static class DefaultSelf {
    }

    public static int self() {
        ClassesMarker cm = Self.class.getAnnotation(ClassesMarker.class);
        if (cm.self() == Self.class) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int defaultSelf() {
        ClassesMarker cm = DefaultSelf.class.getAnnotation(ClassesMarker.class);
        if (cm.self() == Object.class) {
            return 1;
        } else {
            throw new IllegalStateException("" + cm.self());
        }
    }

    public static String simpleName() {
        return IOException.class.getSimpleName();
    }
    public static String canonicalName() {
        return IOException.class.getCanonicalName();
    }
    
    public static String objectName() throws NoSuchMethodException {
        return IOException.class.getMethod("wait").getDeclaringClass().getName();
    }
    
    public static boolean newInstance() throws Exception {
        IOException ioe = IOException.class.newInstance();
        if (ioe instanceof IOException) {
            return ioe.getClass() == IOException.class;
        }
        throw new IllegalStateException("Not a subtype: " + ioe);
    }
    public static String newInstanceNoPubConstructor() throws Exception {
        try {
            Float f = Float.class.newInstance();
            return "wrong, can't instantiate: " + f;
        } catch (Exception ex) {
            return (ex.getClass().getName() + ":" + ex.getMessage()).toString().toString();
        }
    }

    public static int defaultInt() {
        return DefCls.class.getAnnotation(ClassesMarker.class).number();
    }

    public static String defaultCls() {
        StringBuilder sb = new StringBuilder();
        for (Class<?> c : DefCls.class.getAnnotation(ClassesMarker.class).clsArr()) {
            sb.append(c.getName());
        }
        return sb.toString();
    }

    public static int getMarker() {
        if (!Classes.class.isAnnotationPresent(ClassesMarker.class)) {
            return -2;
        }
        ClassesMarker cm = Classes.class.getAnnotation(ClassesMarker.class);
        assert cm instanceof Object : "Is object " + cm;
        assert cm instanceof Annotation : "Is annotation " + cm;
        assert !((Object)cm instanceof Class) : "Is not Class " + cm;
        return cm == null ? -1 : cm.number();
    }
    public static int getMarkerDefault() {
        try { throw new IllegalStateException(); } catch (Exception e) {}
        ClassesMarker cm = CD.class.getAnnotation(ClassesMarker.class);
        return cm == null ? -1 : cm.number();
    }
    public static String getMarkerNicknames() {
        ClassesMarker cm = Classes.class.getAnnotation(ClassesMarker.class);
        if (cm == null) {
            return null;
        }
        
        final Object[] arr = cm.nicknames();
        assert arr instanceof Object[] : "Instance of Object array: " + arr;
        assert arr instanceof String[] : "Instance of String array: " + arr;
        assert !(arr instanceof Integer[]) : "Not instance of Integer array: " + arr;
        
        StringBuilder sb = new StringBuilder();
        for (String s : cm.nicknames()) {
            sb.append(s).append("\n");
        }
        return sb.toString().toString();
    }

    static String listObject(boolean string, String prefix) {
        final Class<?> c = string ? String.class : Object.class;
        StringBuilder sb = new StringBuilder();
        for (Method m : c.getMethods()) {
            final String n = m.getName();
            if (n.startsWith(prefix)) {
                sb.append(n).append("\n");
            }
        }
        return sb.toString().toString();
    }
    @Retention(RetentionPolicy.CLASS)
    @interface Ann {
    }
    
    public static String getRetention() throws Exception {
        Retention r = Ann.class.getAnnotation(Retention.class);
        assert r != null : "Annotation is present";
        assert r.value() == RetentionPolicy.CLASS : "Policy value is OK: " + r.value();
        return r.annotationType().getName();
    }
    public static String getMarkerE() {
        ClassesMarker cm = Classes.class.getAnnotation(ClassesMarker.class);
        if (cm == null) {
            return null;
        }
        return cm.count().name();
    }

    @ClassesMarker(nicknames = {})
    class CD {
    }
    public static String getMarkerED() {
        ClassesMarker cm = CD.class.getAnnotation(ClassesMarker.class);
        if (cm == null) {
            return null;
        }
        return cm.count().name();
    }
    public static String getNamer(boolean direct) {
        if (direct) {
            ClassesNamer cm = Classes.class.getAnnotation(ClassesNamer.class);
            return cm == null ? null : cm.name();
        }
        for (Annotation a : Classes.class.getAnnotations()) {
            if (a instanceof ClassesNamer) {
                return ((ClassesNamer)a).name();
            }
        }
        return null;
    }
    public static int getInnerNamer() {
        ClassesNamer cm = Classes.class.getAnnotation(ClassesNamer.class);
        assert cm != null : "ClassesNamer is present";
        return cm.anno().value();
    }
    public static int getInnerNamers() {
        ClassesMarker cm = Classes.class.getAnnotation(ClassesMarker.class);
        assert cm != null : "ClassesNamer is present";
        int sum = 0;
        for (ClassesMarker.Anno anno : cm.subs()) {
            sum += anno.value();
        }
        return sum;
    }
    
    public static String intType() {
        return Integer.TYPE.getName();
    }
    
    public static int primitive() {
        return 1;
    }
    public static boolean primitiveB() {
        return true;
    }
    
    public static String primitiveType(String method) throws Exception {
        return reflectiveMethodCall(false, method).getClass().getName();
    }
    
    @JavaScriptBody(args = "msg", body = "throw msg;")
    private static native void thrw(String msg);
    
    public static Object reflectiveMethodCall(boolean direct, String mn) throws Exception {
        Method find = null;
        StringBuilder sb = new StringBuilder();
        if (!direct) {
            final Class<? extends Annotation> v = ClassesMarker.class;
            for (Method m : Classes.class.getMethods()) {
                sb.append("\n").append(m.getName());
                if (mn != null) {
                    if (m.getName().equals(mn)) {
                        find = m;
                        break;
                    }
                } else {
                    if (m.getAnnotation(v) != null) {
                        find = m;
                        break;
                    }
                }
            }
        } else {
            find = Classes.class.getMethod(mn);
        }
        if (find == null) {
            thrw(sb.toString());
            throw new NullPointerException(sb.toString());
        }
        return find.invoke(null);
    }
    
    public static int reflectiveSum(int a, int b) throws Exception {
        Method m = StaticMethod.class.getMethod("sum", int.class, int.class);
        return (int) m.invoke(null, a, b);
    }
    
    private abstract class Application {
        public abstract int getID();
    }

    private class MyApplication extends Application {
        @Override
        public int getID() {
            return 1;
        }
    }

    public static boolean isClassAssignable() {
        return Application.class.isAssignableFrom(MyApplication.class);
    }
    
    public static String valueEnum(String v) {
        return ClassesMarker.E.valueOf(v).toString();
    }
    
    public static String typeOfFn() {
        return fn().getClass().getName();
    }
    
    @JavaScriptBody(args = {  }, body = "return function() { alert('x'); };")
    private native static Object fn();
    
    public static boolean instanceOfSuperInterface() {
        Object obj = new SuperSerial() {
        };
        return obj instanceof Serializable;
    }
    
    public static String superInterface() {
        return dumpInterfaces(SuperSerial.class);
    }

    private static String dumpInterfaces(final Class<?> aClass) {
        final Class<?>[] arr = aClass.getInterfaces();
        StringBuilder sb = new StringBuilder();
        for (Class<?> c : arr) {
            sb.append(c.getName()).append("\n");
        }
        return sb.toString();
    }

    public static String superInterfaceInst() {
        return dumpInterfaces(new SuperSerial() {}.getClass());
    }
    
    private static interface SuperSerial extends Serializable {
    }

    @ClassesMarker(count = ClassesMarker.E.TWO, nicknames = "", clsArr = {
        int.class, String.class, Integer.class,
        byte.class, short.class, long.class,
        float.class, double.class,
        boolean.class, char.class,
        int[].class, String[].class, Integer[][].class,
        byte[][].class, short[][][].class, long[].class,
        float[].class, double[].class,
        boolean[][].class, char[][][].class,
    })
    public static class DefCls {
    }
}
