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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
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

    static Set<String> findMainClass(File jarOrDir) throws IOException {
        final TreeSet<String> classes = new TreeSet<>();
        Iterable<Path> roots;
        if (jarOrDir.isDirectory()) {
            roots = Collections.singleton(jarOrDir.toPath());
        } else {
            try (JarFile jf = new JarFile(jarOrDir)) {
                Attributes attr = jf.getManifest().getMainAttributes();
                String mc = attr.getValue("Main-Class");
                if (mc != null) {
                    classes.add(mc);
                    return classes;
                }
            } catch (IOException ex) {
                // go on
            }
            FileSystem fs;
            try {
                fs = FileSystems.newFileSystem(new URI("jar:" + jarOrDir.toURI()), new HashMap<>(), null);
            } catch (URISyntaxException ex) {
                throw new IOException(ex);
            }
            roots = fs.getRootDirectories();
        }

        for (Path r : roots) {
            Files.walkFileTree(r, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    UtilAsm.visitFile(file, classes);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        return classes;
    }

    private static void visitFile(Path file, Set<String> classes) throws IOException {
        if (!file.getFileName().toString().endsWith(".class")) {
            return;
        }
        try (InputStream is = Files.newInputStream(file)) {
            ClassReader r = new ClassReader(is);
            r.accept(new ClassVisitor(Opcodes.ASM9) {
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
                    if (name != null
                            && "main".equals(methodName)
                            && "([Ljava/lang/String;)V".equals(signature)) {
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
