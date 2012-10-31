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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** Generator of JavaScript from bytecode of classes on classpath of the VM.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class GenJS {
    private GenJS() {}
    
    public static void main(String... args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java -cp ... -jar ... <file_to_generate_js_code_to> java/lang/Class org/your/App ...");
            return;
        }
        
        Writer w = new BufferedWriter(new FileWriter(args[0]));
        List<String> classes = Arrays.asList(args).subList(1, args.length);
        compile(w, classes);
        w.close();
    }
    
    static void compile(Appendable out, String... names) throws IOException {
        compile(out, Arrays.asList(names));
    }
    static void compile(Appendable out, List<String> names) throws IOException {
        compile(GenJS.class.getClassLoader(), out, names);
    }
    static void compile(ClassLoader l, Appendable out, List<String> names) throws IOException {
        final Map<String,String> processed = new HashMap<String, String>();
        for (String baseClass : names) {
            LinkedHashSet<String> toProcess = new LinkedHashSet<String>() {
                @Override
                public boolean add(String e) {
                    if (processed.containsKey(e)) {
                        return false;
                    }
                    return super.add(e);
                }
            };
            toProcess.add(baseClass);
            for (;;) {
                String name = null;
                Iterator<String> it = toProcess.iterator();
                while (it.hasNext() && name == null) {
                    String n = it.next();
                    if (processed.get(n) != null) {
                        continue;
                    }
                    name = n;
                }
                if (name == null) {
                    break;
                }
                if (name.startsWith("sun/")) {
                    processed.put(name, "");
                    continue;
                }            
                InputStream is = loadClass(l, name);
                if (is == null) {
                    throw new IOException("Can't find class " + name); 
                }
                LinkedList<String> scripts = new LinkedList<String>();
                try {
                    String initCode = ByteCodeToJavaScript.compile(is, out, toProcess, scripts);
                    processed.put(name, initCode == null ? "" : initCode);
                } catch (RuntimeException ex) {
                    if (out instanceof CharSequence) {
                        CharSequence seq = (CharSequence)out;
                        int lastBlock = seq.length();
                        while (lastBlock-- >= 0) {
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
                for (String resource : scripts) {
                    while (resource.startsWith("/")) {
                        resource = resource.substring(1);
                    }
                    InputStream emul = l.getResourceAsStream(resource);
                    if (emul == null) {
                        throw new IOException("Can't find " + resource);
                    }
                    readResource(emul, out);
                }
            }

            List<String> toInit = new ArrayList<String>(toProcess);
            Collections.reverse(toInit);

            for (String clazz : toInit) {
                String initCode = processed.get(clazz);
                if (initCode != null && !initCode.isEmpty()) {
                    out.append(initCode).append("\n");
                    processed.put(clazz, "");
                }
            }

        }
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
        return u.openStream();
    }
    
}
