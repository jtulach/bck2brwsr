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
package org.apidesign.bck2brwsr.launcher;

import java.io.File;
import org.apidesign.bck2brwsr.launcher.fximpl.FXBrwsr;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
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
                        List<String> params = new ArrayList<String>();
                        params.add(url.toString());
                        if (isDebugged()) {
                            params.add("--toolbar=true");
                            params.add("--firebug=true");
                            String ud = System.getProperty("netbeans.user");
                            if (ud != null) {
                                params.add("--userdir=" + ud);
                            }
                        }
                        FXBrwsr.launch(FXBrwsr.class, params.toArray(new String[params.size()]));
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
    void generateBck2BrwsrJS(StringBuilder sb, Res loader, String url, boolean unitTestMode) throws IOException {
        sb.append("(function() {\n"
            + "  var impl = this.bck2brwsr;\n"
            + "  this.bck2brwsr = function() { return impl; };\n");
        sb.append("})(window);\n");
        JVMBridge.onBck2BrwsrLoad();
        if (unitTestMode) {
            sb.append("var vm = bck2brwsr();\n");
            sb.append("try {\n");
            sb.append("    (function() {\n");
            sb.append("        var cls = vm.loadClass('org.apidesign.bck2brwsr.launcher.fximpl.Console');\n");
            sb.append("        // fxbrwsr mangling\n");
            sb.append("        var inst = cls.newInstance();\n");
            int last = url.lastIndexOf('/');
            url = url.substring(0, last + 1);
            sb.append("        inst.harness('").append(url).append("/data');\n");
            sb.append("    })();\n");
            sb.append("} catch (err) {\n");
            sb.append("    alert('Error executing harness: ' + err);\n");
            sb.append("}\n");
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        Platform.exit();
    }

    String harnessResource() {
        return "org/apidesign/bck2brwsr/launcher/fximpl/harness.html";
    }

    public static void main(String... args) throws IOException {
        String startPage = null;

        final ClassLoader cl = FXBrwsrLauncher.class.getClassLoader();
        URL[] manifestURL = { null };
        startPage = findStartPage(cl, startPage, manifestURL);
        if (startPage == null) {
            throw new NullPointerException("Can't find StartPage tag in manifests!");
        }
        
        File dir = new File(".");
        if (manifestURL[0].getProtocol().equals("jar")) {
            try {
                dir = new File(
                    ((JarURLConnection)manifestURL[0].openConnection()).getJarFileURL().toURI()
                ).getParentFile();
            } catch (URISyntaxException ex) {
                LOG.log(Level.WARNING, "Can't find root directory", ex);
            }
        }
        
        Launcher.showDir("fxbrwsr", dir, cl, startPage);
    }
    
    private static String findStartPage(
        final ClassLoader cl, String startPage, URL[] startURL
    ) throws IOException {
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
                if (startURL != null) {
                    startURL[0] = url;
                }
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
