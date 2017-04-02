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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Proxies implements InvocationHandler {
    private String name;

    public static String runViaProxy() {
        Proxies p = new Proxies();
        Runnable r = (Runnable) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[] { Runnable.class }, p);
        r.run();
        return p.name;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        name = method.getName();
        return null;
    }

}
