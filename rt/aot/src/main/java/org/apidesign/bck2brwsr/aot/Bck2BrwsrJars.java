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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.apidesign.vm4brwsr.Bck2Brwsr;

/** Utilities to process JAR files and set a compiler
 * up 
 *
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
     * also recognizes META-INF/services and makes sure the file names
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
        final JarFile jf = new JarFile(jar);
        List<String> classes = new ArrayList<>();
        List<String> resources = new ArrayList<>();
        Set<String> exported = new HashSet<>();

        listJAR(jf, classes, resources, exported);

        class JarRes extends EmulationResources implements Bck2Brwsr.Resources {

            @Override
            public InputStream get(String resource) throws IOException {
                InputStream is = jf.getInputStream(new ZipEntry(resource));
                return is == null ? super.get(resource) : is;
            }
        }
        return Bck2Brwsr.newCompiler()
            .library(true)
            .addClasses(classes.toArray(new String[classes.size()]))
            .addExported(exported.toArray(new String[exported.size()]))
            .addResources(resources.toArray(new String[resources.size()]))
            .resources(new JarRes());
    }
    
    private static void listJAR(
        JarFile j, List<String> classes,
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
                classes.add(n.substring(0, n.length() - 6));
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

    static class EmulationResources implements Bck2Brwsr.Resources {

        @Override
        public InputStream get(String name) throws IOException {
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
    }
    
}
