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
package org.apidesign.bck2brwsr.aot;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.apidesign.vm4brwsr.Bck2Brwsr;

/** Utilities to process JAR files and set a compiler
 * up.
 *
 * @since 0.9
 * @author Jaroslav Tulach
 */
public final class Bck2BrwsrJars {
    private static final Logger LOG = Logger.getLogger(Bck2BrwsrJars.class.getName());

    private Bck2BrwsrJars() {
    }
    
    /** Creates new compiler pre-configured from the content of 
     * provided JAR file. The compiler will compile all classes.
     * The system understands OSGi manifest entries and will export
     * all packages that are exported in the JAR file. The system
     * also recognizes META-INF/services and makes sure the class names
     * are not mangled.
     * 
     * @param c the compiler to {@link Bck2Brwsr#addClasses(java.lang.String...) add classes},
     *    {@link Bck2Brwsr#addResources(java.lang.String...) add resources} and
     *    {@link Bck2Brwsr#addExported(java.lang.String...) exported objects} to.
     *    Can be <code>null</code> - in such case an 
     *    {@link Bck2Brwsr#newCompiler() empty compiler} is constructed.
     * @param jar the file to process
     * @return newly configured compiler
     * @throws IOException if something goes wrong
     */
    public static Bck2Brwsr configureFrom(Bck2Brwsr c, File jar) throws IOException {
        if (jar.isDirectory()) {
            return configureDir(c, jar);
        }
        final JarFile jf = new JarFile(jar);
        final List<String> classes = new ArrayList<>();
        List<String> resources = new ArrayList<>();
        Set<String> exported = new HashSet<>();
        class JarRes extends EmulationResources implements Bck2Brwsr.Resources {
            JarRes() {
                super(classes);
            }
            @Override
            public InputStream get(String resource) throws IOException {
                InputStream is = getConverted(resource);
                if (is != null) {
                    return is;
                }
                is = jf.getInputStream(new ZipEntry(resource));
                return is == null ? super.get(resource) : is;
            }
        }
        JarRes jarRes = new JarRes();

        listJAR(jf, jarRes, resources, exported);
        
        String cp = jf.getManifest().getMainAttributes().getValue("Class-Path"); // NOI18N
        String[] classpath = cp == null ? new String[0] : cp.split(" ");

        if (c == null) {
            c = Bck2Brwsr.newCompiler();
        }
        
        return c
            .library(classpath)
            .addClasses(classes.toArray(new String[classes.size()]))
            .addExported(exported.toArray(new String[exported.size()]))
            .addResources(resources.toArray(new String[resources.size()]))
            .resources(jarRes);
    }
    
