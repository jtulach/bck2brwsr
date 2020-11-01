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
package org.apidesign.bck2brwsr.aot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.orfjackal.retrolambda.ClassAnalyzer;
import net.orfjackal.retrolambda.Transformers;
import net.orfjackal.retrolambda.asm.ClassReader;
import net.orfjackal.retrolambda.asm.Opcodes;
import static net.orfjackal.retrolambda.asm.Opcodes.H_INVOKESPECIAL;
import net.orfjackal.retrolambda.asm.Type;
import net.orfjackal.retrolambda.files.OutputDirectory;
import net.orfjackal.retrolambda.interfaces.MethodRef;
import net.orfjackal.retrolambda.lambdas.LambdaClassDumper;
import net.orfjackal.retrolambda.lambdas.LambdaClassSaver;
import org.apidesign.bck2brwsr.core.ExtraJavaScript;
import org.apidesign.vm4brwsr.Bck2Brwsr;

/**
 *
 * @author Jaroslav Tulach
 */
@ExtraJavaScript(processByteCode = false, resource="")
final class RetroLambda extends OutputDirectory implements BytecodeProcessor {
    private Map<String,byte[]> converted;
    private final Transformers transformers;
    private final LambdaClassSaver saver;
    private final ClassAnalyzer analyzer = new ClassAnalyzer() {
        @Override
        protected String companionClassName(String name) {
            return name;
        }

    /*
        @Override
        public Optional<Type> getCompanionClass(Type type) {
            return Optional.empty();
        }

        @Override
        public MethodRef getMethodCallTarget(MethodRef original) {
            if (original.tag == H_INVOKESPECIAL) {
                // change Interface.super.defaultMethod() calls to static calls on the companion class
                MethodRef impl = getMethodDefaultImplementation(original);
                if (impl != null) {
                    return impl;
                }
            }
            return original;
        }
    */
    };

    public RetroLambda() {
        super(null);
        transformers = new Transformers(Opcodes.V1_7, false, analyzer);
        saver = new LambdaClassSaver(this, transformers, true);
    }

    @Override
    public void writeClass(byte[] bytecode, boolean isJavacHacksEnabled) throws IOException {
        writeFile(null, bytecode);
    }

    @Override
    public void writeFile(Path relativePath, byte[] bytecode) throws IOException {
        if (bytecode == null) {
            return;
        }
        ClassReader cr = new ClassReader(bytecode);
        String className = cr.getClassName();
        putBytecode(className + ".class", bytecode);
    }

    private void putBytecode(String className, byte[] backportedBytecode) {
        assert className.endsWith(".class") : "Full resource: " + className;
        if (converted == null) {
            converted = new HashMap<>();
        }
        converted.put(className, backportedBytecode);
    }

    @Override
    public Map<String, byte[]> process(
        String className, byte[] byteCode, Bck2Brwsr.Resources resources
    ) throws IOException {
        int minor = byteCode[4] << 8 | byteCode[5];
        int major = byteCode[6] << 8 | byteCode[7];
        if (major <= 51) {
            return null;
        }

        ClassLoader prev = Thread.currentThread().getContextClassLoader();
        try (LambdaClassDumper dumper = new LambdaClassDumper(saver)) {
            Thread.currentThread().setContextClassLoader(new ResLdr(resources));
            dumper.install();

            analyzer.analyze(new ClassReader(byteCode));
            byte[] newB = transformers.backportClass(new ClassReader(byteCode));
            if (!Arrays.equals(newB, byteCode)) {
                putBytecode(className, newB);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(prev);
        }

        Map<String, byte[]> ret = converted;
        converted = null;
        return ret;
    }

    private static final class ResLdr extends ClassLoader {
        private final Bck2Brwsr.Resources res;

        public ResLdr(Bck2Brwsr.Resources res) {
            this.res = res;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            Class<?> c = findLoadedClass(name);
            if (c != null) {
                return c;
            }
            if (name.startsWith("java.")) {
                return super.loadClass(name);
            }
            String r = name.replace('.', '/') + ".class";
            try (InputStream is = res.get(r)) {
                if (is == null) {
                    return Class.forName(name);
                }
                byte[] arr = Bck2BrwsrJars.readFrom(is);
                return defineClass(name, arr, 0, arr.length);
            } catch (IOException e) {
                return super.loadClass(name);
            }
        }
    }
}
