/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.bck2brwsr.dew;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Compile implements JavaFileManager, DiagnosticListener<JavaFileObject> {
    private final JFO jfo;
    private final String pkg;
    private StandardJavaFileManager delegate;
    private List<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<>();

    public Compile(String pkg, JFO jfo) {
        this.pkg = pkg;
        this.jfo = jfo;
    }
    
    /*
    public static Map<String,byte[]> compile(String html, String java) throws IOException {
        JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        String pkg = findPkg(java);
        String cls = findCls(java);
        
        JFO jfo = new JFO(java, pkg.replace('.', '/') + '/' + cls + ".java");
        final Compile cmp = new Compile(pkg, jfo);
        cmp.delegate = jc.getStandardFileManager(cmp, Locale.ENGLISH, Charset.forName("UTF-8"));
        
        Set<String> toCmp = Collections.singleton(pkg + '.' + cls);
        Set<JFO> unit = Collections.singleton(jfo);
        CompilationTask task = jc.getTask(null, cmp, cmp, null, null, unit);
        if (task.call() != true) {
            throw new IOException("Compilation failed: " + cmp.errors);
        }
        return Collections.emptyMap();
    }
    */
    public static Map<String, byte[]> compile(final String html, final String code) throws IOException {
        final String pkg = findPkg(code);
//        String cls = findCls(code);
        
        DiagnosticListener<JavaFileObject> devNull = new DiagnosticListener<JavaFileObject>() {
            public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
                System.err.println("diagnostic=" + diagnostic);
            }
        };
        StandardJavaFileManager sjfm = ToolProvider.getSystemJavaCompiler().getStandardFileManager(devNull, null, null);

//        sjfm.setLocation(StandardLocation.PLATFORM_CLASS_PATH, toFiles(boot));
//        sjfm.setLocation(StandardLocation.CLASS_PATH, toFiles(compile));

        final Map<String, ByteArrayOutputStream> class2BAOS = new HashMap<String, ByteArrayOutputStream>();

        JavaFileObject file = new SimpleJavaFileObject(URI.create("mem://mem"), Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                return code;
            }
        };
        final JavaFileObject htmlFile = new SimpleJavaFileObject(URI.create("mem://mem2"), Kind.OTHER) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                return html;
            }

            @Override
            public InputStream openInputStream() throws IOException {
                return new ByteArrayInputStream(html.getBytes());
            }
        };
        
        final URI scratch;
        try {
            scratch = new URI("mem://mem3");
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
        
        JavaFileManager jfm = new ForwardingJavaFileManager<JavaFileManager>(sjfm) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
                if (kind  == Kind.CLASS) {
                    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                    class2BAOS.put(className.replace('.', '/') + ".class", buffer);
                    return new SimpleJavaFileObject(sibling.toUri(), kind) {
                        @Override
                        public OutputStream openOutputStream() throws IOException {
                            return buffer;
                        }
                    };
                }
                
                if (kind == Kind.SOURCE) {
                    return new SimpleJavaFileObject(scratch/*sibling.toUri()*/, kind) {
                        private final ByteArrayOutputStream data = new ByteArrayOutputStream();
                        @Override
                        public OutputStream openOutputStream() throws IOException {
                            return data;
                        }

                        @Override
                        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
                            data.close();
                            return new String(data.toByteArray());
                        }
                    };
                }
                
                throw new IllegalStateException();
            }
//            @Override
//            public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
//                if (location == StandardLocation.PLATFORM_CLASS_PATH) {
//                    return super.list(location, packageName, kinds, recurse);
//                }
//                if (location == StandardLocation.CLASS_PATH) {
//                    return super.list(location, packageName, kinds, recurse);
//                }
//                if (location == StandardLocation.SOURCE_PATH) {
//                    System.out.println("src path for " + packageName + " kinds: " + kinds);
//                    if (packageName.equals(pkg) && kinds.contains(Kind.OTHER)) {
//                        return Collections.<JavaFileObject>singleton(htmlFile);
//                    }
//                    return Collections.emptyList();
//                }
//                throw new UnsupportedOperationException("Loc: " + location + " pkg: " + packageName + " kinds: " + kinds + " rec: " + recurse);
//            }

            @Override
            public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
                if (location == StandardLocation.SOURCE_PATH) {
//                    System.out.println("src path for " + packageName + " kinds: " + kinds);
                    if (packageName.equals(pkg)) {
                        return htmlFile;
                    }
                }
                
                return null;
            }
            
        };

        ToolProvider.getSystemJavaCompiler().getTask(null, jfm, devNull, /*XXX:*/Arrays.asList("-source", "1.7", "-target", "1.7"), null, Arrays.asList(file)).call();

        Map<String, byte[]> result = new HashMap<String, byte[]>();

        for (Map.Entry<String, ByteArrayOutputStream> e : class2BAOS.entrySet()) {
            result.put(e.getKey(), e.getValue().toByteArray());
        }

        return result;
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return null;//Compile.class.getClassLoader();
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
        if (location == StandardLocation.PLATFORM_CLASS_PATH) {
            return delegate.list(location, packageName, kinds, recurse);
        }
        if (location == StandardLocation.CLASS_PATH) {
            return delegate.list(location, packageName, kinds, recurse);
        }
        if (location == StandardLocation.SOURCE_PATH) {
            if (packageName.equals(pkg)) {
                return Collections.<JavaFileObject>singleton(jfo);
            }
            return Collections.emptyList();
        }
        throw new UnsupportedOperationException("Loc: " + location + " pkg: " + packageName + " kinds: " + kinds + " rec: " + recurse);
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file == jfo) {
            return pkg + "." + file.getName();
        }
        return delegate.inferBinaryName(location, file);
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean handleOption(String current, Iterator<String> remaining) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasLocation(Location location) {
        return true;
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int isSupportedOption(String option) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
        errors.add(diagnostic);
    }
    private static String findPkg(String java) throws IOException {
        Pattern p = Pattern.compile("package\\p{javaWhitespace}*([\\p{Alnum}\\.]+)\\p{javaWhitespace}*;", Pattern.MULTILINE);
        Matcher m = p.matcher(java);
        if (!m.find()) {
            throw new IOException("Can't find package declaration in the java file");
        }
        String pkg = m.group(1);
        return pkg;
    }
    private static String findCls(String java) throws IOException {
        Pattern p = Pattern.compile("class\\p{javaWhitespace}*([\\p{Alnum}\\.]+)\\p{javaWhitespace}", Pattern.MULTILINE);
        Matcher m = p.matcher(java);
        if (!m.find()) {
            throw new IOException("Can't find package declaration in the java file");
        }
        String cls = m.group(1);
        return cls;
    }
}
