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
package org.apidesign.bck2brwsr.truffle;

import java.io.File;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.io.ByteSequence;

public class Main {
    public static void main(String... args) throws Exception {
        Context ctx = Context.newBuilder().allowAllAccess(true).build();
        String jarFile = null;
        String mainClass = null;
        for (int i = 0; i < args.length; i++) {
            if (isOption(args[i], "cp", "classpath")) {
                for (String element : args[++i].split(File.pathSeparator)) {
                    final File file = new File(element);
                    if (file.isFile()) {
                        Source src = Source.newBuilder("Java", file).mimeType("application/x-jar").build();
                        ctx.eval(src);
                    } else {
                        if (file.isDirectory()) {
                            Source src = Source.newBuilder("Java", ByteSequence.create(new byte[0]), file.getPath()).
                                mimeType("application/x-dir").
                                uri(file.toURI()).
                                build();
                            ctx.eval(src);
                        }
                    }
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
            if (jarFile == null && mainClass == null) {
                mainClass = args[i];
            }
            break;
        }

        if (jarFile == null && mainClass == null) {
            throw new IllegalArgumentException("Usage: -cp jar1:jar2:jar3 [-jar jarToExecute | mainClass ]");
        }

        if (jarFile != null) {
            Source src = Source.newBuilder("Java", new File(jarFile)).mimeType("application/x-jar").build();
            ctx.eval(src);
        } else {
            Value jvm = ctx.getPolyglotBindings().getMember("jvm");
            Value clazz = jvm.getMember(mainClass);
            clazz.invokeMember("main", (Object[]) new String[0]);
        }
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
