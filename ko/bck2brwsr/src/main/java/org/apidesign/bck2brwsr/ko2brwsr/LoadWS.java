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

import net.java.html.js.JavaScriptBody;
import org.apidesign.html.json.spi.JSONCall;

/** Communication with WebSockets for WebView 1.8.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class LoadWS {
    private static final boolean SUPPORTED = isWebSocket();
    private final Object ws;
    private final JSONCall call;
    LoadWS(JSONCall first, String url) {
        call = first;
        ws = initWebSocket(this, url);
        if (ws == null) {
            first.notifyError(new IllegalArgumentException("Wrong URL: " + url));
        }
    }
    
    static boolean isSupported() {
        return SUPPORTED;
    }
    
    void send(JSONCall call) {
        push(call);
    }
    
    private synchronized void push(JSONCall call) {
        send(ws, call.getMessage());
    }

    void onOpen(Object ev) {
        if (!call.isDoOutput()) {
            call.notifySuccess(null);
        }
    }
    
    
    @JavaScriptBody(args = { "data" }, body = "try {\n"
        + "    return eval('(' + data + ')');\n"
        + "  } catch (error) {;\n"
        + "    return data;\n"
        + "  }\n"
    )
    private static native Object toJSON(String data);
    
    void onMessage(Object ev, String data) {
        Object json = toJSON(data);
        call.notifySuccess(json);
    }
    
    void onError(Object ev) {
        call.notifyError(new Exception(ev.toString()));
    }

    void onClose(boolean wasClean, int code, String reason) {
        call.notifyError(null);
    }
    
    @JavaScriptBody(args = {}, body = "if (window.WebSocket) return true; else return false;")
    private static boolean isWebSocket() {
        return false;
    }

    @JavaScriptBody(args = { "back", "url" }, javacall = true, body = ""
        + "if (window.WebSocket) {\n"
        + "  try {\n"
        + "    var ws = new window.WebSocket(url);\n"
        + "    ws.onopen = function(ev) {\n"
        + "      back.@org.apidesign.bck2brwsr.ko2brwsr.LoadWS::onOpen(Ljava/lang/Object;)(ev);\n"
        + "    };\n"
        + "    ws.onmessage = function(ev) {\n"
        + "      back.@org.apidesign.bck2brwsr.ko2brwsr.LoadWS::onMessage(Ljava/lang/Object;Ljava/lang/String;)(ev, ev.data);\n"
        + "    };\n"
        + "    ws.onerror = function(ev) {\n"
        + "      back.@org.apidesign.bck2brwsr.ko2brwsr.LoadWS::onError(Ljava/lang/Object;)(ev);\n"
        + "    };\n"
        + "    ws.onclose = function(ev) {\n"
        + "      back.@org.apidesign.bck2brwsr.ko2brwsr.LoadWS::onClose(ZILjava/lang/String;)(ev.wasClean, ev.code, ev.reason);\n"
        + "    };\n"
        + "    return ws;\n"
        + "  } catch (ex) {\n"
        + "    return null;\n"
        + "  }\n"
        + "} else {\n"
        + "  return null;\n"
        + "}\n"
    )
    private static Object initWebSocket(Object back, String url) {
        return null;
    }
    

    @JavaScriptBody(args = { "ws", "msg" }, body = ""
        + "ws.send(msg);"
    )
    private void send(Object ws, String msg) {
    }

    @JavaScriptBody(args = { "ws" }, body = "ws.close();")
    private static void close(Object ws) {
    }

    void close() {
        close(ws);
    }
}
