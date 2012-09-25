package org.apidesign.vm4brwsr;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
        Set<String> processed = new HashSet<String>();
        LinkedList<String> toProcess = new LinkedList<String>(names);
        for (;;) {
            toProcess.removeAll(processed);
            if (toProcess.isEmpty()) {
                break;
            }
            String name = toProcess.getFirst();
            processed.add(name);
            if (name.startsWith("java/") 
                && !name.equals("java/lang/Object")
                && !name.equals("java/lang/String")
            ) {
                continue;
            }
            InputStream is = GenJS.class.getClassLoader().getResourceAsStream(name + ".class");
            if (is == null) {
                throw new IOException("Can't find class " + name); 
            }
            try {
                ByteCodeToJavaScript.compile(is, out, toProcess);
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
        }
    }
    
}
