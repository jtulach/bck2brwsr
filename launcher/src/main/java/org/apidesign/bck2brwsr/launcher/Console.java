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
        return "HellofromBck2Brwsr";
    }
    public static String multiply() {
        return String.valueOf(Integer.MAX_VALUE / 2 + Integer.MAX_VALUE);
    }
    
    @JavaScriptBody(args = {"id", "attr"}, body = 
        "return window.document.getElementById(id)[attr].toString();")
    private static native Object getAttr(String id, String attr);

    @JavaScriptBody(args = {"id", "attr", "value"}, body = 
        "window.document.getElementById(id)[attr] = value;")
    private static native void setAttr(String id, String attr, Object value);

    private static void addAttr(String id, String attr, String newText) {
        setAttr(id, attr, getAttr(id, attr) + "\n" + newText);
    }
    
    public static void execute() throws Exception {
        String clazz = (String) getAttr("clazz", "value");
        String method = (String) getAttr("method", "value");
        Object res = invokeMethod(clazz, method);
        setAttr("result", "value", res);
    }
    
    public static void harness(String url) {
        setAttr("result", "value", "Connecting to " + url);
        try {
            URL u = new URL(url);
            for (;;) {
                String data = (String) u.getContent(new Class[] { String.class });
                addAttr("result", "value", data);
                if (data.isEmpty()) {
                    addAttr("result", "value", "No data, exiting");
                    break;
                }
                
                Case c = Case.parseData(data);
                addAttr("result", "value", "className: " + c.getClassName());
                addAttr("result", "value", "methodName: " + c.getMethodName());
                addAttr("result", "value", "request: " + c.getRequestId());

                Object result = invokeMethod(c.getClassName(), c.getMethodName());

                addAttr("result", "value", "result: " + result);
                
                String toSend;
                if (result == null) {
                    toSend = "null";
                } else {
                    toSend = result.toString();
                }
                
                addAttr("result", "value", "Sending back: " + url + "?request=" + c.getRequestId() + "&result=" + toSend);
                u = new URL(url + "?request=" + c.getRequestId() + "&result=" + toSend);
            }
            
            
        } catch (Exception ex) {
            addAttr("result", "value", ex.getMessage());
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
    
    private static final class Case {
        private final Object data;

        private Case(Object data) {
            this.data = data;
        }
        
        public static Case parseData(String s) {
            return new Case(toJSON(s));
        }
        
        public String getMethodName() {
            return value("methodName", data);
        }

        public String getClassName() {
            return value("className", data);
        }
        
        public String getRequestId() {
            return value("request", data);
        }
        
        @JavaScriptBody(args = "s", body = "return eval('(' + s + ')');")
        private static native Object toJSON(String s);
        
        @JavaScriptBody(args = {"p", "d"}, body = "return d[p].toString();")
        private static native String value(String p, Object d);
    }
}
