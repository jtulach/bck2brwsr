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
package org.apidesign.vm4brwsr;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Proxies implements InvocationHandler {
    private String name;

    public static String runViaProxy(boolean direct) throws Exception {
        Proxies p = new Proxies();
        Runnable r = createProxy(Runnable.class, p, direct);
        r.run();
        assertNoInstance(PlusInts.class, r);
        assertNoInstance(SumNums.class, r);
        return p.name;
    }

    private static <T> T createProxy(Class<T> type, InvocationHandler handler, boolean direct) throws Exception {
        final ClassLoader l = ClassLoader.getSystemClassLoader();
        if (direct) {
            return type.cast(Proxy.newProxyInstance(l, new Class[] { type }, handler));
        } else {
            Class<?> proxyClazz = Proxy.getProxyClass(l, new Class[] { type });
            Constructor<?> cnstr = proxyClazz.getConstructor(InvocationHandler.class);
            Object proxy = cnstr.newInstance(handler);
            return type.cast(proxy);
        }
    }

    public interface PlusInts {
        public int plus(byte b, short s, int i);
    }

    public interface SumNums {
        public Number sum(byte b, short s, int i);
    }

    public static int countViaProxy(boolean direct) throws Exception {
        InvocationHandler h = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                int i1 = ((Byte)args[0]).intValue();
                int i2 = ((Short)args[1]).intValue();
                int i3 = ((Integer)args[2]).intValue();
                return i1 + i2 + i3;
            }
        };
        PlusInts r = createProxy(PlusInts.class, h, direct);
        int res = r.plus((byte)30, (short)8, 4);
        assertNoInstance(Runnable.class, r);
        assertNoInstance(SumNums.class, r);
        return res;
    }

    public static int countViaProxies() {
        InvocationHandler h = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                int i1 = ((Byte)args[0]).intValue();
                int i2 = ((Short)args[1]).intValue();
                int i3 = ((Integer)args[2]).intValue();
                return i1 + i2 + i3;
            }
        };
        Object r = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[] { SumNums.class, PlusInts.class }, h);
        int res1 = ((PlusInts)r).plus((byte)15, (short)4, 2);
        int res2 = ((SumNums)r).sum((byte)5, (short)6, 10).intValue();
        assertNoInstance(Runnable.class, r);
        return res1 + res2;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        name = method.getName();
        return null;
    }

    private static void assertNoInstance(Class<?> type, Object obj) {
        if (type.isInstance(obj)) {
            throw new IllegalStateException(obj + " is instance of " + type.getName());
        }
    }
}
