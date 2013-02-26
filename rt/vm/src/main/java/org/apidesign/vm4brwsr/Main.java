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
import java.io.Writer;

/** Generator of JavaScript from bytecode of classes on classpath of the VM
 * with a Main method.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class Main {
    private Main() {}
    
    public static void main(String... args) throws IOException {
        if (args.length < 2) {
            System.err.println("Bck2Brwsr Translator from Java(tm) to JavaScript, (c) Jaroslav Tulach 2012");
            System.err.println("Usage: java -cp ... -jar ... <file_to_generate_js_code_to> java/lang/Class org/your/App ...");
            return;
        }
        
        Writer w = new BufferedWriter(new FileWriter(args[0]));
        StringArray classes = StringArray.asList(args);
        classes.delete(0);
        try {
            Bck2Brwsr.generate(w, Main.class.getClassLoader(),
                               classes.toArray());
        } finally {
            w.close();
        }
    }
}
