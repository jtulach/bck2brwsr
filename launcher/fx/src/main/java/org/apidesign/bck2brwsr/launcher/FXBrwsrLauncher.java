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
                        FXBrwsr.launch(FXBrwsr.class, url.toString());
                        LOG.log(Level.INFO, "Launcher is back. Closing");
                        close();
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
    void generateBck2BrwsrJS(StringBuilder sb, Object loader) throws IOException {
        sb.append("(function() {\n"
            + "  var impl = this.bck2brwsr;\n"
            + "  this.bck2brwsr = function() { return impl; };\n"
            + "})(window);\n"
        );
        JVMBridge.onBck2BrwsrLoad();
    }
    
    
    
    @Override
    public void close() throws IOException {
        super.close();
        Platform.exit();
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
            try (InputStream is = url.openStream()) {
                mf = new Manifest(is);
            }
            String sp = mf.getMainAttributes().getValue("StartPage");
            if (sp != null) {
                startPage = sp;
                break;
            }
        }
        return startPage;
    }
}
