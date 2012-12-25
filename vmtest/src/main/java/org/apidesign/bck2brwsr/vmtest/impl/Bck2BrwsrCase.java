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
package org.apidesign.bck2brwsr.vmtest.impl;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;
import javax.script.Invocable;
import org.apidesign.bck2brwsr.launcher.MethodInvocation;
import org.testng.ITest;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Bck2BrwsrCase implements ITest {
    private final Method m;
    private final Launchers l;
    private final int type;
    Object value;
    private Invocable code;
    private CharSequence codeSeq;
    private static final Map<Class, Object[]> compiled = new WeakHashMap<>();
    private Object inst;

    Bck2BrwsrCase(Method m, int type, Launchers l) {
        this.l = l;
        this.m = m;
        this.type = type;
    }

    @Test(groups = "run")
    public void executeCode() throws Throwable {
        if (type == 1) {
            MethodInvocation c = l.invokeMethod(m.getDeclaringClass(), m.getName(), false);
            value = c.toString();
        } else if (type == 2) {
            MethodInvocation c = l.invokeMethod(m.getDeclaringClass(), m.getName(), true);
            value = c.toString();
        } else {
            value = m.invoke(m.getDeclaringClass().newInstance());
        }
    }

    @Override
    public String getTestName() {
        return m.getName() + "[" + typeName() + "]";
    }

    final String typeName() {
        switch (type) {
            case 0:
                return "Java";
            case 1:
                return "JavaScript";
            case 2:
                return "Browser";
            default:
                return "Unknown type " + type;
        }
    }

    private static String computeSignature(Method m) {
        StringBuilder sb = new StringBuilder();
        appendType(sb, m.getReturnType());
        for (Class<?> c : m.getParameterTypes()) {
            appendType(sb, c);
        }
        return sb.toString();
    }

    private static void appendType(StringBuilder sb, Class<?> t) {
        if (t == null) {
            sb.append('V');
            return;
        }
        if (t.isPrimitive()) {
            int ch = -1;
            if (t == int.class) {
                ch = 'I';
            }
            if (t == short.class) {
                ch = 'S';
            }
            if (t == byte.class) {
                ch = 'B';
            }
            if (t == boolean.class) {
                ch = 'Z';
            }
            if (t == long.class) {
                ch = 'J';
            }
            if (t == float.class) {
                ch = 'F';
            }
            if (t == double.class) {
                ch = 'D';
            }
            assert ch != -1 : "Unknown primitive type " + t;
            sb.append((char) ch);
            return;
        }
        if (t.isArray()) {
            sb.append("_3");
            appendType(sb, t.getComponentType());
            return;
        }
        sb.append('L');
        sb.append(t.getName().replace('.', '_'));
        sb.append("_2");
    }
    
}
