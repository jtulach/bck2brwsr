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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apidesign.bck2brwsr.launcher.InvocationContext.Resource;
import org.glassfish.grizzly.PortRange;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.openide.util.Exceptions;

/**
 * Lightweight server to launch Bck2Brwsr applications and tests.
 * Supports execution in native browser as well as Java's internal 
 * execution engine.
 */
abstract class BaseHTTPLauncher extends Launcher implements Closeable, Callable<HttpServer> {
    static final Logger LOG = Logger.getLogger(BaseHTTPLauncher.class.getName());
    private static final InvocationContext END = new InvocationContext(null, null, null);
    private final Set<ClassLoader> loaders = new LinkedHashSet<ClassLoader>();
    private final BlockingQueue<InvocationContext> methods = new LinkedBlockingQueue<InvocationContext>();
    private long timeOut;
    private final Res resources = new Res();
    private final String cmd;
    private Object[] brwsr;
    private HttpServer server;
    private CountDownLatch wait;
    
    public BaseHTTPLauncher(String cmd) {
        this.cmd = cmd;
        addClassLoader(BaseHTTPLauncher.class.getClassLoader());
        setTimeout(180000);
    }
    
    @Override
    InvocationContext runMethod(InvocationContext c) throws IOException {
        loaders.add(c.clazz.getClassLoader());
        methods.add(c);
        try {
            c.await(timeOut);
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
        return c;
    }
    
    public void setTimeout(long ms) {
        timeOut = ms;
    }
    
    public void addClassLoader(ClassLoader url) {
        this.loaders.add(url);
    }
    
    ClassLoader[] loaders() {
        return loaders.toArray(new ClassLoader[loaders.size()]);
    }

    public void showURL(String startpage) throws IOException {
        if (!startpage.startsWith("/")) {
            startpage = "/" + startpage;
        }
        HttpServer s = initServer(".", true, "");
        int last = startpage.lastIndexOf('/');
        String prefix = startpage.substring(0, last);
        String simpleName = startpage.substring(last);
        s.getServerConfiguration().addHttpHandler(new SubTree(resources, prefix), "/");
        server = s;
        try {
            launchServerAndBrwsr(s, simpleName);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    void showDirectory(File dir, String startpage, boolean addClasses) throws IOException {
        if (!startpage.startsWith("/")) {
            startpage = "/" + startpage;
        }
        String prefix = "";
        int last = startpage.lastIndexOf('/');
        if (last >= 0) {
            prefix = startpage.substring(0, last);
        }
        HttpServer s = initServer(dir.getPath(), addClasses, prefix);
        try {
            launchServerAndBrwsr(s, startpage);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void initialize() throws IOException {
        try {
            executeInBrowser();
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
    
    private HttpServer initServer(String path, boolean addClasses, String vmPrefix) throws IOException {
        HttpServer s = HttpServer.createSimpleServer(null, new PortRange(8080, 65535));
        /*
        ThreadPoolConfig fewThreads = ThreadPoolConfig.defaultConfig().copy().
            setPoolName("Fx/Bck2 Brwsr").
            setCorePoolSize(3).
            setMaxPoolSize(5);
        ThreadPoolConfig oneKernel = ThreadPoolConfig.defaultConfig().copy().
            setPoolName("Kernel Fx/Bck2").
            setCorePoolSize(3).
            setMaxPoolSize(3);
        for (NetworkListener nl : s.getListeners()) {
            nl.getTransport().setWorkerThreadPoolConfig(fewThreads);
            nl.getTransport().setKernelThreadPoolConfig(oneKernel);
        }
        */
        final ServerConfiguration conf = s.getServerConfiguration();
        VMAndPages vm = new VMAndPages();
        conf.addHttpHandler(vm, "/");
        if (vmPrefix != null) {
            vm.registerVM(vmPrefix + "/bck2brwsr.js");
        }
        if (path != null) {
            vm.addDocRoot(path);
        }
        if (addClasses) {
            conf.addHttpHandler(new Classes(resources), "/classes/");
        }
        final WebSocketAddOn addon = new WebSocketAddOn();
        for (NetworkListener listener : s.getListeners()) {
            listener.registerAddOn(addon);
        }
        return s;
    }
    
    private void executeInBrowser() throws InterruptedException, URISyntaxException, IOException {
        wait = new CountDownLatch(1);
        server = initServer(".", true, "");
        final ServerConfiguration conf = server.getServerConfiguration();
        
        class DynamicResourceHandler extends HttpHandler {
            private final InvocationContext ic;
            private int resourcesCount;
            DynamicResourceHandler delegate;
            public DynamicResourceHandler(InvocationContext ic) {
                this.ic = ic;
                for (Resource r : ic.resources) {
                    conf.addHttpHandler(this, r.httpPath);
                }
            }

            public void close(DynamicResourceHandler del) {
                conf.removeHttpHandler(this);
                delegate = del;
            }
            
            @Override
            public void service(Request request, Response response) throws Exception {
                if (delegate != null) {
                    delegate.service(request, response);
                    return;
                }
                
                if ("/dynamic".equals(request.getRequestURI())) {
                    boolean webSocket = false;
                    String mimeType = request.getParameter("mimeType");
                    List<String> params = new ArrayList<String>();
                    for (int i = 0; ; i++) {
                        String p = request.getParameter("param" + i);
                        if (p == null) {
                            break;
                        }
                        params.add(p);
                        if ("protocol:ws".equals(p)) {
                            webSocket = true;
                            continue;
                        }                    }
                    final String cnt = request.getParameter("content");
                    String mangle = cnt.replace("%20", " ").replace("%0A", "\n");
                    ByteArrayInputStream is = new ByteArrayInputStream(mangle.getBytes("UTF-8"));
                    URI url;
                    final Resource res = new Resource(is, mimeType, "/dynamic/res" + ++resourcesCount, params.toArray(new String[params.size()]));
                    if (webSocket) {
                        url = registerWebSocket(res);
                    } else {
                        url = registerResource(res);
                    }
                    response.getWriter().write(url.toString());
                    response.getWriter().write("\n");
                    return;
                }
                
                for (Resource r : ic.resources) {
                    if (r.httpPath.equals(request.getRequestURI())) {
                        LOG.log(Level.INFO, "Serving HttpResource for {0}", request.getRequestURI());
                        response.setContentType(r.httpType);
                        r.httpContent.reset();
                        String[] params = null;
                        if (r.parameters.length != 0) {
                            params = new String[r.parameters.length];
                            for (int i = 0; i < r.parameters.length; i++) {
                                params[i] = request.getParameter(r.parameters[i]);
                                if (params[i] == null) {
                                    if ("http.method".equals(r.parameters[i])) {
                                        params[i] = request.getMethod().toString();
                                    } else if ("http.requestBody".equals(r.parameters[i])) {
                                        Reader rdr = request.getReader();
                                        StringBuilder sb = new StringBuilder();
                                        for (;;) {
                                            int ch = rdr.read();
                                            if (ch == -1) {
                                                break;
                                            }
                                            sb.append((char)ch);
                                        }
                                        params[i] = sb.toString();
                                    }
                                }
                                if (params[i] == null) {
                                    params[i] = "null";
                                }
                            }
                        }
                        
                        copyStream(r.httpContent, response.getOutputStream(), null, params);
                    }
                }
            }
            
            private URI registerWebSocket(Resource r) {
                WebSocketEngine.getEngine().register("", r.httpPath, new WS(r));
                return pageURL("ws", server, r.httpPath);
            }

            private URI registerResource(Resource r) {
                if (!ic.resources.contains(r)) {
                    ic.resources.add(r);
                    conf.addHttpHandler(this, r.httpPath);
                }
                return pageURL("http", server, r.httpPath);
            }
        }
        
        conf.addHttpHandler(new Page(resources, harnessResource()), "/execute");
        
        conf.addHttpHandler(new HttpHandler() {
            int cnt;
            List<InvocationContext> cases = new ArrayList<InvocationContext>();
            DynamicResourceHandler prev;
            @Override
            public void service(Request request, Response response) throws Exception {
                String id = request.getParameter("request");
                String value = request.getParameter("result");
                if (value != null && value.indexOf((char)0xC5) != -1) {
                    value = toUTF8(value);
                }
                
                
                InvocationContext mi = null;
                int caseNmbr = -1;
                
                if (id != null && value != null) {
                    LOG.log(Level.INFO, "Received result for case {0} = {1}", new Object[]{id, value});
                    value = decodeURL(value);
                    int indx = Integer.parseInt(id);
                    cases.get(indx).result(value, null);
                    if (++indx < cases.size()) {
                        mi = cases.get(indx);
                        LOG.log(Level.INFO, "Re-executing case {0}", indx);
                        caseNmbr = indx;
                    }
                } else {
                    if (!cases.isEmpty()) {
                        LOG.info("Re-executing test cases");
                        mi = cases.get(0);
                        caseNmbr = 0;
                    }
                }
                
                if (mi == null) {
                    mi = methods.take();
                    caseNmbr = cnt++;
                }
                if (mi == END) {
                    response.getWriter().write("");
                    wait.countDown();
                    cnt = 0;
                    LOG.log(Level.INFO, "End of data reached. Exiting.");
                    return;
                }
                final DynamicResourceHandler newRH = new DynamicResourceHandler(mi);
                if (prev != null) {
                    prev.close(newRH);
                }
                prev = newRH;
                conf.addHttpHandler(prev, "/dynamic");
                
                cases.add(mi);
                final String cn = mi.clazz.getName();
                final String mn = mi.methodName;
                LOG.log(Level.INFO, "Request for {0} case. Sending {1}.{2}", new Object[]{caseNmbr, cn, mn});
                response.getWriter().write("{"
                    + "className: '" + cn + "', "
                    + "methodName: '" + mn + "', "
                    + "request: " + caseNmbr
                );
                if (mi.html != null) {
                    response.getWriter().write(", html: '");
                    response.getWriter().write(encodeJSON(mi.html));
                    response.getWriter().write("'");
                }
                response.getWriter().write("}");
            }
        }, "/data");

        this.brwsr = launchServerAndBrwsr(server, "/execute");
    }
    
    private static String encodeJSON(String in) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            char ch = in.charAt(i);
            if (ch < 32 || ch == '\'' || ch == '"') {
                sb.append("\\u");
                String hs = "0000" + Integer.toHexString(ch);
                hs = hs.substring(hs.length() - 4);
                sb.append(hs);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
    
    @Override
    public void shutdown() throws IOException {
        methods.offer(END);
        for (;;) {
            int prev = methods.size();
            try {
                if (wait != null && wait.await(timeOut, TimeUnit.MILLISECONDS)) {
                    break;
                }
            } catch (InterruptedException ex) {
                throw new IOException(ex);
            }
            if (prev == methods.size()) {
                LOG.log(
                    Level.WARNING, 
                    "Timeout and no test has been executed meanwhile (at {0}). Giving up.", 
                    methods.size()
                );
                break;
            }
            LOG.log(Level.INFO, 
                "Timeout, but tests got from {0} to {1}. Trying again.", 
                new Object[]{prev, methods.size()}
            );
        }
        stopServerAndBrwsr(server, brwsr);
    }
    
    static void copyStream(InputStream is, OutputStream os, String baseURL, String... params) throws IOException {
        for (;;) {
            int ch = is.read();
            if (ch == -1) {
                break;
            }
            if (ch == '$' && params.length > 0) {
                int cnt = is.read() - '0';
                if (baseURL != null && cnt == 'U' - '0') {
                    os.write(baseURL.getBytes("UTF-8"));
                } else {
                    if (cnt >= 0 && cnt < params.length) {
                        os.write(params[cnt].getBytes("UTF-8"));
                    } else {
                        os.write('$');
                        os.write(cnt + '0');
                    }
                }
            } else {
                os.write(ch);
            }
        }
    }

    private Object[] launchServerAndBrwsr(HttpServer server, final String page) throws IOException, URISyntaxException, InterruptedException {
        server.start();
        URI uri = pageURL("http", server, page);
        return showBrwsr(uri);
    }
    private static String toUTF8(String value) throws UnsupportedEncodingException {
        byte[] arr = new byte[value.length()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte)value.charAt(i);
        }
        return new String(arr, "UTF-8");
    }
    
    private static String decodeURL(String s) {
        for (;;) {
            int pos = s.indexOf('%');
            if (pos == -1) {
                return s;
            }
            int i = Integer.parseInt(s.substring(pos + 1, pos + 2), 16);
            s = s.substring(0, pos) + (char)i + s.substring(pos + 2);
        }
    }
    
    private void stopServerAndBrwsr(HttpServer server, Object[] brwsr) throws IOException {
        if (brwsr == null) {
            return;
        }
        Process process = (Process)brwsr[0];
        
        server.stop();
        InputStream stdout = process.getInputStream();
        InputStream stderr = process.getErrorStream();
        drain("StdOut", stdout);
        drain("StdErr", stderr);
        process.destroy();
        int res;
        try {
            res = process.waitFor();
        } catch (InterruptedException ex) {
            throw new IOException(ex);
        }
        LOG.log(Level.INFO, "Exit code: {0}", res);

        deleteTree((File)brwsr[1]);
    }
    
    private static void drain(String name, InputStream is) throws IOException {
        int av = is.available();
        if (av > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("v== ").append(name).append(" ==v\n");
            while (av-- > 0) {
                sb.append((char)is.read());
            }
            sb.append("\n^== ").append(name).append(" ==^");
            LOG.log(Level.INFO, sb.toString());
        }
    }

    private void deleteTree(File file) {
        if (file == null) {
            return;
        }
        File[] arr = file.listFiles();
        if (arr != null) {
            for (File s : arr) {
                deleteTree(s);
            }
        }
        file.delete();
    }

    @Override
    public HttpServer call() throws Exception {
        return server;
    }
    
    @Override
    public void close() throws IOException {
        shutdown();
    }

    protected Object[] showBrwsr(URI uri) throws IOException {
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

    abstract void generateBck2BrwsrJS(StringBuilder sb, Res loader) throws IOException;
    abstract String harnessResource();

    private static URI pageURL(String protocol, HttpServer server, final String page) {
        NetworkListener listener = server.getListeners().iterator().next();
        int port = listener.getPort();
        try {
            return new URI(protocol + "://localhost:" + port + page);
        } catch (URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }

    class Res {
        public InputStream get(String resource, int skip) throws IOException {
            if (!resource.endsWith(".class")) {
                return getResource(resource, skip);
            }
            URL u = null;
            for (ClassLoader l : loaders) {
                Enumeration<URL> en = l.getResources(resource);
                while (en.hasMoreElements()) {
                    u = en.nextElement();
                    if (u.toExternalForm().matches("^.*emul.*rt\\.jar.*$")) {
                        return u.openStream();
                    }
                }
            }
            if (u != null) {
                if (u.toExternalForm().contains("rt.jar")) {
                    LOG.log(Level.WARNING, "No fallback to bootclasspath for {0}", u);
                    return null;
                }
                return u.openStream();
            }
            throw new IOException("Can't find " + resource);
        }
        private InputStream getResource(String resource, int skip) throws IOException {
            URL u = null;
            for (ClassLoader l : loaders) {
                Enumeration<URL> en = l.getResources(resource);
                while (en.hasMoreElements()) {
                    final URL now = en.nextElement();
                    if (--skip < 0) {
                        u = now;
                        break;
                    }
                }
            }
            if (u != null) {
                return u.openStream();
            }
            return null;
        }
    }

    private static class Page extends HttpHandler {
        final String resource;
        private final String[] args;
        private final Res res;
        
        public Page(Res res, String resource, String... args) {
            this.res = res;
            this.resource = resource;
            this.args = args.length == 0 ? new String[] { "$0" } : args;
        }

        @Override
        public void service(Request request, Response response) throws Exception {
            String r = computePage(request);
            if (r.startsWith("/")) {
                r = r.substring(1);
            }
            String[] replace = {};
            if (r.endsWith(".html")) {
                response.setContentType("text/html");
                LOG.info("Content type text/html");
                replace = args;
            }
            if (r.endsWith(".xhtml")) {
                response.setContentType("application/xhtml+xml");
                LOG.info("Content type application/xhtml+xml");
                replace = args;
            }
            OutputStream os = response.getOutputStream();
            try { 
                InputStream is = res.get(r, 0);
                copyStream(is, os, request.getRequestURL().toString(), replace);
            } catch (IOException ex) {
                response.setDetailMessage(ex.getLocalizedMessage());
                response.setError();
                response.setStatus(404);
            }
        }

        protected String computePage(Request request) {
            String r = resource;
            if (r == null) {
                r = request.getHttpHandlerPath();
            }
            return r;
        }
    }
    
    private static class SubTree extends Page {

        public SubTree(Res res, String resource, String... args) {
            super(res, resource, args);
        }

        @Override
        protected String computePage(Request request) {
            return resource + request.getHttpHandlerPath();
        }
        
        
    }

    private class VMAndPages extends StaticHttpHandler {
        private String vmResource;
        
        public VMAndPages() {
            super((String[]) null);
        }
        
        @Override
        public void service(Request request, Response response) throws Exception {
            if (request.getRequestURI().equals(vmResource)) {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/javascript");
                StringBuilder sb = new StringBuilder();
                generateBck2BrwsrJS(sb, BaseHTTPLauncher.this.resources);
                response.getWriter().write(sb.toString());
            } else {
                super.service(request, response);
            }
        }

        private void registerVM(String vmResource) {
            this.vmResource = vmResource;
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
            InputStream is = null;
            try {
                String skip = request.getParameter("skip");
                int skipCnt = skip == null ? 0 : Integer.parseInt(skip);
                is = loader.get(res, skipCnt);
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
                response.setStatus(HttpStatus.NOT_FOUND_404);
                response.setError();
                response.setDetailMessage(ex.getMessage());
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }
    private static class WS extends WebSocketApplication {

        private final Resource r;

        private WS(Resource r) {
            this.r = r;
        }

        @Override
        public void onMessage(WebSocket socket, String text) {
            try {
                r.httpContent.reset();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                copyStream(r.httpContent, out, null, text);
                String s = new String(out.toByteArray(), "UTF-8");
                socket.send(s);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }}
