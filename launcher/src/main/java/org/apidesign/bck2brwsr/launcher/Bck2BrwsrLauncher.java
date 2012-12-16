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

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import static org.apidesign.bck2brwsr.launcher.Bck2BrwsrLauncher.copyStream;
import org.apidesign.vm4brwsr.Bck2Brwsr;
import org.glassfish.grizzly.PortRange;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;

/**
 * Lightweight server to launch Bck2Brwsr applications in real browser.
 */
public class Bck2BrwsrLauncher {
    public static void main( String[] args ) throws Exception {
        HttpServer server = HttpServer.createSimpleServer(".", new PortRange(8080, 65535));
        final ClassLoader loader = Bck2BrwsrLauncher.class.getClassLoader();
        
        final ServerConfiguration conf = server.getServerConfiguration();
        conf.addHttpHandler(new Page("console.xhtml", 
            "org.apidesign.bck2brwsr.launcher.Console", "welcome", "false"
        ), "/console");
        conf.addHttpHandler(new VM(loader), "/bck2brwsr.js");
        conf.addHttpHandler(new VMInit(), "/vm.js");
        conf.addHttpHandler(new Classes(loader), "/classes/");
        conf.addHttpHandler(new HttpHandler() {
            @Override
            public void service(Request request, Response response) throws Exception {
                String clazz = request.getParameter("class");
                String method = request.getParameter("method");
                if (clazz == null || method == null) {
                    response.setError();
                    response.setDetailMessage("Need two parameters: class and method name!");
                    return;
                }
                response.setContentType("text/html");
                OutputStream os = response.getOutputStream();
                InputStream is = Bck2BrwsrLauncher.class.getResourceAsStream("console.xhtml");
                copyStream(is, os, clazz, method, "true");
            }
        }, "/");
        conf.addHttpHandler(new HttpHandler() {
            int cnt;
            @Override
            public void service(Request request, Response response) throws Exception {
                response.getWriter().write("{"
                    + "className: 'org.apidesign.bck2brwsr.launcher.Console',"
                    + "methodName: 'welcome',"
                    + "request: " + cnt
                    + "}");
            }
        }, "execute/data");
        conf.addHttpHandler(new Page("harness.xhtml"), "/execute");
        
        server.start();
        NetworkListener listener = server.getListeners().iterator().next();
        int port = listener.getPort();
        
        URI uri = new URI("http://localhost:" + port + "/execute");
        try {
            Desktop.getDesktop().browse(uri);
        } catch (UnsupportedOperationException ex) {
            String[] cmd = { 
                "xdg-open", uri.toString()
            };
            Runtime.getRuntime().exec(cmd).waitFor();
        }
        
        System.in.read();
    }
    
    static void copyStream(InputStream is, OutputStream os, String... params) throws IOException {
        for (;;) {
            int ch = is.read();
            if (ch == -1) {
                break;
            }
            if (ch == '$') {
                int cnt = is.read() - '0';
                if (cnt < params.length) {
                    os.write(params[cnt].getBytes());
                }
            } else {
                os.write(ch);
            }
        }
    }

    private static class Page extends HttpHandler {
        private final String resource;
        private final String[] args;
        
        public Page(String resource, String... args) {
            this.resource = resource;
            this.args = args;
        }

        @Override
        public void service(Request request, Response response) throws Exception {
            response.setContentType("text/html");
            OutputStream os = response.getOutputStream();
            InputStream is = Bck2BrwsrLauncher.class.getResourceAsStream(resource);
            copyStream(is, os, args);
        }
    }

    private static class VM extends HttpHandler {
        private final ClassLoader loader;

        public VM(ClassLoader loader) {
            this.loader = loader;
        }

        @Override
        public void service(Request request, Response response) throws Exception {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            Bck2Brwsr.generate(response.getWriter(), loader);
        }
    }
    private static class VMInit extends HttpHandler {
        public VMInit() {
        }

        @Override
        public void service(Request request, Response response) throws Exception {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/javascript");
            response.getWriter().append(
                "function ldCls(res) {\n"
                + "  var request = new XMLHttpRequest();\n"
                + "  request.open('GET', 'classes/' + res, false);\n"
                + "  request.send();\n"
                + "  var arr = eval('(' + request.responseText + ')');\n"
                + "  return arr;\n"
                + "}\n"
                + "var vm = new bck2brwsr(ldCls);\n");
        }
    }

    private static class Classes extends HttpHandler {
        private final ClassLoader loader;

        public Classes(ClassLoader loader) {
            this.loader = loader;
        }

        @Override
        public void service(Request request, Response response) throws Exception {
            String res = request.getHttpHandlerPath();
            if (res.startsWith("/")) {
                res = res.substring(1);
            }
            Enumeration<URL> en = loader.getResources(res);
            URL u = null;
            while (en.hasMoreElements()) {
                u = en.nextElement();
            }
            if (u == null) {
                response.setError();
                response.setDetailMessage("Can't find resource " + res);
            }
            response.setContentType("text/javascript");
            InputStream is = u.openStream();
            Writer w = response.getWriter();
            w.append("[");
            for (int i = 0;; i++) {
                int b = is.read();
                if (b == -1) {
                    break;
                }
                if (i > 0) {
                    w.append(", ");
                }
                if (i % 20 == 0) {
                    w.write("\n");
                }
                if (b > 127) {
                    b = b - 256;
                }
                w.append(Integer.toString(b));
            }
            w.append("\n]");
        }
    }
}
