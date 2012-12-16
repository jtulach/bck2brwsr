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
package org.apidesign.bck2brwsr.launcher;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class Console {
    public static String welcome() {
        final String msg = "Hello from Bck2Brwsr!";
        alert(msg);
        return msg;
    }
    
    @JavaScriptBody(args = "msg", body = "alert(msg);")
    private static native void alert(String msg);
    
    @JavaScriptBody(args = {"id", "attr"}, body = 
        "return window.document.getElementById(id)[attr].toString();")
    private static native Object getAttr(String id, String attr);

    @JavaScriptBody(args = {"id", "attr", "value"}, body = 
        "window.document.getElementById(id)[attr] = value;")
    private static native void setAttr(String id, String attr, Object value);
    
    public static void execute() throws Exception {
        String clazz = (String) getAttr("clazz", "value");
        String method = (String) getAttr("method", "value");
        Object res = invokeMethod(clazz, method);
        setAttr("result", "value", res);
    }
    
    public static void harness() {
        try {
            URL u = new URL("/execute/data");
            String data = (String) u.getContent(new Class[] { String.class });
            setAttr("result", "value", data);
        } catch (Exception ex) {
            setAttr("result", "value", ex.getMessage());
        }
    }

    private static Object invokeMethod(String clazz, String method) 
    throws ClassNotFoundException, InvocationTargetException, 
    SecurityException, IllegalAccessException, IllegalArgumentException {
        Method found = null;
        Class<?> c = Class.forName(clazz);
        for (Method m : c.getMethods()) {
            if (m.getName().equals(method)) {
                found = m;
            }
        }
        Object res;
        if (found != null) {
            res = found.invoke(null);
        } else {
            res = "Can't find method " + method + " in " + clazz;
        }
        return res;
    }
}
