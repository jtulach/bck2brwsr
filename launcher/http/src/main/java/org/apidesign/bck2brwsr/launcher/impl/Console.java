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
package org.apidesign.bck2brwsr.launcher.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@org.apidesign.bck2brwsr.core.Exported 
public class Console {
    private Console() {
    }
    static {
        turnAssetionStatusOn();
    }
    
    @JavaScriptBody(args = {"id", "attr"}, body = 
        "var e = window.document.getElementById(id);" +
        "return e ? e[attr].toString() : null;"
    )
    private static native Object getAttr(String id, String attr);
    @JavaScriptBody(args = {"elem", "attr"}, body = 
        "return elem ? elem[attr].toString() : null;")
    private static native Object getAttr(Object elem, String attr);

    @JavaScriptBody(args = {"id", "attr", "value"}, body = 
        "var e = window.document.getElementById(id);\n"
      + "if (e) e[attr] = value;"
    )
    private static native void setAttr(String id, String attr, Object value);
    @JavaScriptBody(args = {"elem", "attr", "value"}, body = 
        "if (elem) elem[attr] = value;")
    private static native void setAttr(Object id, String attr, Object value);
    
    @net.java.html.js.JavaScriptBody(args = {}, body = "return new Date().getTime()")
    private static native double getTime();

    @net.java.html.js.JavaScriptBody(args = {  }, body = 
        "if (!document.getElementById('bck2brwsr.fragment')) return;\b"
      + "var a = document.createElement('a');\n"
      + "a.innerHTML = 'Cancel: closing in 10s...';\n"
      + "a.href = '#';\n"
      + "var closing = window.setTimeout(function() { window.close(); }, 10000);\n"
      + "a.onclick = function() { clearTimeout(closing); document.body.removeChild(a); };\n"
      + "document.body.appendChild(a);\n" +
        "return;\n"
    )
    static native void closeWindow();

    private static Object textArea;
    private static Object statusArea;
    
    private static void log(String newText) {
        if (textArea == null) {
            return;
        }
        String attr = "value";
        setAttr(textArea, attr, getAttr(textArea, attr) + "\n" + newText);
        setAttr(textArea, "scrollTop", getAttr(textArea, "scrollHeight"));
    }
    
    private static void beginTest(Case c) {
        Object[] arr = new Object[2];
        beginTest(c.getClassName() + "." + c.getMethodName(), c, arr);
        textArea = arr[0];
        statusArea = arr[1];
    }
    
    private static void finishTest(Case c, Object res) {
        if ("null".equals(res)) {
            setAttr(statusArea, "innerHTML", "Success");
        } else {
            setAttr(statusArea, "innerHTML", "Result " + res);
        }
        statusArea = null;
        textArea = null;
    }

    @JavaScriptBody(args = { "test", "c", "arr" }, body = 
          "var ul = window.document.getElementById('bck2brwsr.result');\n"
        + "var li = window.document.createElement('li');\n"
        + "var span = window.document.createElement('span');"
        + "span.innerHTML = test + ' - ';\n"
        + "var details = window.document.createElement('a');\n"
        + "details.innerHTML = 'Details';\n"
        + "details.href = '#';\n"
        + "var p = window.document.createElement('p');\n"
        + "var status = window.document.createElement('a');\n"
        + "status.innerHTML = 'running';"
        + "details.onclick = function() { li.appendChild(p); li.removeChild(details); status.innerHTML = 'Run Again'; status.href = '#'; };\n"
        + "status.onclick = function() { c.again__V_3Ljava_lang_Object_2(arr); }\n"
        + "var pre = window.document.createElement('textarea');\n"
        + "pre.cols = 100;"
        + "pre.rows = 10;"
        + "li.appendChild(span);\n"
        + "li.appendChild(status);\n"
        + "var span = window.document.createElement('span');"
        + "span.innerHTML = ' ';\n"
        + "li.appendChild(span);\n"
        + "li.appendChild(details);\n"
        + "p.appendChild(pre);\n"
        + "if (ul) ul.appendChild(li);\n"
        + "arr[0] = pre;\n"
        + "arr[1] = status;\n"
    )
    private static native void beginTest(String test, Case c, Object[] arr);
    
