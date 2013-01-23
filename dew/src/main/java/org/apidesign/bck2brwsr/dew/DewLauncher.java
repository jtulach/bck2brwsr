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
package org.apidesign.bck2brwsr.dew;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apidesign.vm4brwsr.Bck2Brwsr;
import org.glassfish.grizzly.PortRange;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;

/**
 * Lightweight server to launch dew - the Development Environment for Web.
 */
final class DewLauncher {
    private static final Logger LOG = Logger.getLogger(DewLauncher.class.getName());
    private Set<ClassLoader> loaders = new LinkedHashSet<>();
    private Set<Bck2Brwsr.Resources> xRes = new LinkedHashSet<>();
    private final Res resources = new Res();
    private final String cmd;

    public DewLauncher(String cmd) {
        this.cmd = cmd;
    }
    
    public void addClassLoader(ClassLoader url) {
        this.loaders.add(url);
    }

    final HttpServer initServer(Bck2Brwsr.Resources... extraResources) {
        xRes.addAll(Arrays.asList(extraResources));
        
        HttpServer s = HttpServer.createSimpleServer(".", new PortRange(8080, 65535));

        final ServerConfiguration conf = s.getServerConfiguration();
        conf.addHttpHandler(new VM(resources), "/bck2brwsr.js");
        conf.addHttpHandler(new VMInit(), "/vm.js");
        conf.addHttpHandler(new Classes(resources), "/classes/");
        return s;
    }
    
    final Object[] launchServerAndBrwsr(HttpServer server, final String page) throws IOException, URISyntaxException, InterruptedException {
        server.start();
        NetworkListener listener = server.getListeners().iterator().next();
        int port = listener.getPort();
        
        URI uri = new URI("http://localhost:" + port + page);
        LOG.log(Level.INFO, "Showing {0}", uri);
        if (cmd == null) {
            try {
                LOG.log(Level.INFO, "Trying Desktop.browse on {0} {2} by {1}", new Object[] {
                    System.getProperty("java.vm.name"),
                    System.getProperty("java.vm.vendor"),
                    System.getProperty("java.vm.version"),
                });
                java.awt.Desktop.getDesktop().browse(uri);
                LOG.log(Level.INFO, "Desktop.browse successfully finished");
                return null;
            } catch (UnsupportedOperationException ex) {
                LOG.log(Level.INFO, "Desktop.browse not supported: {0}", ex.getMessage());
                LOG.log(Level.FINE, null, ex);
            }
        }
        {
            String cmdName = cmd == null ? "xdg-open" : cmd;
            String[] cmdArr = { 
                cmdName, uri.toString()
            };
            LOG.log(Level.INFO, "Launching {0}", Arrays.toString(cmdArr));
            final Process process = Runtime.getRuntime().exec(cmdArr);
            return new Object[] { process, null };
        }
    }

    private class Res implements Bck2Brwsr.Resources {
        @Override
        public InputStream get(String resource) throws IOException {
            for (ClassLoader l : loaders) {
                URL u = null;
                Enumeration<URL> en = l.getResources(resource);
                while (en.hasMoreElements()) {
                    u = en.nextElement();
                }
                if (u != null) {
                    return u.openStream();
                }
            }
            for (Bck2Brwsr.Resources r : xRes) {
                InputStream is = r.get(resource);
                if (is != null) {
                    return is;
                }
            }
            throw new IOException("Can't find " + resource);
        }
    }

    private static class VM extends HttpHandler {
        private final Res loader;

        public VM(Res loader) {
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
                + "  request.open('GET', '/classes/' + res, false);\n"
                + "  request.send();\n"
                + "  var arr = eval('(' + request.responseText + ')');\n"
                + "  return arr;\n"
                + "}\n"
                + "var vm = new bck2brwsr(ldCls);\n");
        }
    }

    private static class Classes extends HttpHandler {
        private final Res loader;

        public Classes(Res loader) {
            this.loader = loader;
        }

        @Override
        public void service(Request request, Response response) throws Exception {
            String res = request.getHttpHandlerPath();
            if (res.startsWith("/")) {
                res = res.substring(1);
            }
            try (InputStream is = loader.get(res)) {
                response.setContentType("text/javascript");
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
            } catch (IOException ex) {
                response.setError();
                response.setDetailMessage(ex.getMessage());
            }
        }
    }
}
