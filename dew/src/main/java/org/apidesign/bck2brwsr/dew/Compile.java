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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
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
final class Compile implements DiagnosticListener<JavaFileObject> {
    private final List<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<>();
    private final Map<String, byte[]> classes;
    private final String pkg;
    private final String cls;
    private final String html;

    private Compile(String html, String code) throws IOException {
        this.pkg = find("package", ';', code);
        this.cls = find("class", ' ', code);
        this.html = html;
        classes = compile(html, code);
    }

    /** Performs compilation of given HTML page and associated Java code
     */
    public static Compile create(String html, String code) throws IOException {
        return new Compile(html, code);
    }
    
    /** Checks for given class among compiled resources */
    public byte[] get(String res) {
        return classes.get(res);
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
    
    private Map<String, byte[]> compile(final String html, final String code) throws IOException {
        final ClassLoaderFileManager clfm = new ClassLoaderFileManager();
        final JavaFileObject file = clfm.createMemoryFileObject(
                ClassLoaderFileManager.convertFQNToResource(pkg.isEmpty() ? cls : pkg + "." + cls) + Kind.SOURCE.extension,
                Kind.SOURCE,
                code.getBytes());
        final JavaFileObject htmlFile = clfm.createMemoryFileObject(
            ClassLoaderFileManager.convertFQNToResource(pkg),
            Kind.OTHER,
            html.getBytes());

        JavaFileManager jfm = new ForwardingJavaFileManager<JavaFileManager>(clfm) {            
            @Override
            public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
                if (location == StandardLocation.SOURCE_PATH) {
                    if (packageName.equals(pkg)) {
                        return htmlFile;
                    }
                }                
                return null;
            }
        };

        final Boolean res = ToolProvider.getSystemJavaCompiler().getTask(null, jfm, this, /*XXX:*/Arrays.asList("-source", "1.7", "-target", "1.7"), null, Arrays.asList(file)).call();
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
    private static String find(String pref, char term, String java) throws IOException {
        int pkg = java.indexOf(pref);
        if (pkg != -1) {
            pkg += pref.length();
            while (Character.isWhitespace(java.charAt(pkg))) {
                pkg++;
            }
            int semicolon = java.indexOf(term, pkg);
            if (semicolon != -1) {
                String pkgName = java.substring(pkg, semicolon).trim();
                return pkgName;
            }
        }
        throw new IOException("Can't find " + pref + " declaration in the java file");
    }

    String getHtml() {
        String fqn = "'" + pkg + '.' + cls + "'";
        return html.replace("'${fqn}'", fqn);
    }
}
