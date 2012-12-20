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
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
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
 * Lightweight server to launch Bck2Brwsr applications and tests.
 * Supports execution in native browser as well as Java's internal 
 * execution engine.
 */
public class Bck2BrwsrLauncher {
    private Set<ClassLoader> loaders = new LinkedHashSet<>();
    private List<MethodInvocation> methods = new ArrayList<>();
    private long timeOut;
    private String sen;
    private String showURL;
    private final Res resources = new Res();
    
    
    public MethodInvocation addMethod(Class<?> clazz, String method) {
        loaders.add(clazz.getClassLoader());
        MethodInvocation c = new MethodInvocation(clazz.getName(), method);
        methods.add(c);
        return c;
    }
    
    public void setTimeout(long ms) {
        timeOut = ms;
    }
    
    public void setScriptEngineName(String sen) {
        this.sen = sen;
    }

    public void setStartPage(String startpage) {
        if (!startpage.startsWith("/")) {
            startpage = "/" + startpage;
        }
        this.showURL = startpage;
    }

    public void addClassLoader(ClassLoader url) {
        this.loaders.add(url);
    }
    
    public static void main( String[] args ) throws Exception {
        Bck2BrwsrLauncher l = new Bck2BrwsrLauncher();
        l.setStartPage("org/apidesign/bck2brwsr/launcher/console.xhtml");
        l.addClassLoader(Bck2BrwsrLauncher.class.getClassLoader());
        l.execute();
        System.in.read();
    }


    public void execute() throws IOException {
        try {
            if (sen != null) {
                executeRhino();
            } else if (showURL != null) {
                HttpServer server = initServer();
                server.getServerConfiguration().addHttpHandler(new Page(resources, null), "/");
                launchServerAndBrwsr(server, showURL);
            } else {
                executeInBrowser();
            }
        } catch (InterruptedException ex) {
            final InterruptedIOException iio = new InterruptedIOException(ex.getMessage());
            iio.initCause(ex);
            throw iio;
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException)ex;
            }
            if (ex instanceof RuntimeException) {
                throw (RuntimeException)ex;
            }
            throw new IOException(ex);
        }
    }
    
    private void executeRhino() throws IOException, ScriptException, NoSuchMethodException {
        StringBuilder sb = new StringBuilder();
        Bck2Brwsr.generate(sb, new Res());

        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine mach = sem.getEngineByExtension(sen);

        sb.append(
              "\nvar vm = new bck2brwsr(org.apidesign.bck2brwsr.launcher.Console.read);"
            + "\nfunction initVM() { return vm; };"
            + "\n");

        Object res = mach.eval(sb.toString());
        if (!(mach instanceof Invocable)) {
            throw new IOException("It is invocable object: " + res);
        }
        Invocable code = (Invocable) mach;
        
        Object vm = code.invokeFunction("initVM");
        Object console = code.invokeMethod(vm, "loadClass", Console.class.getName());

        final MethodInvocation[] cases = this.methods.toArray(new MethodInvocation[0]);
        for (MethodInvocation mi : cases) {
            mi.result = code.invokeMethod(console, 
                "invoke__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2", 
                mi.className, mi.methodName
            ).toString();
        }
    }
    
    private HttpServer initServer() {
        HttpServer server = HttpServer.createSimpleServer(".", new PortRange(8080, 65535));

        final ServerConfiguration conf = server.getServerConfiguration();
        conf.addHttpHandler(new Page(resources, 
            "org/apidesign/bck2brwsr/launcher/console.xhtml",
            "org.apidesign.bck2brwsr.launcher.Console", "welcome", "false"
        ), "/console");
        conf.addHttpHandler(new VM(resources), "/bck2brwsr.js");
        conf.addHttpHandler(new VMInit(), "/vm.js");
        conf.addHttpHandler(new Classes(resources), "/classes/");
        return server;
    }
    
    private void executeInBrowser() throws InterruptedException, URISyntaxException, IOException {
        final CountDownLatch wait = new CountDownLatch(1);
        final MethodInvocation[] cases = this.methods.toArray(new MethodInvocation[0]);
        
        HttpServer server = initServer();
        ServerConfiguration conf = server.getServerConfiguration();
        conf.addHttpHandler(new Page(resources, 
            "org/apidesign/bck2brwsr/launcher/harness.xhtml"
        ), "/execute");
        conf.addHttpHandler(new HttpHandler() {
            int cnt;
            @Override
            public void service(Request request, Response response) throws Exception {
                String id = request.getParameter("request");
                String value = request.getParameter("result");
                if (id != null && value != null) {
                    value = value.replace("%20", " ");
                    cases[Integer.parseInt(id)].result = value;
                }
                
                if (cnt >= cases.length) {
                    response.getWriter().write("");
                    wait.countDown();
                    cnt = 0;
                    return;
                }
                
                response.getWriter().write("{"
                    + "className: '" + cases[cnt].className + "', "
                    + "methodName: '" + cases[cnt].methodName + "', "
                    + "request: " + cnt
                    + "}");
                cnt++;
            }
        }, "/data");

        launchServerAndBrwsr(server, "/execute");
        
        wait.await(timeOut, TimeUnit.MILLISECONDS);
        server.stop();
    }
    
    static void copyStream(InputStream is, OutputStream os, String baseURL, String... params) throws IOException {
        for (;;) {
            int ch = is.read();
            if (ch == -1) {
                break;
            }
            if (ch == '$') {
                int cnt = is.read() - '0';
                if (cnt == 'U' - '0') {
                    os.write(baseURL.getBytes());
                }
                if (cnt < params.length) {
                    os.write(params[cnt].getBytes());
                }
            } else {
                os.write(ch);
            }
        }
    }

    private void launchServerAndBrwsr(HttpServer server, final String page) throws IOException, URISyntaxException, InterruptedException {
        server.start();
        NetworkListener listener = server.getListeners().iterator().next();
        int port = listener.getPort();
        
        URI uri = new URI("http://localhost:" + port + page);
        try {
            Desktop.getDesktop().browse(uri);
        } catch (UnsupportedOperationException ex) {
            String[] cmd = { 
                "xdg-open", uri.toString()
            };
            Runtime.getRuntime().exec(cmd).waitFor();
        }
        System.err.println("Showing " + uri);
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
            throw new IOException("Can't find " + resource);
        }
    }

    private static class Page extends HttpHandler {
        private final String resource;
        private final String[] args;
        private final Res res;
        
        public Page(Res res, String resource, String... args) {
            this.res = res;
            this.resource = resource;
            this.args = args;
        }

        @Override
        public void service(Request request, Response response) throws Exception {
            String r = resource;
            if (r == null) {
                r = request.getHttpHandlerPath();
                if (r.startsWith("/")) {
                    r = r.substring(1);
                }
            }
            if (r.endsWith(".html") || r.endsWith(".xhtml")) {
                response.setContentType("text/html");
            }
            OutputStream os = response.getOutputStream();
            try (InputStream is = res.get(r)) {
                copyStream(is, os, request.getRequestURL().toString(), args);
            } catch (IOException ex) {
                response.setDetailMessage(ex.getLocalizedMessage());
                response.setError();
                response.setStatus(404);
            }
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
    
    public static final class MethodInvocation {
        final String className;
        final String methodName;
        String result;

        MethodInvocation(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }

        @Override
        public String toString() {
            return result;
        }
    }
}
