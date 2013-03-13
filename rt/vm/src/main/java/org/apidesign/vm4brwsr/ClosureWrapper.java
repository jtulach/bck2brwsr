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
import java.util.Collections;
import java.util.List;
import org.apidesign.bck2brwsr.core.ExtraJavaScript;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ExtraJavaScript(processByteCode = false, resource="")
final class ClosureWrapper extends CommandLineRunner implements SourceFile.Generator {
    private static final String[] ARGS = { "--compilation_level", "SIMPLE_OPTIMIZATIONS", "--js", "bck2brwsr-raw.js" };

    private String code;
    private final Bck2Brwsr.Resources res;
    private final StringArray classes;
    private ClosureWrapper(Appendable out, ObfuscationLevel obfuscationLevel,
                           Bck2Brwsr.Resources res, StringArray classes) {
        super(
            generateArguments(obfuscationLevel),
            new PrintStream(new APS(out)), System.err
        );
        this.res = res;
        this.classes = classes;
    }

    @Override
    protected List<SourceFile> createInputs(List<String> files, boolean allowStdIn) throws FlagUsageException, IOException {
        if (files.size() != 1 || !"bck2brwsr-raw.js".equals(files.get(0))) {
            throw new IOException("Unexpected files: " + files);
        }
        return Collections.nCopies(1, SourceFile.fromGenerator("bck2brwsr-raw.js", this));
    }

    @Override
    public String getCode() {
        if (code == null) {
            StringBuilder sb = new StringBuilder();
            try {
                VM.compile(res, sb, classes);
            } catch (IOException ex) {
                code = ex.getMessage();
            }
            code = sb.toString();
        }
        return code;
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

    private static String[] generateArguments(
            ObfuscationLevel obfuscationLevel) {
        String[] finalArgs = ARGS.clone();
        finalArgs[1] = obfuscationLevel.toString();

        return finalArgs;
    }

    static int produceTo(Appendable w, ObfuscationLevel obfuscationLevel, Bck2Brwsr.Resources resources, StringArray arr) throws IOException {
        ClosureWrapper cw = new ClosureWrapper(w, obfuscationLevel, resources,
                                               arr);
        try {
            return cw.doRun();
        } catch (FlagUsageException ex) {
            throw new IOException(ex);
        }
    }
}
