/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.bck2brwsr.dew;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

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
