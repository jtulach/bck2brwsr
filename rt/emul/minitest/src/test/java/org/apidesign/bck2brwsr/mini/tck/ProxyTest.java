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
package org.apidesign.bck2brwsr.mini.tck;

import java.lang.annotation.Annotation;
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
    
    @Compare public String castAnnoToAnnotation() throws Exception {
        class InvHandler implements InvocationHandler {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                assert method.getName().equals("annotationType");
                return Anno.class;
            }
        }
        Annotation anno = (Annotation) Proxy.newProxyInstance(
            Anno.class.getClassLoader(), 
            new Class[] { Anno.class }, 
            new InvHandler()
        );
        return anno.annotationType().getName();
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

    @Compare public int hashCodeTest() throws Exception {
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
        return anno.hashCode();
    }
    
    @Compare public boolean isSubclassOfAnno() throws Exception {
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
        return Anno.class.isAssignableFrom(anno.getClass());
    }

    @Compare public String equalsCheck() throws Exception {
        class InvHandler implements InvocationHandler {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return this == Proxy.getInvocationHandler(args[0]);
            }
        }
        final InvocationHandler h1 = new InvHandler();
        final InvocationHandler h2 = new InvHandler();
        Object p1 = Proxy.newProxyInstance(Anno.class.getClassLoader(),
            new Class[] { Anno.class }, h1);
        Object p2 = Proxy.newProxyInstance(Anno.class.getClassLoader(),
            new Class[] { Anno.class }, h1);
        Object p3 = Proxy.newProxyInstance(Anno.class.getClassLoader(),
            new Class[] { Anno.class }, h2);
        return "12:" + p1.equals(p2) + "23:" + p2.equals(p3) + "31:" + p3.equals(p1);
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
