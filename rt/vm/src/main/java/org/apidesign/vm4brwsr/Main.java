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
import java.io.File;
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
        final String obfuscate = "--obfuscatelevel";
        
        if (args.length < 2) {
            System.err.println("Bck2Brwsr Translator from Java(tm) to JavaScript, (c) Jaroslav Tulach 2012");
            System.err.println("Usage: java -cp ... -jar ... [");
            System.err.print(obfuscate);
            System.err.print(" [");
            boolean first = true;
            for (ObfuscationLevel l : ObfuscationLevel.values()) {
                if (!first) {
                    System.err.print('|');
                }
                System.err.print(l.name());
                first = false;
            }
                
            System.err.println("] <file_to_generate_js_code_to> java/lang/Class org/your/App ...");
            System.exit(9);
        }
        
        ObfuscationLevel obfLevel = ObfuscationLevel.NONE;
        StringArray classes = new StringArray();
        String generateTo = null;
        for (int i = 0; i < args.length; i++) {
            if (obfuscate.equals(args[i])) { // NOI18N
                i++;
                try {
                    obfLevel = ObfuscationLevel.valueOf(args[i]);
                } catch (Exception e) {
                    System.err.print(obfuscate);
                    System.err.print(" parameter needs to be followed by one of ");
                    boolean first = true;
                    for (ObfuscationLevel l : ObfuscationLevel.values()) {
                        if (!first) {
                            System.err.print(", ");
                        }
                        System.err.print(l.name());
                        first = false;
                    }
                    System.err.println();
                    System.exit(1);
                }
                continue;
            }
            if (generateTo == null) {
                generateTo = args[i];
            } else {
                classes = classes.addAndNew(args[i]);
            }
        }
        
        File gt = new File(generateTo);
        if (Boolean.getBoolean("skip.if.exists") && gt.isFile()) {
            System.err.println("Skipping as " + gt + " exists.");
            System.exit(0);
        }
        
        try (Writer w = new BufferedWriter(new FileWriter(gt))) {
            Bck2Brwsr.newCompiler().
                obfuscation(obfLevel).
                addRootClasses(classes.toArray()).
                resources(new LdrRsrcs(Main.class.getClassLoader(), true)).
                generate(w);
        }
    }
}
