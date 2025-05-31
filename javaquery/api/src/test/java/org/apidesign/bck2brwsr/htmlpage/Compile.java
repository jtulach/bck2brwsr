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
package org.apidesign.bck2brwsr.htmlpage;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.apidesign.bck2brwsr.core.ExtraJavaScript;

/**
 *
 * @author Jaroslav Tulach
 */
@ExtraJavaScript(processByteCode = false, resource = "")
final class Compile implements DiagnosticListener<JavaFileObject> {
    private final List<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<>();
    private final Map<String, byte[]> classes;
    private final String pkg;
    private final String cls;
    private final String html;

    private Compile(String html, String code) throws IOException {
        this.pkg = findPkg(code);
        this.cls = findCls(code);
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
        StandardJavaFileManager sjfm = ToolProvider.getSystemJavaCompiler().getStandardFileManager(this, null, null);

        final Map<String, ByteArrayOutputStream> class2BAOS = new HashMap<>();

        JavaFileObject file = new Mem(URI.create("mem://mem"), Kind.SOURCE, code);
        final JavaFileObject htmlFile = new Mem2(URI.create("mem://mem2"), Kind.OTHER, html);
        
        final URI scratch;
        try {
            scratch = new URI("mem://mem3");
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
        
        JavaFileManager jfm = new ForwardingJavaFileManagerImpl(sjfm, class2BAOS, scratch, htmlFile);

        ToolProvider.getSystemJavaCompiler().getTask(null, jfm, this, /*XXX:*/Arrays.asList("-source", "1.7", "-target", "1.7"), null, Arrays.asList(file)).call();

        Map<String, byte[]> result = new HashMap<>();

        for (Map.Entry<String, ByteArrayOutputStream> e : class2BAOS.entrySet()) {
            result.put(e.getKey(), e.getValue().toByteArray());
        }

        return result;
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

    String getHtml() {
        String fqn = "'" + pkg + '.' + cls + "'";
        return html.replace("'${fqn}'", fqn);
    }

    @ExtraJavaScript(processByteCode = false, resource = "")
    private class ForwardingJavaFileManagerImpl extends ForwardingJavaFileManager<JavaFileManager> {

        private final Map<String, ByteArrayOutputStream> class2BAOS;
        private final URI scratch;
        private final JavaFileObject htmlFile;

        public ForwardingJavaFileManagerImpl(JavaFileManager fileManager, Map<String, ByteArrayOutputStream> class2BAOS, URI scratch, JavaFileObject htmlFile) {
            super(fileManager);
            this.class2BAOS = class2BAOS;
            this.scratch = scratch;
            this.htmlFile = htmlFile;
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
            if (kind  == Kind.CLASS) {
                final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                
                class2BAOS.put(className.replace('.', '/') + ".class", buffer);
                return new Sibling(sibling.toUri(), kind, buffer);
            }
            
            if (kind == Kind.SOURCE) {
                return new Source(scratch/*sibling.toUri()*/, kind);
            }
            
            throw new IllegalStateException();
        }

            @Override
            public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
                if (location == StandardLocation.SOURCE_PATH) {
                    if (packageName.equals(pkg)) {
                        return htmlFile;
                    }
                }
                
                return null;
            }

      @ExtraJavaScript(processByteCode = false, resource = "")
      private class Sibling extends SimpleJavaFileObject {
            private final ByteArrayOutputStream buffer;

            public Sibling(URI uri, Kind kind, ByteArrayOutputStream buffer) {
                super(uri, kind);
                this.buffer = buffer;
            }

            @Override
            public OutputStream openOutputStream() throws IOException {
                return buffer;
            }
        }

      @ExtraJavaScript(processByteCode = false, resource = "")
      private class Source extends SimpleJavaFileObject {
            public Source(URI uri, Kind kind) {
                super(uri, kind);
            }
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
        }
    }

    @ExtraJavaScript(processByteCode = false, resource = "")
    private static class Mem extends SimpleJavaFileObject {

        private final String code;

        public Mem(URI uri, Kind kind, String code) {
            super(uri, kind);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return code;
        }
    }

    @ExtraJavaScript(processByteCode = false, resource = "")
    private static class Mem2 extends SimpleJavaFileObject {

        private final String html;

        public Mem2(URI uri, Kind kind, String html) {
            super(uri, kind);
            this.html = html;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return html;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return new ByteArrayInputStream(html.getBytes());
        }
    }
}