    private static void listJAR(
        JarFile j, EmulationResources classes,
        List<String> resources, Set<String> keep
    ) throws IOException {
        Enumeration<JarEntry> en = j.entries();
        while (en.hasMoreElements()) {
            JarEntry e = en.nextElement();
            final String n = e.getName();
            if (n.endsWith("/")) {
                continue;
            }
            if (n.startsWith("META-INF/maven/")) {
                continue;
            }
            int last = n.lastIndexOf('/');
            String pkg = n.substring(0, last + 1);
            if (pkg.startsWith("java/")) {
                keep.add(pkg);
            }
            if (n.endsWith(".class")) {
                classes.addClassResource(n);
            } else {
                resources.add(n);
                if (n.startsWith("META-INF/services/") && keep != null) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(j.getInputStream(e)));
                    for (;;) {
                        String l = r.readLine();
                        if (l == null) {
                            break;
                        }
                        if (l.startsWith("#")) {
                            continue;
                        }
                        keep.add(l.replace('.', '/'));
                    }
                }
            }
        }
        String exp = j.getManifest().getMainAttributes().getValue("Export-Package");
        if (exp != null && keep != null) {
            for (String def : exp.split(",")) {
                for (String sep : def.split(";")) {
                    keep.add(sep.replace('.', '/') + "/");
                    break;
                }
            }
        }
    }
    
    static byte[] readFrom(InputStream is) throws IOException {
        int expLen = is.available();
        if (expLen < 1) {
            expLen = 1;
        }
        byte[] arr = new byte[expLen];
        int pos = 0;
        for (;;) {
            int read = is.read(arr, pos, arr.length - pos);
            if (read == -1) {
                break;
            }
            pos += read;
            if (pos == arr.length) {
                byte[] tmp = new byte[arr.length * 2];
                System.arraycopy(arr, 0, tmp, 0, arr.length);
                arr = tmp;
            }
        }
        if (pos != arr.length) {
            byte[] tmp = new byte[pos];
            System.arraycopy(arr, 0, tmp, 0, pos);
            arr = tmp;
        }
        return arr;
    }
    

    static class EmulationResources implements Bck2Brwsr.Resources {
        private final List<String> classes;
        private final Map<String,byte[]> converted = new HashMap<>();
        private final BytecodeProcessor proc;

        protected EmulationResources(List<String> classes) {
            this.classes = classes;
            BytecodeProcessor p;
            try {
                Class<?> bpClass = Class.forName("org.apidesign.bck2brwsr.aot.RetroLambda");
                p = (BytecodeProcessor) bpClass.newInstance();
            } catch (Throwable t) {
                p = null;
            }
            this.proc = p;
        }

        protected final InputStream getConverted(String name) throws IOException {
            byte[] arr = converted.get(name);
            if (arr != null) {
                return new ByteArrayInputStream(arr);
            }
            return null;
        }
        
        @Override
        public InputStream get(String name) throws IOException {
            InputStream is = getConverted(name);
            if (is != null) {
                return is;
            }
            Enumeration<URL> en = Bck2BrwsrJars.class.getClassLoader().getResources(name);
            URL u = null;
            while (en.hasMoreElements()) {
                u = en.nextElement();
            }
            if (u == null) {
                LOG.log(Level.WARNING, "Cannot find {0}", name);
                return null;
            }
            if (u.toExternalForm().contains("/rt.jar!")) {
                LOG.log(Level.WARNING, "{0}No bootdelegation for ", name);
                return null;
            }
            return u.openStream();
        }

        private void addClassResource(String n) throws IOException {
            if (proc != null) {
                try (InputStream is = this.get(n)) {
                    Map<String, byte[]> conv = proc.process(n, readFrom(is), this);
                    if (conv != null) {
                        boolean found = false;
                        for (Map.Entry<String, byte[]> entrySet : conv.entrySet()) {
                            String res = entrySet.getKey();
                            byte[] bytes = entrySet.getValue();
                            if (res.equals(n)) {
                                found = true;
                            }
                            assert res.endsWith(".class") : "Wrong resource: " + res;
                            converted.put(res, bytes);
                            classes.add(res.substring(0, res.length() - 6));
                        }
                        if (!found) {
                            throw new IOException("Cannot find " + n + " among " + conv);
                        }
                        return;
                    }
                }
            }
            classes.add(n.substring(0, n.length() - 6));
        }
    }
    
    private static Bck2Brwsr configureDir(Bck2Brwsr c, final File dir) throws IOException {
        List<String> arr = new ArrayList<>();
        List<String> classes = new ArrayList<>();
        class DirRes extends EmulationResources {
            public DirRes(List<String> classes) {
                super(classes);
            }

            @Override
            public InputStream get(String name) throws IOException {
                InputStream is = super.get(name);
                if (is != null) {
                    return is;
                }
                File r = new File(dir, name.replace('/', File.separatorChar));
                if (r.exists()) {
                    return new FileInputStream(r);
                }
                return null;
            }
        }
        DirRes dirRes = new DirRes(classes);
        listDir(dir, null, dirRes, arr);
        if (c == null) {
            c = Bck2Brwsr.newCompiler();
        }
        return c
        .addRootClasses(classes.toArray(new String[0]))
        .addResources(arr.toArray(new String[0]))
        .library()
        //.obfuscation(ObfuscationLevel.FULL)
        .resources(dirRes);
    }

    private static void listDir(
        File f, String pref, EmulationResources res, List<String> resources
    ) throws IOException {
        File[] arr = f.listFiles();
        if (arr == null) {
            if (f.getName().endsWith(".class")) {
                res.addClassResource(pref + f.getName());
            } else {
                resources.add(pref + f.getName());
            }
        } else {
            for (File ch : arr) {
                listDir(ch, pref == null ? "" : pref + f.getName() + "/", res, resources);
            }
        }
    }
    
}
