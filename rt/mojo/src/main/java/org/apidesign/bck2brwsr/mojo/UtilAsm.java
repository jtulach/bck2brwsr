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
package org.apidesign.bck2brwsr.mojo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

final class UtilAsm {
    private UtilAsm() {
    }

    static Set<String> findMainClass(File jar) throws IOException {
        final TreeSet<String> classes = new TreeSet<>();
        try (JarFile jf = new JarFile(jar)) {
            Attributes attr = jf.getManifest().getMainAttributes();
            String mc = attr.getValue("Main-Class");
            if (mc != null) {
                classes.add(mc);
                return classes;
            }

            Enumeration<JarEntry> en = jf.entries();
            while (en.hasMoreElements()) {
                JarEntry entry = en.nextElement();
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }
                try (InputStream is = jf.getInputStream(entry)) {
                    ClassReader r = new ClassReader(is);
                    r.accept(new ClassVisitor(Opcodes.ASM5) {
                        private String name;

                        @Override
                        public void visit(int i, int i1, String jvmName, String string1, String string2, String[] strings) {
                            name = jvmName.replace('/', '.');
                        }
                        
                        @Override
                        public void visitSource(String string, String string1) {
                        }
                        
                        @Override
                        public void visitOuterClass(String string, String string1, String string2) {
                        }

                        @Override
                        public AnnotationVisitor visitAnnotation(String string, boolean bln) {
                            return null;
                        }

                        @Override
                        public void visitAttribute(Attribute atrbt) {
                        }
                        
                        @Override
                        public void visitInnerClass(String string, String string1, String string2, int i) {
                        }

                        @Override
                        public FieldVisitor visitField(int i, String string, String string1, String string2, Object o) {
                            return null;
                        }

                        @Override
                        public MethodVisitor visitMethod(int mod, String methodName, String signature, String string2, String[] strings) {
                            if (
                                name != null &&
                                "main".equals(methodName) &&
                                "([Ljava/lang/String;)V".equals(signature)
                            ) {
                                classes.add(name);
                            }
                            return null;
                        }

                        @Override
                        public void visitEnd() {
                        }
                    }, Opcodes.V1_8);
                }
            }
        }
        return classes;
    }
}
