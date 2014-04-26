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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.apidesign.vm4brwsr.Bck2Brwsr;

/**
 *
 * @author Jaroslav Tulach
 */
class CompileCP {
    static String compileJAR(final JarFile jar) throws IOException {
        List<String> arr = new ArrayList<>();
        List<String> classes = new ArrayList<>();
        listJAR(jar, classes, arr);
        StringWriter w = new StringWriter();
        try {
            class JarRes extends EmulationResources implements Bck2Brwsr.Resources {
                @Override
                public InputStream get(String resource) throws IOException {
                    InputStream is = jar.getInputStream(new ZipEntry(resource));
                    return is == null ? super.get(resource) : is;
                }
            }
            
            Bck2Brwsr.newCompiler()
                .addClasses(classes.toArray(new String[0]))
                .library(true)
                .resources(new JarRes())
                .generate(w);
            w.flush();
            return w.toString();
        } catch (Throwable ex) {
            throw new IOException("Cannot compile: ", ex);
        } finally {
            w.close();
        }
    }
    
    static String compileFromClassPath(URL u) throws IOException, URISyntaxException {
        File f = new File(u.toURI());
        for (String s : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (!f.getPath().startsWith(s)) {
                continue;
            }
            File root = new File(s);
            List<String> arr = new ArrayList<>();
            List<String> classes = new ArrayList<>();
            listDir(root, null, classes, arr);
            StringWriter w = new StringWriter();
            try {
                Bck2Brwsr.newCompiler()
                    .addRootClasses(classes.toArray(new String[0]))
                    .library(true)
                    .resources(new EmulationResources())
                    .generate(w);
                w.flush();
                return w.toString();
            } catch (ClassFormatError ex) {
                throw new IOException(ex);
            } finally {
                w.close();
            }
        }
        return null;
    }
    
    private static void listJAR(JarFile j, List<String> classes, List<String> resources) throws IOException {
        Enumeration<JarEntry> en = j.entries();
        while (en.hasMoreElements()) {
            JarEntry e = en.nextElement();
            final String n = e.getName();
            if (n.endsWith("/")) {
                continue;
            }
            int last = n.lastIndexOf('/');
            String pkg = n.substring(0, last + 1);
            if (skipPkg(pkg)) {
                continue;
            }
            if (n.endsWith(".class")) {
                classes.add(n.substring(0, n.length() - 6));
            } else {
                resources.add(n);
            }
        }
    }

    private static boolean skipPkg(String pkg) {
        return pkg.equals("org/apidesign/bck2brwsr/launcher/");
    }
    
    private static void listDir(File f, String pref, List<String> classes, List<String> resources) throws IOException {
        File[] arr = f.listFiles();
        if (arr == null) {
            if (f.getName().endsWith(".class")) {
                classes.add(pref + f.getName().substring(0, f.getName().length() - 6));
            } else {
                resources.add(pref + f.getName());
            }
        } else {
            for (File ch : arr) {
                
                listDir(ch, pref == null ? "" : pref + f.getName() + "/", classes, resources);
            }
        }
    }

    static class EmulationResources implements Bck2Brwsr.Resources {

        @Override
        public InputStream get(String name) throws IOException {
            Enumeration<URL> en = CompileCP.class.getClassLoader().getResources(name);
            URL u = null;
            while (en.hasMoreElements()) {
                u = en.nextElement();
            }
            if (u == null) {
                throw new IOException("Can't find " + name);
            }
            if (u.toExternalForm().contains("rt.jar!")) {
                throw new IOException("No emulation for " + u);
            }
            return u.openStream();
        }
    }
}
