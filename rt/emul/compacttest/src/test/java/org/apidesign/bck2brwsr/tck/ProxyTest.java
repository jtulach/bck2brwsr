/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ProxyTest {
    @Compare public String generateAnnotation() throws Exception {
        class InvHandler implements InvocationHandler {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return "Joe Hacker";
            }
        }
        Anno anno = (Anno) Proxy.newProxyInstance(
            Anno.class.getClassLoader(), 
            new Class[] { Anno.class }, 
            new InvHandler()
        );
        return anno.name();
    }

    @Compare public int getPrimitiveType() throws Exception {
        class InvHandler implements InvocationHandler {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return 40;
            }
        }
        Anno anno = (Anno) Proxy.newProxyInstance(
            Anno.class.getClassLoader(), 
            new Class[] { Anno.class }, 
            new InvHandler()
        );
        return 2 + anno.age();
    }
    
    public static @interface Anno {
        public String name();
        public int age();
    }
    
    @Factory
    public static Object[] create() {
        return VMTest.create(ProxyTest.class);
    }
}
