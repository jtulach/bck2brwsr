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

import org.apidesign.bck2brwsr.launcher.fximpl.FXBrwsr;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;

import java.util.concurrent.Executors;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.apidesign.bck2brwsr.launcher.fximpl.JVMBridge;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class FXBrwsrLauncher extends BaseHTTPLauncher {
    private static final Logger LOG = Logger.getLogger(FXBrwsrLauncher.class.getName());
    static {
        try {
            Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            m.setAccessible(true);
            URL l = new URL("file://" + System.getProperty("java.home") + "/lib/jfxrt.jar");
            LOG.log(Level.INFO, "url : {0}", l);
            m.invoke(ClassLoader.getSystemClassLoader(), l);
        } catch (Exception ex) {
            throw new LinkageError("Can't add jfxrt.jar on the classpath", ex);
        }
    }

    public FXBrwsrLauncher(String ignore) {
        super(null);
    }

    @Override
    protected Object[] showBrwsr(final URI url) throws IOException {
        try {
            LOG.log(Level.INFO, "showBrwsr for {0}", url);
            JVMBridge.registerClassLoaders(loaders());
            LOG.info("About to launch WebView");
            Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    LOG.log(Level.INFO, "In FX thread. Launching!");
                    try {
                        if (isDebugged()) {
                            FXBrwsr.launch(FXBrwsr.class, url.toString(), "--toolbar=true");
                        } else {
                            FXBrwsr.launch(FXBrwsr.class, url.toString());
                        }
                        LOG.log(Level.INFO, "Launcher is back. Closing");
                        close();
                        System.exit(0);
                    } catch (Throwable ex) {
                        LOG.log(Level.WARNING, "Error launching Web View", ex);
                    }
                }
            });
        } catch (Throwable ex) {
            LOG.log(Level.WARNING, "Can't open WebView", ex);
        }
        return null;
    }
    
    @Override
    void generateBck2BrwsrJS(StringBuilder sb, Res loader) throws IOException {
        sb.append("(function() {\n"
            + "  var impl = this.bck2brwsr;\n"
            + "  this.bck2brwsr = function() { return impl; };\n");
        if (isDebugged()) {
            sb.append("var scr = window.document.createElement('script');\n");
            sb.append("scr.type = 'text/javascript';\n");
            sb.append("scr.src = 'https://getfirebug.com/firebug-lite.js';\n");
            sb.append("scr.text = '{ startOpened: true }';\n");
            sb.append("var head = window.document.getElementsByTagName('head')[0];");
            sb.append("head.appendChild(scr);\n");
            sb.append("var html = window.document.getElementsByTagName('html')[0];");
            sb.append("html.debug = true;\n");
        }
        
        sb.append("})(window);\n");
        JVMBridge.onBck2BrwsrLoad();
    }

    @Override
    public void close() throws IOException {
        super.close();
        Platform.exit();
    }

    String harnessResource() {
        return "org/apidesign/bck2brwsr/launcher/fximpl/harness.xhtml";
    }

    public static void main(String... args) throws IOException {
        String startPage = null;

        final ClassLoader cl = FXBrwsrLauncher.class.getClassLoader();
        startPage = findStartPage(cl, startPage);
        if (startPage == null) {
            throw new NullPointerException("Can't find StartPage tag in manifests!");
        }
        
        Launcher.showURL("fxbrwsr", cl, startPage);
    }
    
    private static String findStartPage(final ClassLoader cl, String startPage) throws IOException {
        Enumeration<URL> en = cl.getResources("META-INF/MANIFEST.MF");
        while (en.hasMoreElements()) {
            URL url = en.nextElement();
            Manifest mf;
            InputStream is = null;
            try {
                is = url.openStream();
                mf = new Manifest(is);
            } finally {
                if (is != null) is.close();
            }
            String sp = mf.getMainAttributes().getValue("StartPage");
            if (sp != null) {
                startPage = sp;
                break;
            }
        }
        return startPage;
    }
    
    private static boolean isDebugged() {
        try {
            return isDebuggedImpl();
        } catch (LinkageError e) {
            return false;
        }
    }

    private static boolean isDebuggedImpl() {
        java.lang.management.RuntimeMXBean runtime;
        runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
        List<String> args = runtime.getInputArguments();
        if (args.contains("-Xdebug")) { // NOI18N
            return true;
        }
        return false;
    }
}
