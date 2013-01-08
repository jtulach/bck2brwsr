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
    
    @Compare public String cannotCallNonStaticMethodWithNull() throws Exception {
        StaticUse.class.getMethod("instanceMethod").invoke(null);
        return "should not happen";
    }

    @Compare public Object voidReturnType() throws Exception {
        return StaticUse.class.getMethod("instanceMethod").getReturnType();
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
    @Compare public int methodWithArgs() throws Exception {
        Method plus = StaticUse.class.getMethod("plus", int.class, Integer.TYPE);
        return (Integer)plus.invoke(null, 2, 3);
    }
    
    @JavaScriptBody(args = { "arr", "len" }, body="var a = arr.slice(0, len); a.sort(); return a;")
    private static String[] sort(String[] arr, int len) {
        List<String> list = Arrays.asList(arr).subList(0, len);
        Collections.sort(list);
        return list.toArray(new String[0]);
    }
    
    @Factory
    public static Object[] create() {
        return VMTest.create(ReflectionTest.class);
    }
    
}
