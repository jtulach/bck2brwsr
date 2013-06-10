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
package org.apidesign.bck2brwsr.launcher.fximpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/** 
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
public abstract class JsClassLoader extends URLClassLoader {
    JsClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        URL u = findResource(name.replace('.', '/') + ".class");
        if (u != null) {
            InputStream is = null;
            try {
                is = u.openStream();
                ClassReader cr = new ClassReader(is);
                ClassWriter w = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                FindInClass fic = new FindInClass(w);
                cr.accept(fic, 0);
                byte[] arr = w.toByteArray();
                return defineClass(name, arr, 0, arr.length);
            } catch (IOException ex) {
                throw new ClassNotFoundException("Can't load " + name, ex);
            } finally {
                try {
                    if (is != null) is.close();
                } catch (IOException ex) {
                    throw new ClassNotFoundException(null, ex);
                }
            }
        }
        if (name.startsWith("org.apidesign.bck2brwsr.launcher.fximpl.JsClassLoader")) {
            return Class.forName(name);
        }
        
        return super.findClass(name);
    }
    
    public final Fn define(String code, String... names) {
        return defineFn(code, names);
    }
    

    protected abstract Fn defineFn(String code, String... names);
    
    public static abstract class Fn {
        public abstract Object invoke(Object... args) throws Exception;
    }
    
    
    private static final class FindInClass extends ClassVisitor {
        private String name;
        
        public FindInClass(ClassVisitor cv) {
            super(Opcodes.ASM4, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.name = name;
            super.visit(version, access, name, signature, superName, interfaces);
        }
        
        

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new FindInMethod(name,
                super.visitMethod(access, name, desc, signature, exceptions)
            );
        }
        
        private final class FindInMethod extends MethodVisitor {
            private final String name;
            private List<String> args;
            private String body;
            
            public FindInMethod(String name, MethodVisitor mv) {
                super(Opcodes.ASM4, mv);
                this.name = name;
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if ("Lorg/apidesign/bck2brwsr/core/JavaScriptBody;".equals(desc)) { // NOI18N
                    return new FindInAnno();
                }
                return super.visitAnnotation(desc, visible);
            }

            private void generateJSBody(List<String> args, String body) {
                this.args = args;
                this.body = body;
            }
            
            @Override
            public void visitCode() {
                if (body == null) {
                    return;
                } 
                
                super.visitFieldInsn(
                    Opcodes.GETSTATIC, FindInClass.this.name, 
                    "$$bck2brwsr$$" + name, 
                    "Lorg/apidesign/bck2brwsr/launcher/fximpl/JsClassLoader$Fn;"
                );
                super.visitInsn(Opcodes.DUP);
                Label ifNotNull = new Label();
                super.visitJumpInsn(Opcodes.IFNONNULL, ifNotNull);
                
                // init Fn
                super.visitInsn(Opcodes.POP);
                super.visitLdcInsn(Type.getObjectType(FindInClass.this.name));
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                    "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;"
                );
                super.visitTypeInsn(Opcodes.CHECKCAST, "org/apidesign/bck2brwsr/launcher/fximpl/JsClassLoader");
                super.visitLdcInsn(body);
                super.visitIntInsn(Opcodes.SIPUSH, args.size());
                super.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/String");
                for (int i = 0; i < args.size(); i++) {
                    String name = args.get(i);
                    super.visitInsn(Opcodes.DUP);
                    super.visitIntInsn(Opcodes.BIPUSH, i);
                    super.visitLdcInsn(name);
                    super.visitInsn(Opcodes.AASTORE);
                }
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                    "org/apidesign/bck2brwsr/launcher/fximpl/JsClassLoader",
                    "define", "(Ljava/lang/String;[Ljava/lang/String;)Lorg/apidesign/bck2brwsr/launcher/fximpl/JsClassLoader$Fn;"
                );
                // end of Fn init
                
                super.visitLabel(ifNotNull);
                super.visitIntInsn(Opcodes.SIPUSH, args.size());
                super.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                    "org/apidesign/bck2brwsr/launcher/fximpl/JsClassLoader$Fn", "invoke", "([Ljava/lang/Object;)Ljava/lang/Object;"
                );
                super.visitInsn(Opcodes.ARETURN);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                if (body != null) {
                    FindInClass.this.visitField(
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, 
                        "$$bck2brwsr$$" + name, 
                        "Lorg/apidesign/bck2brwsr/launcher/fximpl/JsClassLoader$Fn;", 
                        null, null
                    );
                }
            }
            
            
            
            
        
            private final class FindInAnno extends AnnotationVisitor {
                private List<String> args = new ArrayList<String>();
                private String body;

                public FindInAnno() {
                    super(Opcodes.ASM4);
                }

                @Override
                public void visit(String name, Object value) {
                    if (name == null) {
                        args.add((String) value);
                        return;
                    }
                    assert name.equals("body");
                    body = (String) value;
                }

                @Override
                public AnnotationVisitor visitArray(String name) {
                    return this;
                }

                @Override
                public void visitEnd() {
                    if (body != null) {
                        generateJSBody(args, body);
                    }
                }
            }
        }
    }
}
