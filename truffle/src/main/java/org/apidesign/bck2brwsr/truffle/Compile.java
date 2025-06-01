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
package org.apidesign.bck2brwsr.truffle;

import com.oracle.truffle.api.source.Source;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 *
 * @author Jaroslav Tulach
 */
final class Compile implements DiagnosticListener<JavaFileObject> {
    private final List<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<>();
    private final Map<String, byte[]> classes;
    private final String pkg;
    private final String cls;

    private Compile(Source code) throws IOException {
        this.pkg = findPkg(code.getCharacters());
        this.cls = findCls(code.getCharacters());
        classes = compile(code);
    }

    /** Performs compilation of given HTML page and associated Java code
     */
    public static Compile create(Source code) throws IOException {
        return new Compile(code);
    }

    /** Checks for given class among compiled resources */
    public Map<String,byte[]> getClasses() {
        return classes;
    }

    /** Obtains errors created during compilation.
     */
    public List<Diagnostic<? extends JavaFileObject>> getErrors() {
        List<Diagnostic<? extends JavaFileObject>> err = new ArrayList<>();
        for (Diagnostic<? extends JavaFileObject> diagnostic : errors) {
            if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                err.add(diagnostic);
            }
        }
        return err;
    }

    private Map<String, byte[]> compile(final Source code) throws IOException {
        final ClassLoaderFileManager clfm = new ClassLoaderFileManager();
        final byte[] bytes = code.getCharacters().toString().getBytes(StandardCharsets.UTF_8);
        final JavaFileObject file = clfm.createMemoryFileObject(ClassLoaderFileManager.convertFQNToResource(pkg.isEmpty() ? cls : pkg + "." + cls) + Kind.SOURCE.extension,
                Kind.SOURCE, bytes);

        JavaFileManager jfm = new ForwardingJavaFileManager<JavaFileManager>(clfm) {
        };

        final Boolean res = ToolProvider.getSystemJavaCompiler().getTask(
            null, jfm, this, Arrays.asList("-source", "1.8", "-target", "1.8"),
            null, Arrays.asList(file)
        ).call();
        Map<String, byte[]> result = new HashMap<>();
        for (MemoryFileObject generated : clfm.getGeneratedFiles(Kind.CLASS)) {
            result.put(generated.getName().substring(1), generated.getContent());
        }
        return result;
    }


    @Override
    public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
        errors.add(diagnostic);
    }
    private static String findPkg(CharSequence java) throws IOException {
        Pattern p = Pattern.compile("package\\p{javaWhitespace}*([\\p{Alnum}\\.]+)\\p{javaWhitespace}*;", Pattern.MULTILINE);
        Matcher m = p.matcher(java);
        if (!m.find()) {
            return "";
        }
        String pkg = m.group(1);
        return pkg;
    }
    private static String findCls(CharSequence java) throws IOException {
        Pattern p = Pattern.compile("class\\p{javaWhitespace}*([\\p{Alnum}\\.]+)\\p{javaWhitespace}", Pattern.MULTILINE);
        Matcher m = p.matcher(java);
        if (!m.find()) {
            throw new IOException("Can't find package declaration in the java file");
        }
        String cls = m.group(1);
        return cls;
    }

    /**
     *
     * @author Tomas Zezula
     */
    private static class MemoryFileObject extends BaseFileObject {

        private byte[] content;
        private long lastModified;

        MemoryFileObject(String resourceName, Kind kind, byte[] content) {
            super(resourceName, kind);
            this.content = content;
            this.lastModified = this.content == null ? -1 : System.currentTimeMillis();
        }

        MemoryFileObject(String resourceName, byte[] content) {
            this(resourceName, getKind(resourceName), content);
        }

        @Override
        public InputStream openInputStream() throws IOException {
            if (content == null) {
                throw new IOException();
            } else {
                return new ByteArrayInputStream(content);
            }
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return new CloseStream();
        }

        @Override
        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            return new InputStreamReader(openInputStream());
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            if (content == null) {
                throw new IOException();
            } else {
                return new String(content);
            }
        }

        @Override
        public Writer openWriter() throws IOException {
            return new OutputStreamWriter(openOutputStream());
        }

        @Override
        public long getLastModified() {
            return lastModified;
        }

        @Override
        public boolean delete() {
            return false;
        }

        byte[] getContent() {
            return content;
        }

        private class CloseStream extends OutputStream {

            private final ByteArrayOutputStream delegate;

            CloseStream() {
                super();
                delegate = new ByteArrayOutputStream();
            }

            @Override
            public void write(int b) throws IOException {
                delegate.write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                delegate.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                delegate.write(b, off, len);
            }

            @Override
            public void close() throws IOException {
                delegate.close();
                content = delegate.toByteArray();
                lastModified = System.currentTimeMillis();
            }
        }
    }

    /**
     *
     * @author Tomas Zezula
     */
    private static abstract class BaseFileObject implements JavaFileObject {

        protected final String path;
        protected final Kind kind;

        BaseFileObject(String path, Kind kind) {
            super();
            if (!path.startsWith("/")) {
                //NOI18N
                throw new IllegalArgumentException();
            }
            this.path = path;
            this.kind = kind;
        }

        public String infer() {
            return ClassLoaderFileManager.convertResourceToFQN(path);
        }

        @Override
        public Kind getKind() {
            return kind;
        }

        @Override
        public boolean isNameCompatible(String simpleName, Kind kind) {
            if (this.kind != kind) {
                return false;
            }
            String name = getSimpleName(path);
            if (name.endsWith(".java")) { // NOI18N
                return simpleName.equals(name.substring(0, name.length() - 5));
            }
            return simpleName.equals(name);
        }

        @Override
        public NestingKind getNestingKind() {
            return null;
        }

        @Override
        public Modifier getAccessLevel() {
            return null;
        }

        @Override
        public URI toUri() {
            return URI.create(escape(path));
        }

        @Override
        public String getName() {
            return path;
        }

        protected static String getSimpleName(String path) {
            int slashIndex = path.lastIndexOf('/');
            assert slashIndex >= 0;
            return (slashIndex + 1 < path.length()) ? path.substring(slashIndex + 1) : ""; //NOI18N
        }

        protected static Kind getKind(final String path) {
            final String simpleName = getSimpleName(path);
            final int dotIndex = simpleName.lastIndexOf('.'); //NOI18N
            final String ext = dotIndex > 0 ? simpleName.substring(dotIndex) : "";
            for (Kind k : Kind.values()) {
                if (k.extension.equals(ext)) {
                    return k;
                }
            }
            return Kind.OTHER;
        }

        private String escape(String path) {
            return path;
        }
    }

    /**
     *
     * @author Tomas Zezula
     */
    private static class ClassLoaderJavaFileObject extends Compile.BaseFileObject {

        ClassLoaderJavaFileObject(final String path) {
            super(path, getKind(path));
        }

        @Override
        public InputStream openInputStream() throws IOException {
            final InputStream in = getClass().getClassLoader().getResourceAsStream(path.substring(1));
            if (in == null) {
                throw new FileNotFoundException(path);
            }
            return in;
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            throw new UnsupportedOperationException("Read Only FileObject"); //NOI18N
        }

        @Override
        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            return new InputStreamReader(openInputStream());
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            final BufferedReader in = new BufferedReader(openReader(ignoreEncodingErrors));
            try {
                final StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    sb.append('\n'); //NOI18N
                }
                return sb.toString();
            } finally {
                in.close();
            }
        }

        @Override
        public Writer openWriter() throws IOException {
            return new OutputStreamWriter(openOutputStream());
        }

        @Override
        public long getLastModified() {
            return System.currentTimeMillis();
        }

        @Override
        public boolean delete() {
            return false;
        }
    }

    /**
     *
     * @author Tomas Zezula
     */
    private static class ClassLoaderFileManager implements JavaFileManager {

        private static final Location[] READ_LOCATIONS = {StandardLocation.PLATFORM_CLASS_PATH, StandardLocation.CLASS_PATH, StandardLocation.SOURCE_PATH};
        private static final Location[] WRITE_LOCATIONS = {StandardLocation.CLASS_OUTPUT, StandardLocation.SOURCE_OUTPUT};
        private static final Location[] CLASS_LOADER_LOCATIONS = {StandardLocation.ANNOTATION_PROCESSOR_PATH};
        private Map<Location, Map<String, List<MemoryFileObject>>> generated;

        ClassLoaderFileManager() {
            super();
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
                Map<String, List<MemoryFileObject>> folders = generated.get(location);
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
            return ((BaseFileObject) file).infer();
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
            if (location.getName().contains("_MODULE_")) {
                return false;
            }
            for (Location l : StandardLocation.values()) {
                if (l.equals(location)) {
                    return true;
                }
            }
            return false;
        }

        public Location getLocationForModule(Location location, String moduleName) throws IOException {
            return location;
        }

        @Override
        public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
            if (canRead(location)) {
                return new ClassLoaderJavaFileObject(convertFQNToResource(className) + kind.extension);
            } else {
                throw new UnsupportedOperationException("Unsupported location for reading: " + location); //NOI18N
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
                throw new UnsupportedOperationException("Unsupported location for reading: " + location); //NOI18N
            }
        }

        @Override
        public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
            if (canRead(location)) {
                return new ClassLoaderJavaFileObject(convertFQNToResource(packageName) + '/' + relativeName); //NOI18N
            } else {
                throw new UnsupportedOperationException("Unsupported location for reading: " + location); //NOI18N
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
                throw new UnsupportedOperationException("Unsupported location for reading: " + location); //NOI18N
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
                    File f = new File(entry);
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
                                    final String name = String.format("/%s", e.getName());
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
                            addFiles(f, "/", classPathContent);
                        }
                    }
                }
                Iterator<Map.Entry<String, List<String>>> it = classPathContent.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, List<String>> entry = it.next();
                    if (entry.getKey().startsWith("/java/")) {
                        continue;
                    }
                    it.remove();
                }
            }
            List<String> content = classPathContent.get(folder);
            return content == null ? Collections.<String>emptyList() : content;
        }

        private void addFiles(File folder, String path, Map<String, List<String>> into) {
            for (File f : folder.listFiles()) {
                String fname = path + (path.length() == 1 ? "" : "/") + f.getName();
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
        private Map<String, List<String>> classPathContent;

        private void register(Location loc, String resource, MemoryFileObject jfo) {
            Map<String, List<MemoryFileObject>> folders = generated.get(loc);
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
            return '/' + fqn.replace('.', '/'); //NOI18N
        }

        static String convertResourceToFQN(String resource) {
            assert resource.startsWith("/"); //NOI18N
            int lastSlash = resource.lastIndexOf('/'); //NOI18N
            int lastDot = resource.lastIndexOf('.'); //NOI18N
            int stop = lastSlash < lastDot ? lastDot : resource.length();
            return resource.substring(1, stop).replace('/', '.'); //NOI18N
        }

        JavaFileObject createMemoryFileObject(String resourceName, JavaFileObject.Kind kind, byte[] content) {
            return new MemoryFileObject(resourceName, kind, content);
        }

        Iterable<? extends MemoryFileObject> getGeneratedFiles(JavaFileObject.Kind... kinds) {
            final Set<JavaFileObject.Kind> ks = EnumSet.noneOf(JavaFileObject.Kind.class);
            Collections.addAll(ks, kinds);
            final List<MemoryFileObject> res = new ArrayList<>();
            for (Map<String, List<MemoryFileObject>> folders : generated.values()) {
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
                super();
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
}