    @JavaScriptBody(args = { "url", "callback", "arr" }, body = ""
        + "var request = new XMLHttpRequest();\n"
        + "request.open('GET', url, true);\n"
        + "request.setRequestHeader('Content-Type', 'text/plain; charset=utf-8');\n"
        + "request.onreadystatechange = function() {\n"
        + "  if (this.readyState!==4) return;\n"
        + "  arr[0] = this.responseText;\n"
        + "  callback.run__V();\n"
        + "};"
        + "request.send();"
    )
    private static native void loadText(String url, Runnable callback, String[] arr) throws IOException;
    
    public static void harness(String url) throws IOException {
        log("Connecting to " + url);
        Request r = new Request(url);
    }
    
    private static class Request implements Runnable {
        private final String[] arr = { null };
        private final String url;
        private Case c;
        private int retries;

        private Request(String url) throws IOException {
            this.url = url;
            loadText(url, this, arr);
        }
        private Request(String url, String u) throws IOException {
            this.url = url;
            loadText(u, this, arr);
        }
        
        @Override
        public void run() {
            try {
                if (c == null) {
                    String data = arr[0];

                    if (data == null) {
                        log("Some error exiting");
                        closeWindow();
                        return;
                    }

                    if (data.isEmpty()) {
                        log("No data, exiting");
                        closeWindow();
                        return;
                    }

                    c = Case.parseData(data);
                    beginTest(c);
                    log("Got \"" + data + "\"");
                } else {
                    log("Processing \"" + arr[0] + "\" for " + retries + " time");
                }
                Object result = retries++ >= 100 ? "java.lang.InterruptedException:timeout(" + retries + ")" : c.runTest();
                String reply = "?request=" + c.getRequestId() + "&time=" + c.time + "&result=" + result;
                log("Sending back: ..." + reply);
                finishTest(c, result);
                
                String u = url + reply;
                new Request(url, u);
            } catch (Exception ex) {
                if (ex instanceof InterruptedException) {
                    log("Re-scheduling in 100ms");
                    schedule(this, 100);
                    return;
                }
                log(ex.getClass().getName() + ":" + ex.getMessage());
            }
        }
    }
    
    private static String encodeURL(String r) throws UnsupportedEncodingException {
        final String SPECIAL = "%$&+,/:;=?@";
        StringBuilder sb = new StringBuilder();
        byte[] utf8 = r.getBytes("UTF-8");
        for (int i = 0; i < utf8.length; i++) {
            int ch = utf8[i] & 0xff;
            if (ch < 32 || ch > 127 || SPECIAL.indexOf(ch) >= 0) {
                final String numbers = "0" + Integer.toHexString(ch);
                sb.append("%").append(numbers.substring(numbers.length() - 2));
            } else {
                if (ch == 32) {
                    sb.append("+");
                } else {
                    sb.append((char)ch);
                }
            }
        }
        return sb.toString();
    }
    
    static String invoke(String clazz, String method) throws 
    ClassNotFoundException, InvocationTargetException, IllegalAccessException, 
    InstantiationException, InterruptedException {
        final Object r = new Case(null).invokeMethod(clazz, method, null);
        return r == null ? "null" : r.toString().toString();
    }

    /** Helper method that inspects the classpath and loads given resource
     * (usually a class file). Used while running tests in Rhino.
     * 
     * @param name resource name to find
     * @return the array of bytes in the given resource
     * @throws IOException I/O in case something goes wrong
     */
    public static byte[] read(String name, int skip) throws IOException {
        URL u = null;
        if (!name.endsWith(".class")) {
            u = getResource(name, skip);
        } else {
            Enumeration<URL> en = Console.class.getClassLoader().getResources(name);
            while (en.hasMoreElements()) {
                u = en.nextElement();
            }
        }
        if (u == null) {
            if (name.endsWith(".class")) {
                throw new IOException("Can't find " + name);
            } else {
                return null;
            }
        }
        try (InputStream is = u.openStream()) {
            byte[] arr;
            arr = new byte[is.available()];
            int offset = 0;
            while (offset < arr.length) {
                int len = is.read(arr, offset, arr.length - offset);
                if (len == -1) {
                    throw new IOException("Can't read " + name);
                }
                offset += len;
            }
            return arr;
        }
    }
   
