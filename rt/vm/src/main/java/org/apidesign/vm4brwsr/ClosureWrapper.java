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

import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.SourceFile;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apidesign.bck2brwsr.core.ExtraJavaScript;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ExtraJavaScript(processByteCode = false, resource="")
final class ClosureWrapper extends CommandLineRunner {
    private static final String[] ARGS = { 
        "--compilation_level", 
        "SIMPLE_OPTIMIZATIONS", 
        "--js", "bck2brwsr-raw.js" 
        //, "--debug"
        //, "--formatting", "PRETTY_PRINT"
    };

    private final Bck2Brwsr config;

    private String compiledCode;
    private String externsCode;

    private ClosureWrapper(Appendable out,
                           String compilationLevel, Bck2Brwsr config) {
        super(
            generateArguments(compilationLevel),
            new PrintStream(new APS(out)), System.err
        );
        this.config = config;
    }

    @Override
    protected List<SourceFile> createInputs(List<String> files, boolean allowStdIn) throws FlagUsageException, IOException {
        if (files.size() != 1 || !"bck2brwsr-raw.js".equals(files.get(0))) {
            throw new IOException("Unexpected files: " + files);
        }
        return Collections.nCopies(
                   1,
                   SourceFile.fromGenerator(
                       "bck2brwsr-raw.js",
                       new SourceFile.Generator() {
                           @Override
                           public String getCode() {
                               return getCompiledCode();
                           }
                       }));
    }


    @Override
    protected List<SourceFile> createExterns()
            throws FlagUsageException, IOException {
        final List<SourceFile> externsFiles =
                new ArrayList<SourceFile>(super.createExterns());

        externsFiles.add(
                SourceFile.fromGenerator(
                        "bck2brwsr_externs.js",
                        new SourceFile.Generator() {
                            @Override
                            public String getCode() {
                                return getExternsCode();
                            }
                        }));
        return externsFiles;
    }

    private String getCompiledCode() {
        if (compiledCode == null) {
            StringBuilder sb = new StringBuilder();
            try {
                VM.compile(sb, config);
                compiledCode = sb.toString();
            } catch (IOException ex) {
                compiledCode = ex.getMessage();
            }
        }
        return compiledCode;
    }

    private String getExternsCode() {
        if (externsCode == null) {
            // need compiled code at this point
            getCompiledCode();

            final StringBuilder sb = new StringBuilder("function RAW() {};\n");
            for (final String extern: FIXED_EXTERNS) {
                sb.append("RAW.prototype.").append(extern).append(";\n");
            }
            externsCode = sb.toString();
        }
        return externsCode;
    }

    private static final class APS extends OutputStream {
        private final Appendable out;

        public APS(Appendable out) {
            this.out = out;
        }
        @Override
        public void write(int b) throws IOException {
            out.append((char)b);
        }
    }

    private static String[] generateArguments(String compilationLevel) {
        String[] finalArgs = ARGS.clone();
        finalArgs[1] = compilationLevel;

        return finalArgs;
    }

    static int produceTo(Appendable output,
        ObfuscationLevel obfuscationLevel,
        Bck2Brwsr config
    ) throws IOException {
        final ClosureWrapper cw =
                new ClosureWrapper(output,
                                   (obfuscationLevel == ObfuscationLevel.FULL)
                                           ? "ADVANCED_OPTIMIZATIONS"
                                           : "SIMPLE_OPTIMIZATIONS",
                                   config);
        try {
            return cw.doRun();
        } catch (FlagUsageException ex) {
            throw new IOException(ex);
        }
    }

    private static final String[] FIXED_EXTERNS = {
        "bck2brwsr",
        "bck2BrwsrThrwrbl",
        "registerExtension",
        "$class",
        "anno",
        "array",
        "access",
        "cls",
        "vm",
        "loadClass",
        "loadBytes",
        "jvmName",
        "primitive",
        "superclass",
        "cnstr",
        "add32",
        "sub32",
        "mul32",
        "neg32",
        "toInt8",
        "toInt16",
        "next32",
        "high32",
        "toInt32",
        "toFP",
        "toLong",
        "toJS",
        "toExactString",
        "add64",
        "sub64",
        "mul64",
        "and64",
        "or64",
        "xor64",
        "shl64",
        "shr64",
        "ushr64",
        "compare",
        "compare64",
        "neg64",
        "div32",
        "mod32",
        "div64",
        "mod64",
        "at",
        "getClass__Ljava_lang_Class_2",
        "clone__Ljava_lang_Object_2"
    };
}
