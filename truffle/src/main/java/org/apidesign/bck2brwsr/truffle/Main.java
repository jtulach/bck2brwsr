/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.truffle;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import java.io.File;

public class Main {
    public static void main(String... args) throws Exception {
        PolyglotEngine engine = PolyglotEngine.newBuilder().build();
        String jarFile = null;
        for (int i = 0; i < args.length; i++) {
            if (isOption(args[i], "cp", "classpath")) {
                for (String element : args[++i].split(File.pathSeparator)) {
                    Source src = Source.newBuilder(new File(element)).mimeType("application/x-jar").build();
                    engine.eval(src);
                }
                continue;
            }
            if (isOption(args[i], "jar")) {
                if (jarFile != null) {
                    throw new IllegalArgumentException("Only one executable JAR may be present.");
                }
                jarFile = args[++i];
                continue;
            }
            jarFile = null;
            break;
        }

        if (jarFile == null) {
            throw new IllegalArgumentException("Usage: -cp jar1:jar2:jar3 -jar jarToExecute");
        }

        Source src = Source.newBuilder(new File(jarFile)).mimeType("application/x-jar").build();
        engine.eval(src);
    }

    private static boolean isOption(String arg, String... options) {
        for (String option : options) {
            if (("-" + option).equals(arg)) {
                return true;
            }
        }
        return false;
    }
}