    private static URL getResource(String resource, int skip) throws IOException {
        URL u = null;
        Enumeration<URL> en = Console.class.getClassLoader().getResources(resource);
        while (en.hasMoreElements()) {
            final URL now = en.nextElement();
            if (--skip < 0) {
                u = now;
                break;
            }
        }
        return u;
    }
    
    @JavaScriptBody(args = {}, body = "vm['java_lang_Class'](false)['desiredAssertionStatus'] = true;")
    private static void turnAssetionStatusOn() {
    }

    @JavaScriptBody(args = {"r", "time"}, body =
        "return window.setTimeout(function() { r.run__V(); }, time);")
    private static native Object schedule(Runnable r, int time);
    
    private static final class Case {
        private final Object data;
        private Object inst;
        private double time;

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

        public String getHtmlFragment() {
            return value("html", data);
        }

        public String[] getArgs() {
            return values("args", data);
        }
        
        void again(Object[] arr) {
            try {
                textArea = arr[0];
                statusArea = arr[1];
                setAttr(textArea, "value", "");
                runTest();
            } catch (Exception ex) {
                log(ex.getClass().getName() + ":" + ex.getMessage());
            }
        }

        private Object runTest() throws IllegalAccessException, 
        IllegalArgumentException, ClassNotFoundException, UnsupportedEncodingException, 
        InvocationTargetException, InstantiationException, InterruptedException {
            if (this.getHtmlFragment() != null) {
                setAttr("bck2brwsr.fragment", "innerHTML", this.getHtmlFragment());
            }
            log("Invoking " + this.getClassName() + '.' + this.getMethodName() + " as request: " + this.getRequestId());
            Object result = invokeMethod(this.getClassName(), this.getMethodName(), this.getArgs());
            setAttr("bck2brwsr.fragment", "innerHTML", "");
            log("Result: " + result);
            result = encodeURL("" + result);
            return result;
        }

        private Object invokeMethod(String clazz, String method, String[] args)
        throws ClassNotFoundException, InvocationTargetException,
        InterruptedException, IllegalAccessException, IllegalArgumentException,
        InstantiationException {
            Method found = null;
            if (args == null) {
                args = new String[0];
            }
            Class<?> c = Class.forName(clazz);
            for (Method m : c.getMethods()) {
                if (m.getName().equals(method) && m.getParameterTypes().length == args.length) {
                    found = m;
                }
            }
            Object res;
            if (found != null) {
                try {
                    double now;
                    if ((found.getModifiers() & Modifier.STATIC) != 0) {
                        now = getTime();
                        res = found.invoke(null, (Object[]) args);
                    } else {
                        if (inst == null) {
                            inst = c.newInstance();
                        }
                        now = getTime();
                        res = found.invoke(inst, (Object[]) args);
                    }
                    double took = Math.round((float)(getTime() - now));
                    time += took;
                } catch (Throwable ex) {
                    if (ex instanceof InvocationTargetException) {
                        ex = ((InvocationTargetException) ex).getTargetException();
                    }
                    if (ex instanceof InterruptedException) {
                        throw (InterruptedException)ex;
                    }
                    res = ex.getClass().getName() + ":" + ex.getMessage();
                }
            } else {
                res = "Can't find method " + method + " in " + clazz;
            }
            return res;
        }
        
        @JavaScriptBody(args = "s", body = "return eval('(' + s + ')');")
        private static native Object toJSON(String s);
        
        @JavaScriptBody(args = {"p", "d"}, body = 
              "var v = d[p];\n"
            + "if (typeof v === 'undefined') return null;\n"
            + "return v.toString();"
        )
        private static native String value(String p, Object d);

        @JavaScriptBody(args = {"p", "d"}, body =
              "var v = d[p];\n"
            + "if (typeof v === 'undefined') return null;\n"
            + "return v;"
        )
        private static native String[] values(String p, Object d);
    }
}
