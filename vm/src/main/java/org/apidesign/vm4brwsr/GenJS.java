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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/** Generator of JavaScript from bytecode of classes on classpath of the VM.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class GenJS extends ByteCodeToJavaScript {
    public GenJS(Appendable out) {
        super(out);
    }
    
    static void compile(Appendable out, String... names) throws IOException {
        compile(out, StringArray.asList(names));
    }
    static void compile(ClassLoader l, Appendable out, String... names) throws IOException {
        compile(l, out, StringArray.asList(names));
    }
    static void compile(Appendable out, StringArray names) throws IOException {
        compile(GenJS.class.getClassLoader(), out, names);
    }
    static void compile(ClassLoader l, Appendable out, StringArray names) throws IOException {
        new GenJS(out).doCompile(l, names);
    }
    protected void doCompile(ClassLoader l, StringArray names) throws IOException {
        out.append("(function VM(global) {");
        out.append("\n  var vm = {};");
        StringArray processed = new StringArray();
        StringArray initCode = new StringArray();
        for (String baseClass : names.toArray()) {
            references.add(baseClass);
            for (;;) {
                String name = null;
                for (String n : references.toArray()) {
                    if (processed.contains(n)) {
                        continue;
                    }
                    name = n;
                }
                if (name == null) {
                    break;
                }
                InputStream is = loadClass(l, name);
                if (is == null) {
                    throw new IOException("Can't find class " + name); 
                }
                try {
                    String ic = compile(is);
                    processed.add(name);
                    initCode.add(ic == null ? "" : ic);
                } catch (RuntimeException ex) {
                    if (out instanceof CharSequence) {
                        CharSequence seq = (CharSequence)out;
                        int lastBlock = seq.length();
                        while (lastBlock-- > 0) {
                            if (seq.charAt(lastBlock) == '{') {
                                break;
                            }
                        }
                        throw new IOException("Error while compiling " + name + "\n" 
                            + seq.subSequence(lastBlock + 1, seq.length()), ex
                        );
                    } else {
                        throw new IOException("Error while compiling " + name + "\n" 
                            + out, ex
                        );
                    }
                }
            }

            for (String resource : scripts.toArray()) {
                while (resource.startsWith("/")) {
                    resource = resource.substring(1);
                }
                InputStream emul = l.getResourceAsStream(resource);
                if (emul == null) {
                    throw new IOException("Can't find " + resource);
                }
                readResource(emul, out);
            }
            scripts = new StringArray();
            
            StringArray toInit = StringArray.asList(references.toArray());
            toInit.reverse();

            for (String ic : toInit.toArray()) {
                int indx = processed.indexOf(ic);
                if (indx >= 0) {
                    out.append(initCode.toArray()[indx]).append("\n");
                    initCode.toArray()[indx] = "";
                }
            }
        }
        out.append(
              "  global.bck2brwsr = function() { return {\n"
            + "    loadClass : function(name) { return vm[name](true); }\n"
            + "  };\n};\n");
        out.append("}(this));");
    }
    private static void readResource(InputStream emul, Appendable out) throws IOException {
        try {
            int state = 0;
            for (;;) {
                int ch = emul.read();
                if (ch == -1) {
                    break;
                }
                if (ch < 0 || ch > 255) {
                    throw new IOException("Invalid char in emulation " + ch);
                }
                switch (state) {
                    case 0: 
                        if (ch == '/') {
                            state = 1;
                        } else {
                            out.append((char)ch);
                        }
                        break;
                    case 1:
                        if (ch == '*') {
                            state = 2;
                        } else {
                            out.append('/').append((char)ch);
                            state = 0;
                        }
                        break;
                    case 2:
                        if (ch == '*') {
                            state = 3;
                        }
                        break;
                    case 3:
                        if (ch == '/') {
                            state = 0;
                        } else {
                            state = 2;
                        }
                        break;
                }
            }
        } finally {
            emul.close();
        }
    }

    private static InputStream loadClass(ClassLoader l, String name) throws IOException {
        Enumeration<URL> en = l.getResources(name + ".class");
        URL u = null;
        while (en.hasMoreElements()) {
            u = en.nextElement();
        }
        if (u == null) {
            throw new IOException("Can't find " + name);
        }
        if (u.toExternalForm().contains("rt.jar!")) {
            throw new IOException("No emulation for " + u);
        }
        return u.openStream();
    }

    static String toString(String name) throws IOException {
        StringBuilder sb = new StringBuilder();
        compile(sb, name);
        return sb.toString().toString();
    }

    private StringArray scripts = new StringArray();
    private StringArray references = new StringArray();
    
    @Override
    protected boolean requireReference(String cn) {
        if (references.contains(cn)) {
            return false;
        }
        references.add(cn);
        return true;
    }

    @Override
    protected void requireScript(String resourcePath) {
        scripts.add(resourcePath);
    }

    @Override
    String assignClass(String className) {
        return "vm." + className + " = ";
    }
    
    @Override
    String accessClass(String className) {
        return "vm." + className;
    }
}
