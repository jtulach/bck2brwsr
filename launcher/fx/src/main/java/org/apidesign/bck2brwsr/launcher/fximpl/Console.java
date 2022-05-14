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
package org.apidesign.bck2brwsr.launcher.fximpl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import net.java.html.js.JavaScriptBody;
import netscape.javascript.JSObject;
import org.netbeans.html.boot.spi.Fn;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Console {
    private final Fn.Presenter presenter;

    public Console() {
        this.presenter = Fn.activePresenter();
        assert this.presenter != null;
    }

    @JavaScriptBody(args = { "elem", "attr" }, body = "return elem[attr].toString();")
    private static native Object getAttr(Object elem, String attr);

    @JavaScriptBody(args = { "id", "attr", "value" }, body = "\n"
        + "var e = window.document.getElementById(id);\n"
        + "if (e) e[attr] = value;\n"
    )
    private static native void setAttr(String id, String attr, Object value);

    @JavaScriptBody(args = { "elem", "attr", "value" }, body = "elem[attr] = value;")
    private static native void setAttr(Object elem, String attr, Object value);

    @JavaScriptBody(args = {}, body = "return new Date().getTime()")
    private static native double getTime();
    
    private static void closeWindow() {}

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
        Object[] arr = beginTest(c.getClassName() + "." + c.getMethodName(), c, new Object[2]);
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
        + "status.onclick = function() { c.again(arr); }\n"
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
        + "return arr;"
    )
    private static native Object[] beginTest(String test, Case c, Object[] arr);
    
    @JavaScriptBody(args = { "url", "callback" }, javacall = true, body =
          "var request = new XMLHttpRequest();\n"
        + "request.open('GET', url, true);\n"
        + "request.setRequestHeader('Content-Type', 'text/plain; charset=utf-8');\n"
        + "request.onreadystatechange = function() {\n"
        + "  if (this.readyState!==4) return;\n"
        + " try {\n"
        + "  callback.@org.apidesign.bck2brwsr.launcher.fximpl.OnMessage::onMessage(Ljava/lang/String;)(this.responseText);\n"
        + " } catch (e) { alert(e); }\n"
        + "};\n"
        + "request.send();\n"
    )
    private static native void loadText(String url, OnMessage callback) throws IOException;
    
    public static void runHarness(String url) throws IOException {
        new Console().harness(url);
    }
    
    public void harness(String url) throws IOException {
        try (var c = Fn.activate(presenter)) {
            log("Connecting to " + url);
            Request r = new Request(url);
        }
    }
    
    private static class Request implements Runnable, OnMessage {
        private final String[] arr = { null };
        private final String url;
        private Case c;
        private int retries;

        private Request(String url) throws IOException {
            this.url = url;
            loadText(url, this);
        }
        private Request(String url, String u) throws IOException {
            this.url = url;
            loadText(u, this);
        }

        @Override
        public void onMessage(String msg) {
            arr[0] = msg;
            run();
        }
        
        @Override
        public void run() {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
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
        final Object r = new Case(null).invokeMethod(clazz, method);
        return r == null ? "null" : r.toString().toString();
    }

    /** Helper method that inspects the classpath and loads given resource
     * (usually a class file). Used while running tests in Rhino.
     * 
     * @param name resource name to find
     * @return the array of bytes in the given resource
     * @throws IOException I/O in case something goes wrong
     */
    public static byte[] read(String name) throws IOException {
        URL u = null;
        Enumeration<URL> en = Console.class.getClassLoader().getResources(name);
        while (en.hasMoreElements()) {
            u = en.nextElement();
        }
        if (u == null) {
            throw new IOException("Can't find " + name);
        }
        InputStream is = null;
        try {
            is = u.openStream();
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
        } finally {
            if (is != null) is.close();
        }
    }
   
    private static void turnAssetionStatusOn() {
    }

    @JavaScriptBody(args = { "r", "time" }, javacall = true, body = 
        "return window.setTimeout(function() { "
        + "r.@java.lang.Runnable::run()(); "
        + "}, time);"
    )
    private static native Object schedule(Runnable r, int time);
    
    private static final class Case {
        private final Object data;
        private double time;
        private Object inst;

        private Case(Object data) {
            this.data = data;
        }
        
        public static Case parseData(String s) {
            return new Case(toJSON(s));
        }
        
        public String getMethodName() {
            return (String) value("methodName", data);
        }

        public String getClassName() {
            return (String) value("className", data);
        }
        
        public int getRequestId() {
            Object v = value("request", data);
            if (v instanceof Number) {
                return ((Number)v).intValue();
            }
            return Integer.parseInt(v.toString());
        }

        public String getHtmlFragment() {
            return (String) value("html", data);
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
            Object result = invokeMethod(this.getClassName(), this.getMethodName());
            setAttr("bck2brwsr.fragment", "innerHTML", "");
            log("Result: " + result);
            log("Time: " + time + " ms");
            result = encodeURL("" + result);
            return result;
        }

        private Object invokeMethod(String clazz, String method)
        throws ClassNotFoundException, InvocationTargetException,
        InterruptedException, IllegalAccessException, IllegalArgumentException,
        InstantiationException {
            Method found = null;
            Class<?> c = Class.forName(clazz);
            for (Method m : c.getMethods()) {
                if (m.getName().equals(method)) {
                    found = m;
                }
            }
            Object res;
            if (found != null) {
                try {
                    if ((found.getModifiers() & Modifier.STATIC) != 0) {
                        res = found.invoke(null);
                    } else {
                        if (inst == null) {
                            inst = c.newInstance();
                        }
                        double now = getTime();
                        res = found.invoke(inst);
                        double took = Math.round(getTime() - now);
                        time += took;
                        log("Execution took " + took + " ms");
                    }
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

        @JavaScriptBody(args = { "s" }, body = "return eval('(' + s + ')');")
        private static native Object toJSON(String s);
        
        private static Object value(String p, Object d) {
            return ((JSObject)d).getMember(p);
        }
    }
    
    static {
        turnAssetionStatusOn();
    }
}
