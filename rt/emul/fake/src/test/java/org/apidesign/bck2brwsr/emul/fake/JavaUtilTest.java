/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2018 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.emul.fake;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.Test;

public class JavaUtilTest {
    private static final ClassLoader cpLoader = JavaUtilTest.class.getClassLoader();
    
    public static void assertSignatures(Class<?> original) throws Exception {
        Class<?> fake = loadFakeClass(original);
        assertNotEquals(original, fake, "The classes shall be different");
        
        for (Method m : fake.getDeclaredMethods()) {
            Class[] parameters = sanitizeTypes(fake, original, m.getParameterTypes());
            try {
                Class<?> retType[] = { null };
                if ((m.getModifiers() & Modifier.PUBLIC) != 0) {
                    retType[0] = original.getMethod(m.getName(), parameters).getReturnType();
                } else {
                    retType[0] = original.getDeclaredMethod(m.getName(), parameters).getReturnType();
                }
                retType = sanitizeTypes(original, fake, retType);
                assertEquals(retType[0], m.getReturnType(), "Return type " + m.getName() + " has the right type");
            } catch (ReflectiveOperationException ex) {
                throw new AssertionError(
                    "Cannot find " + m.getName() + " with arguments " + 
                    toString(parameters) + " in " + original, ex
                );
            }
        }
        for (Constructor c : fake.getConstructors()) {
            try {
                if ((c.getModifiers() & Modifier.PUBLIC) != 0) {
                    original.getConstructor(c.getParameterTypes());
                } else {
                    original.getDeclaredConstructor(c.getParameterTypes());
                }
            } catch (ReflectiveOperationException ex) {
                throw new AssertionError(
                        "Cannot find constructor with arguments "
                        + toString(c.getParameterTypes()) + " in " + original, ex
                );
            }
        }
        for (Field f : fake.getFields()) {
            try {
                Field of;
                if ((f.getModifiers() & Modifier.PUBLIC) != 0) {
                    of = original.getField(f.getName());
                } else {
                    of = original.getDeclaredField(f.getName());
                }
                assertEquals(of.getType(), f.getType(), "Field " + f.getName() + " has the right type");
            } catch (ReflectiveOperationException ex) {
                throw new AssertionError(
                        "Cannot find " + f.getName() + " in " + original, ex
                );
            }
        }
    }

    private static Class<?>[] sanitizeTypes(Class<?> fakeType, Class<?> realType, Class<?>[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (fakeType == arr[i]) {
                arr[i] = realType;
            }
        }
        return arr;
    }

    @Test
    public void arrays() throws Exception {
        assertSignatures(Arrays.class);
    }

    @Test
    public void date() throws Exception {
        assertSignatures(Date.class);
    }

    @Test
    public void list() throws Exception {
        assertSignatures(List.class);
    }

    @Test
    public void locale() throws Exception {
        assertSignatures(Locale.class);
    }

    @Test
    public void map() throws Exception {
        assertSignatures(Map.class);
    }

    @Test
    public void spliterator() throws Exception {
        assertSignatures(Spliterator.class);
    }

    @Test
    public void spliterators() throws Exception {
        assertSignatures(Spliterators.class);
    }

    @Test
    public void concurrentModificationException() throws Exception {
        assertSignatures(ConcurrentModificationException.class);
    }

    @Test
    public void collections() throws Exception {
        assertSignatures(Collections.class);
    }

    private static Class<?> loadFakeClass(Class<?> original) throws ClassNotFoundException {
        ClassLoader loader = new ClassLoader(cpLoader.getParent()) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                return defineFakeClass(name);
            }
            private Class<?> defineFakeClass(String name) throws ClassNotFoundException, ClassFormatError {
                final String vmName = name.replace("lava.", "java.").replace('.', '/');
                try {
                    Enumeration<URL> en = cpLoader.getResources(vmName + ".class");
                    URL last = null;
                    while (en.hasMoreElements()) {
                        last = en.nextElement();
                    }
                    if (last == null) {
                        throw new ClassNotFoundException(name);
                    }
                    InputStream is = last.openStream();
                    assertNotEquals("Resource found for " + name, is);
                    byte[] arr = new byte[4096];
                    int offset = 0;
                    for (;;) {
                        int len = is.read(arr, offset, arr.length - offset);
                        if (len <= 0) {
                            break;
                        }
                        offset += len;
                    }
                    byte[] pattern = vmName.getBytes(StandardCharsets.US_ASCII);
                    BIG:
                    for (int i = 0; i < offset - pattern.length; i++) {
                        for (int j = 0; j < pattern.length; j++) {
                            if (pattern[j] != arr[i + j]) {
                                continue BIG;
                            }
                        }
                        arr[i] = 'l';
                    }
                    return defineClass(name, arr, 0, offset);
                } catch (IOException ex) {
                    throw new ClassNotFoundException(name, ex);
                }
            }
        };
        Class<?> fake = loader.loadClass(original.getName().replace("java.", "lava."));
        return fake;
    }

    private static String toString(Class<?>[] arr) {
        StringBuilder sb = new StringBuilder();
        for(Class<?> c : arr) {
            sb.append(c.getName()).append("\n");
        }
        return sb.toString();
    }

}
