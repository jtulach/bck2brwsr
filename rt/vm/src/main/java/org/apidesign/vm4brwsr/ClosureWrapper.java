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
package org.apidesign.vm4brwsr;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.ComposeWarningsGuard;
import com.google.javascript.jscomp.DiagnosticGroup;
import com.google.javascript.jscomp.DiagnosticGroups;
import com.google.javascript.jscomp.FlagUsageException;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.WarningsGuard;
import java.io.ByteArrayOutputStream;
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
        "--output_wrapper", "(function() {%output%})(this);",
        "--js", "bck2brwsr-raw.js" 
        //, "--debug"
        //, "--formatting", "PRETTY_PRINT"
    };

    private final Bck2Brwsr config;

    private String compiledCode;
    private String externsCode;
    private IOException compilerError;

    private ClosureWrapper(Appendable out,
                           String compilationLevel, Bck2Brwsr config, PrintStream err) {
        super(
            generateArguments(compilationLevel),
            new PrintStream(new APS(out)), err
        );
        this.config = config;
    }

    @Override
    protected List<SourceFile> createInputs(List<FlagEntry<JsSourceType>> files, List<JsonFileSpec> jsonFiles, boolean allowStdIn, List<JsChunkSpec> jsChunkSpecs) throws IOException {
        if (files.size() != 1 || !"bck2brwsr-raw.js".equals(files.get(0).getValue())) {
            return super.createInputs(files, jsonFiles, allowStdIn, jsChunkSpecs);
        }
        return Collections.nCopies(1,
            SourceFile.fromCode("bck2brwsr-raw.js",getCompiledCode())
        );
    }

    @Override
    protected List<SourceFile> createExterns(CompilerOptions options) throws IOException {
        final List<SourceFile> externsFiles = new ArrayList<>(super.createExterns(options));
        externsFiles.add(SourceFile.fromCode(
            "bck2brwsr_externs.js", getExternsCode()
        ));
        return externsFiles;
    }

    private String getCompiledCode() {
        if (compiledCode == null) {
            StringBuilder sb = new StringBuilder();
            try {
                VM.compile(sb, null, config);
                compiledCode = sb.toString();
            } catch (IOException ex) {
                compilerError = ex;
                return "// Error: " + ex.getMessage();
            }
        }
        return compiledCode;
    }

    private String getExternsCode() {
        if (externsCode == null) {
            // need compiled code at this point
            getCompiledCode();

            final StringBuilder sb = new StringBuilder("function RAW() {};\n");
            sb.append("function bck2brwsr() {};\n");
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

    @Override
    protected CompilerOptions createOptions() {
        CompilerOptions opts = super.createOptions();
        opts.addWarningsGuard(new IgnoreRedeclaredVariablesAndNonConstructors());
        return opts;
    }

    static int produceTo(Appendable output,
        ObfuscationLevel obfuscationLevel,
        Bck2Brwsr config
    ) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(out);
        final ClosureWrapper cw = new ClosureWrapper(output,
            (obfuscationLevel == ObfuscationLevel.FULL) ? "ADVANCED_OPTIMIZATIONS" : "SIMPLE_OPTIMIZATIONS",
            config, err
        );
        try {
            int result = cw.doRun();
            if (cw.compilerError != null) {
                throw cw.compilerError;
            }
            if (result != 0) {
//                out.write("\n=====\n".getBytes("UTF-8"));
//                out.write(cw.getCompiledCode().getBytes("UTF-8"));
                throw new IOException(out.toString());
            }
            return result;
        } catch (FlagUsageException ex) {
            throw new IOException(out.toString(), ex);
        }
    }

    private static final String[] FIXED_EXTERNS = {
        "bck2brwsr",
        "bck2BrwsrThrwrbl",
        "register",
        "$class",
        "$lambda",
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
        "interfaces",
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
        "cons__V",
        "getClass__Ljava_lang_Class_2",
        "clone__Ljava_lang_Object_2"
            
        //
        // workarounding export errors in ko4j @ 1.0
        //
            
        , "observable"
        , "notify"
        , "valueHasMutated"
    };

    private static class IgnoreRedeclaredVariablesAndNonConstructors extends WarningsGuard {
        @Override
        protected boolean disables(DiagnosticGroup group) {
            return group == DiagnosticGroups.CHECK_VARIABLES;
        }

        @Override
        public CheckLevel level(JSError error) {
            if (DiagnosticGroups.CHECK_VARIABLES.matches(error)) {
                return CheckLevel.OFF;
            }
            if ("JSC_NOT_A_CONSTRUCTOR".equals(error.getType().key)) {
                return CheckLevel.OFF;
            }
            if ("JSC_UNREACHABLE_CODE".equals(error.getType().key)) {
                return CheckLevel.OFF;
            }
            return null;
        }
    }
}
