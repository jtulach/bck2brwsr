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

package org.apidesign.bck2brwsr.dew;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

/**
 *
 * @author Tomas Zezula
 */
public class ClassLoaderFileManager implements JavaFileManager {

    private static final Location[] READ_LOCATIONS = {
        StandardLocation.PLATFORM_CLASS_PATH,
        StandardLocation.CLASS_PATH,
        StandardLocation.SOURCE_PATH
    };

    private static final Location[] WRITE_LOCATIONS = {
        StandardLocation.CLASS_OUTPUT,
        StandardLocation.SOURCE_OUTPUT
    };

    private static final Location[] CLASS_LOADER_LOCATIONS = {
        StandardLocation.ANNOTATION_PROCESSOR_PATH
    };

    private Map<Location, Map<String,List<MemoryFileObject>>> generated;


    ClassLoaderFileManager() {
        generated = new HashMap<>();
        for (Location l : WRITE_LOCATIONS) {
            generated.put(l, new HashMap<String, List<MemoryFileObject>>());
        }
    }


    @Override
    public ClassLoader getClassLoader(Location location) {
        if (canClassLoad(location)) {
            return new SafeClassLoader(getClass().getClassLoader());
        } else {
            return null;
        }
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        if (canRead(location)) {
            final List<JavaFileObject> res = new ArrayList<JavaFileObject>();
            for (String resource : getResources(convertFQNToResource(packageName))) {
                final JavaFileObject jfo = new ClassLoaderJavaFileObject(resource);
                if (kinds.contains(jfo.getKind())) {
                    res.add(jfo);
                }
            }
            return res;
        } else if (canWrite(location)) {
            Map<String,List<MemoryFileObject>> folders = generated.get(location);
            List<MemoryFileObject> files = folders.get(convertFQNToResource(packageName));
            if (files != null) {
                final List<JavaFileObject> res = new ArrayList<JavaFileObject>();
                for (JavaFileObject file : files) {
                    if (kinds.contains(file.getKind()) && file.getLastModified() >= 0) {
                        res.add(file);
                    }
                }
                return res;
            }
        }
        return Collections.<JavaFileObject>emptyList();
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        return ((InferableJavaFileObject)file).infer();
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        return a.toUri().equals(b.toUri());
    }

    @Override
    public boolean handleOption(String current, Iterator<String> remaining) {
        return false;
    }

