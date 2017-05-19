/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.truffle;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class SingleJarLoader extends ClassLoader {
    private final Map<String,byte[]> resources;

    SingleJarLoader(File jar) throws IOException {
        JarFile jarFile = new JarFile(jar);
        this.resources = new HashMap<>();
        Enumeration<JarEntry> en = jarFile.entries();
        while (en.hasMoreElements()) {
            JarEntry entry = en.nextElement();
            final String name = entry.getName();
            if (!name.endsWith(".class")) {
                continue;
            }
            if (!name.startsWith("java/") &&
                !name.startsWith("org/apidesign/bck2brwsr/emul/lang/") &&
                !name.startsWith("org/apidesign/bck2brwsr/emul/reflect/") &&
                !name.startsWith("org/apidesign/vm4brwsr/api/")
            ) {
                continue;
            }
            byte[] arr = new byte[(int) entry.getSize()];
            InputStream is = jarFile.getInputStream(entry);
            int pos = 0;
            for (;;) {
                int len = is.read(arr, pos, arr.length - pos);
                if (len == -1) {
                    break;
                }
                pos += len;
                if (pos == arr.length) {
                    break;
                }
            }
            resources.put(name, arr);
        }
        jarFile.close();
        if (resources.isEmpty()) {
            throw new IllegalStateException("No java/ files in " + jar);
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        byte[] arr = resources.get(name);
        return arr == null ? null : new ByteArrayInputStream(arr);
    }

    @Override
    public Enumeration<URL> getResources(String string) throws IOException {
        URL url = getResource(string);
        Set<URL> set = url == null ? Collections.<URL>emptySet(): Collections.singleton(url);
        return Collections.enumeration(set);
    }

    @Override
    public URL getResource(String name) {
        final InputStream is = getResourceAsStream(name);
        if (is == null) {
            return null;
        }
        try {
            return new URL("resource", "", -1, name, new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL url) throws IOException {
                    return new URLConnection(url) {
                        @Override
                        public void connect() throws IOException {
                        }

                        @Override
                        public InputStream getInputStream() throws IOException {
                            return is;
                        }
                    };
                }
            });
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException();
    }

}
