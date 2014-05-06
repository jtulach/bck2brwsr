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
package org.apidesign.vm4brwsr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Generator of JavaScript from bytecode of classes on classpath of the VM
 * with a Main method.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Main {
    private Main() {}
    
    public static void main(String... args) throws IOException, URISyntaxException {
        final String obfuscate = "--obfuscatelevel";
        final String extension = "--createextension";

        if (args.length < 2) {
            System.err.println("Bck2Brwsr Translator from Java(tm) to JavaScript, (c) Jaroslav Tulach 2012");
            System.err.print("Usage: java -cp ... -jar ... [");
            System.err.print(obfuscate);
            System.err.print(" [");
            boolean first = true;
            for (ObfuscationLevel l : ObfuscationLevel.values()) {
                if (!first) {
                    System.err.print('|');
                }
                System.err.print(l.name());
                first = false;
            }
            System.err.print("]] [");
            System.err.print(extension);
            System.err.println("] <file_to_generate_js_code_to> java/lang/Class org/your/App ...");
            System.exit(9);
        }

        final ClassLoader mainClassLoader = Main.class.getClassLoader();

        ObfuscationLevel obfLevel = ObfuscationLevel.NONE;
        boolean createExtension = false;
        StringArray classes = new StringArray();
        String generateTo = null;
        for (int i = 0; i < args.length; i++) {
            if (obfuscate.equals(args[i])) { // NOI18N
                i++;
                try {
                    obfLevel = ObfuscationLevel.valueOf(args[i]);
                } catch (Exception e) {
                    System.err.print(obfuscate);
                    System.err.print(" parameter needs to be followed by one of ");
                    boolean first = true;
                    for (ObfuscationLevel l : ObfuscationLevel.values()) {
                        if (!first) {
                            System.err.print(", ");
                        }
                        System.err.print(l.name());
                        first = false;
                    }
                    System.err.println();
                    System.exit(1);
                }
                continue;
            }
            if (extension.equals(args[i])) { // NOI18N
                createExtension = true;
                continue;
            }
            if (generateTo == null) {
                generateTo = args[i];
            } else {
                collectClasses(classes, mainClassLoader, args[i]);
            }
        }
        
        File gt = new File(generateTo);
        if (Boolean.getBoolean("skip.if.exists") && gt.isFile()) {
            System.err.println("Skipping as " + gt + " exists.");
            System.exit(0);
        }
        
        try (Writer w = new BufferedWriter(new FileWriter(gt))) {
            Bck2Brwsr.newCompiler().library(createExtension).
                obfuscation(obfLevel).
                addRootClasses(classes.toArray()).
                resources(new LdrRsrcs(Main.class.getClassLoader(), true)).
                generate(w);
        }
    }

    private static void collectClasses(
            final StringArray dest,
            final ClassLoader cl, final String relativePath)
                throws IOException, URISyntaxException {
        final Enumeration<URL> urls = cl.getResources(relativePath);
        if (!urls.hasMoreElements()) {
            dest.add(relativePath);
            return;
        }
        do {
            final URL url = urls.nextElement();
            switch (url.getProtocol()) {
                case "file":
                    collectClasses(dest, relativePath,
                                   new File(new URI(url.toString())));
                    continue;
                case "jar":
                    final String fullPath = url.getPath();
                    final int sepIndex = fullPath.indexOf('!');
                    final String jarFilePath =
                            (sepIndex != -1) ? fullPath.substring(0, sepIndex)
                                             : fullPath;

                    final URI jarUri = new URI(jarFilePath);
                    if (jarUri.getScheme().equals("file")) {
                        try (JarFile jarFile = new JarFile(new File(jarUri))) {
                            collectClasses(dest, relativePath, jarFile);
                            continue;
                        }
                    }
                    break;
            }

            dest.add(relativePath);
        } while (urls.hasMoreElements());
    }

    private static void collectClasses(final StringArray dest,
                                       final String relativePath,
                                       final File file) {
        if (file.isDirectory()) {
            final File[] subFiles = file.listFiles();
            for (final File subFile: subFiles) {
                collectClasses(dest,
                               extendPath(relativePath, subFile.getName()),
                               subFile);
            }

            return;
        }

        final String filePath = file.getPath();
        if (filePath.endsWith(".class")) {
            validateAndAddClass(dest, relativePath);
        }
    }

    private static void collectClasses(final StringArray dest,
                                       final String relativePath,
                                       final JarFile jarFile) {
        if (relativePath.endsWith(".class")) {
            if (jarFile.getJarEntry(relativePath) != null) {
                validateAndAddClass(dest, relativePath);
            }

            return;
        }

        final String expectedPrefix =
                relativePath.endsWith("/") ? relativePath
                                           : relativePath + '/';
        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                final String entryName = entry.getName();
                if (entryName.startsWith(expectedPrefix)
                        && entryName.endsWith(".class")) {
                    validateAndAddClass(dest, entryName);
                }
            }
        }
    }

    private static String extendPath(final String relativePath,
                                     final String fileName) {
        return relativePath.endsWith("/") ? relativePath + fileName
                                          : relativePath + '/' + fileName;
    }

    private static void validateAndAddClass(final StringArray dest,
                                            final String relativePath) {
        final String className =
                relativePath.substring(0, relativePath.length() - 6);
        if (!className.endsWith("package-info")) {
            dest.add(className);
        }
    }
}