    @Override
    public boolean hasLocation(Location location) {
        for (Location l : StandardLocation.values()) {
            if (l.equals(location)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
        if (canRead(location)) {
            return new ClassLoaderJavaFileObject(convertFQNToResource(className) + kind.extension);
        } else {
            throw new UnsupportedOperationException("Unsupported location for reading: " + location);   //NOI18N
        }
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        if (canWrite(location)) {
            final String resource = convertFQNToResource(className) + kind.extension;
            final MemoryFileObject res = new MemoryFileObject(resource, null);
            register(location, resource, res);
            return res;
        } else {
            throw new UnsupportedOperationException("Unsupported location for reading: " + location);   //NOI18N
        }
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        if (canRead(location)) {
            return new ClassLoaderJavaFileObject(convertFQNToResource(packageName) + '/' + relativeName); //NOI18N
        } else {
            throw new UnsupportedOperationException("Unsupported location for reading: " + location);   //NOI18N
        }
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        if (canWrite(location)) {
            final String resource = convertFQNToResource(packageName) + '/' + relativeName; //NOI18N
            final MemoryFileObject res = new MemoryFileObject(resource, null);
            register(location, resource, res);
            return res;
        } else {
            throw new UnsupportedOperationException("Unsupported location for reading: " + location);   //NOI18N
        }
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {        
    }

    @Override
    public int isSupportedOption(String option) {
        return -1;
    }

//    private List<String> getResources(String folder) throws IOException {
//        final List<String> result = new ArrayList<String>();
//        final BufferedReader in = new BufferedReader(new InputStreamReader(
//                this.getClass().getClassLoader().getResourceAsStream(String.format("%s/pkg-list", folder.substring(0))),    //NOI18N
//                "UTF-8"));  //NOI18N
//        try {
//            String line;
//            while ((line = in.readLine()) != null) {
//                result.add(line);
//            }
//        } finally {
//            in.close();
//        }
//        return result;
//    }

    //MOCK IMPL
    private List<String> getResources(String folder) throws IOException {
        if (classPathContent == null) {
            classPathContent = new HashMap<>();
//            final String boot = System.getProperty("sun.boot.class.path");  //NOI18N
            final String cp = System.getProperty("java.class.path");
            for (String entry : cp.split(File.pathSeparator)) {
                File f = new File (entry);
                if (f.canRead()) {
                    if (f.isFile()) {
                        ZipFile zf = new ZipFile(f);
                        try {
                            Enumeration<? extends ZipEntry> entries = zf.entries();
                            while (entries.hasMoreElements()) {
                                ZipEntry e = entries.nextElement();
                                if (e.isDirectory()) {
                                    continue;
                                }
                                final String name = String.format("/%s",e.getName());
                                final String owner = getOwner(name);
                                List<String> content = classPathContent.get(owner);
                                if (content == null) {
                                    content = new ArrayList<>();
                                    classPathContent.put(owner, content);
                                }
                                content.add(name);
                            }
                        } finally {
                            zf.close();
                        }
                    } else if (f.isDirectory()) {
                        addFiles(f,"/", classPathContent);
                    }
                }
            }                                    
        }
        List<String> content = classPathContent.get(folder);
        return content == null ? Collections.<String>emptyList() : content;
    }

    private void addFiles(File folder, String path, Map<String,List<String>> into) {
        for (File f : folder.listFiles()) {
            String fname = path + (path.length() == 1 ? "" : "/") +  f.getName();
            if (f.isDirectory()) {
                addFiles(f, fname, into);
            } else {
                List<String> content = into.get(path);
                if (content == null) {
                    content = new ArrayList<>();
                    classPathContent.put(path, content);
                }
                content.add(fname);
            }
        }
    }
    
    private Map<String,List<String>> classPathContent;

    private void register(Location loc, String resource, MemoryFileObject jfo) {
        Map<String,List<MemoryFileObject>> folders = generated.get(loc);
        final String folder = getOwner(resource);
        List<MemoryFileObject> content = folders.get(folder);
        if (content == null) {
            content = new ArrayList<>();
            folders.put(folder, content);
        }
        content.add(jfo);
    }

    private static String getOwner(String resource) {
        int lastSlash = resource.lastIndexOf('/');
        assert lastSlash >= 0;
        return resource.substring(0, lastSlash);
    }

    private static boolean canRead(Location loc) {
        for (Location rl : READ_LOCATIONS) {
            if (rl.equals(loc)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canWrite(Location loc) {
        for (Location wl : WRITE_LOCATIONS) {
            if (wl.equals(loc)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canClassLoad(Location loc) {
        for (Location cll : CLASS_LOADER_LOCATIONS) {
            if (cll.equals(loc)) {
                return true;
            }
        }
        return false;
    }

    static String convertFQNToResource(String fqn) {
        return '/' + fqn.replace('.', '/');   //NOI18N
    }

    static String convertResourceToFQN(String resource) {
        assert resource.startsWith("/");    //NOI18N
        int lastSlash = resource.lastIndexOf('/');  //NOI18N
        int lastDot = resource.lastIndexOf('.');    //NOI18N
        int stop = lastSlash < lastDot ?
            lastDot :
            resource.length();
        return resource.substring(1, stop).replace('/', '.');    //NOI18N
    }


    JavaFileObject createMemoryFileObject (String resourceName, JavaFileObject.Kind kind, byte[] content) {
        final InferableJavaFileObject jfo  = new MemoryFileObject(resourceName, kind, content);
        return jfo;
    }

    Iterable<? extends MemoryFileObject> getGeneratedFiles(JavaFileObject.Kind... kinds) {
        final Set<JavaFileObject.Kind> ks = EnumSet.noneOf(JavaFileObject.Kind.class);
        Collections.addAll(ks, kinds);
        final List<MemoryFileObject> res = new ArrayList<>();
        for (Map<String,List<MemoryFileObject>> folders : generated.values()) {
            for (List<MemoryFileObject> content : folders.values()) {
                for (MemoryFileObject fo : content) {
                    if (ks.contains(fo.getKind()) && fo.getLastModified() >= 0) {
                        res.add(fo);
                    }
                }
            }
        }
        return res;
    }

    private static final class SafeClassLoader extends ClassLoader {
        private final ClassLoader delegate;

        SafeClassLoader(final ClassLoader delegate) {
            this.delegate = delegate;

        }

        @Override
        public URL getResource(String name) {
            return delegate.getResource(name);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            return delegate.getResourceAsStream(name);
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            return delegate.getResources(name);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return delegate.loadClass(name);
        }
    }

}
