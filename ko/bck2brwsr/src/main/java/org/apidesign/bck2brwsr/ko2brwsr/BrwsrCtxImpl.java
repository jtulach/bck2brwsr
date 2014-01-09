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
package org.apidesign.bck2brwsr.ko2brwsr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.html.json.spi.JSONCall;
import org.apidesign.html.json.spi.Transfer;
import org.apidesign.html.json.spi.WSTransfer;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class BrwsrCtxImpl implements Transfer, WSTransfer<LoadWS> {
    private BrwsrCtxImpl() {}
    
    public static final BrwsrCtxImpl DEFAULT = new BrwsrCtxImpl();
    
    @Override
    public void extract(Object obj, String[] props, Object[] values) {
        extractJSON(obj, props, values);
    }

    @Override
    public void loadJSON(final JSONCall call) {
        class R implements Runnable {
            final boolean success;

            public R(boolean success) {
                this.success = success;
            }
            
            Object[] arr = { null };
            @Override
            public void run() {
                if (success) {
                    call.notifySuccess(arr[0]);
                } else {
                    Throwable t;
                    if (arr[0] instanceof Throwable) {
                        t = (Throwable) arr[0];
                    } else {
                        if (arr[0] == null) {
                            t = new IOException();
                        } else {
                            t = new IOException(arr[0].toString());
                        }
                    }
                    call.notifyError(t);
                }
            }
        }
        R success = new R(true);
        R failure = new R(false);
        if (call.isJSONP()) {
            String me = createJSONP(success.arr, success);
            loadJSONP(call.composeURL(me), me);
        } else {
            String data = null;
            if (call.isDoOutput()) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    call.writeData(bos);
                    data = new String(bos.toByteArray(), "UTF-8");
                } catch (IOException ex) {
                    call.notifyError(ex);
                }
            }
            loadJSON(call.composeURL(null), success.arr, success, failure, call.getMethod(), data);
        }
    }

    @Override
    public Object toJSON(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader r = new InputStreamReader(is);
        for (;;) {
            int ch = r.read();
            if (ch == -1) {
                break;
            }
            sb.append((char)ch);
        }
        return parse(sb.toString());
    }

    @Override
    public LoadWS open(String url, JSONCall callback) {
        return new LoadWS(callback, url);
    }

    @Override
    public void send(LoadWS socket, JSONCall data) {
        socket.send(data);
    }

    @Override
    public void close(LoadWS socket) {
        socket.close();
    }
    
    //
    // implementations
    //
    
    @JavaScriptBody(args = {"object", "property"},
        body
        = "if (property === null) return object;\n"
        + "if (object === null) return null;\n"
        + "var p = object[property]; return p ? p : null;"
    )
    private static Object getProperty(Object object, String property) {
        return null;
    }

    public static String createJSONP(Object[] jsonResult, Runnable whenDone) {
        int h = whenDone.hashCode();
        String name;
        for (;;) {
            name = "jsonp" + Integer.toHexString(h);
            if (defineIfUnused(name, jsonResult, whenDone)) {
                return name;
            }
            h++;
        }
    }

    @JavaScriptBody(args = {"name", "arr", "run"}, body
        = "if (window[name]) return false;\n "
        + "window[name] = function(data) {\n "
        + "  delete window[name];\n"
        + "  var el = window.document.getElementById(name);\n"
        + "  el.parentNode.removeChild(el);\n"
        + "  arr[0] = data;\n"
        + "  run.run__V();\n"
        + "};\n"
        + "return true;\n"
    )
    private static boolean defineIfUnused(String name, Object[] arr, Runnable run) {
        return true;
    }

    @JavaScriptBody(args = {"s"}, body = "return eval('(' + s + ')');")
    static Object parse(String s) {
        return s;
    }

    @JavaScriptBody(args = {"url", "arr", "callback", "onError", "method", "data"}, body = ""
        + "var request = new XMLHttpRequest();\n"
        + "if (!method) method = 'GET';\n"
        + "request.open(method, url, true);\n"
        + "request.setRequestHeader('Content-Type', 'application/json; charset=utf-8');\n"
        + "request.onreadystatechange = function() {\n"
        + "  if (this.readyState!==4) return;\n"
        + "  try {\n"
        + "    arr[0] = eval('(' + this.response + ')');\n"
        + "  } catch (error) {;\n"
        + "    arr[0] = this.response;\n"
        + "  }\n"
        + "  callback.run__V();\n"
        + "};\n"
        + "request.onerror = function (e) {\n"
        + "  arr[0] = e; onError.run__V();\n"
        + "}\n"
        + "if (data) request.send(data);"
        + "else request.send();"
    )
    static void loadJSON(
        String url, Object[] jsonResult, Runnable whenDone, Runnable whenErr, String method, String data
    ) {
    }

    @JavaScriptBody(args = {"url", "jsonp"}, body
        = "var scrpt = window.document.createElement('script');\n "
        + "scrpt.setAttribute('src', url);\n "
        + "scrpt.setAttribute('id', jsonp);\n "
        + "scrpt.setAttribute('type', 'text/javascript');\n "
        + "var body = document.getElementsByTagName('body')[0];\n "
        + "body.appendChild(scrpt);\n"
    )
    static void loadJSONP(String url, String jsonp) {

    }

    public static void extractJSON(Object jsonObject, String[] props, Object[] values) {
        for (int i = 0; i < props.length; i++) {
            values[i] = getProperty(jsonObject, props[i]);
        }
    }
    
}
