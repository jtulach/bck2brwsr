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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

/** 
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
abstract class JsClassLoader extends ClassLoader {
    JsClassLoader(ClassLoader parent) {
        super(parent);
    }
    
    @Override
    protected abstract URL findResource(String name);
    
    @Override
    protected abstract Enumeration<URL> findResources(String name);

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        URL u = findResource(name.replace('.', '/') + ".class");
        if (u != null) {
            InputStream is = null;
            try {
                is = u.openStream();
                byte[] arr = new byte[is.available()];
                int len = 0;
                while (len < arr.length) {
                    int read = is.read(arr, len, arr.length - len);
                    if (read == -1) {
                        throw new IOException("Can't read " + u);
                    }
                    len += read;
                }
                is.close();
                is = null;
                ClassReader cr = new ClassReader(arr);
                FindInClass tst = new FindInClass(null);
                cr.accept(tst, 0);
                if (tst.found) {
                    ClassWriter w = new ClassWriterEx(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    FindInClass fic = new FindInClass(w);
                    cr.accept(fic, 0);
                    arr = w.toByteArray();
                }
                if (arr != null) {
                    return defineClass(name, arr, 0, arr.length);
                }
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
        if (name.startsWith("org.apidesign.bck2brwsr.launcher.fximpl.Fn")) {
            return Class.forName(name);
        }
        
        return super.findClass(name);
    }
    
    protected abstract Fn defineFn(String code, String... names);
    
    
    private static final class FindInClass extends ClassVisitor {
        private String name;
        private boolean found;
        
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
            return new FindInMethod(name, desc,
                super.visitMethod(access & (~Opcodes.ACC_NATIVE), name, desc, signature, exceptions)
            );
        }
        
        private final class FindInMethod extends MethodVisitor {
            private final String name;
            private final String desc;
            private List<String> args;
            private String body;
            private boolean bodyGenerated;
            
            public FindInMethod(String name, String desc, MethodVisitor mv) {
                super(Opcodes.ASM4, mv);
                this.name = name;
                this.desc = desc;
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if ("Lorg/apidesign/bck2brwsr/core/JavaScriptBody;".equals(desc)) { // NOI18N
                    found = true;
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
                generateBody();
            }
            
            private boolean generateBody() {
                if (bodyGenerated) {
                    return false;
                }
                bodyGenerated = true;
                
                super.visitFieldInsn(
                    Opcodes.GETSTATIC, FindInClass.this.name, 
                    "$$bck2brwsr$$" + name, 
                    "Lorg/apidesign/bck2brwsr/launcher/fximpl/Fn;"
                );
                super.visitInsn(Opcodes.DUP);
                Label ifNotNull = new Label();
                super.visitJumpInsn(Opcodes.IFNONNULL, ifNotNull);
                
                // init Fn
                super.visitInsn(Opcodes.POP);
                super.visitLdcInsn(Type.getObjectType(FindInClass.this.name));
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
                super.visitMethodInsn(Opcodes.INVOKESTATIC, 
                    "org/apidesign/bck2brwsr/launcher/fximpl/Fn", "define", 
                    "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/String;)Lorg/apidesign/bck2brwsr/launcher/fximpl/Fn;"
                );
                // end of Fn init
                
                super.visitLabel(ifNotNull);
                super.visitIntInsn(Opcodes.SIPUSH, args.size());
                super.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
                
                class SV extends SignatureVisitor {
                    private boolean nowReturn;
                    private Type returnType;
                    private int index;
                    
                    public SV() {
                        super(Opcodes.ASM4);
                    }
                    
                    @Override
                    public void visitBaseType(char descriptor) {
                        final Type t = Type.getType("" + descriptor);
                        if (nowReturn) {
                            returnType = t;
                            return;
                        }
                        FindInMethod.super.visitInsn(Opcodes.DUP);
                        FindInMethod.super.visitIntInsn(Opcodes.SIPUSH, index);
                        FindInMethod.super.visitVarInsn(t.getOpcode(Opcodes.ILOAD), index);
                        String factory;
                        switch (descriptor) {
                        case 'I': factory = "java/lang/Integer"; break;
                        case 'J': factory = "java/lang/Long"; break;
                        case 'S': factory = "java/lang/Short"; break;
                        case 'F': factory = "java/lang/Float"; break;
                        case 'D': factory = "java/lang/Double"; break;
                        case 'Z': factory = "java/lang/Boolean"; break;
                        case 'C': factory = "java/lang/Character"; break;
                        case 'B': factory = "java/lang/Byte"; break;
                        default: throw new IllegalStateException(t.toString());
                        }
                        FindInMethod.super.visitMethodInsn(Opcodes.INVOKESTATIC,
                            factory, "valueOf", "(" + descriptor + ")L" + factory + ";"
                        );
                        FindInMethod.super.visitInsn(Opcodes.AASTORE);
                        index++;
                    }

                    @Override
                    public void visitClassType(String name) {
                        if (nowReturn) {
                            returnType = Type.getObjectType(name);
                            return;
                        }
                        FindInMethod.super.visitInsn(Opcodes.DUP);
                        FindInMethod.super.visitIntInsn(Opcodes.SIPUSH, index);
                        FindInMethod.super.visitVarInsn(Opcodes.ALOAD, index);
                        FindInMethod.super.visitInsn(Opcodes.AASTORE);
                        index++;
                    }

                    @Override
                    public SignatureVisitor visitReturnType() {
                        nowReturn = true;
                        return this;
                    }
                    
                    
                }
                SV sv = new SV();
                SignatureReader sr = new SignatureReader(desc);
                sr.accept(sv);
                
                super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                    "org/apidesign/bck2brwsr/launcher/fximpl/Fn", "invoke", "([Ljava/lang/Object;)Ljava/lang/Object;"
                );
                switch (sv.returnType.getSort()) {
                case Type.VOID: 
                    super.visitInsn(Opcodes.RETURN);
                    break;
                case Type.ARRAY:
                case Type.OBJECT:
                    super.visitTypeInsn(Opcodes.CHECKCAST, sv.returnType.getInternalName());
                    super.visitInsn(Opcodes.ARETURN);
                    break;
                default:
                    super.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Number");
                    super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, 
                        "java/lang/Number", sv.returnType.getClassName() + "Value", "()" + sv.returnType.getDescriptor()
                    );
                    super.visitInsn(sv.returnType.getOpcode(Opcodes.IRETURN));
                }
                return true;
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
                if (body != null) {
                    if (generateBody()) {
                        // native method
                        super.visitMaxs(1, 0);
                    }
                    FindInClass.this.visitField(
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, 
                        "$$bck2brwsr$$" + name, 
                        "Lorg/apidesign/bck2brwsr/launcher/fximpl/Fn;", 
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
    
    private class ClassWriterEx extends ClassWriter {

        public ClassWriterEx(ClassReader classReader, int flags) {
            super(classReader, flags);
        }
        
        @Override
        protected String getCommonSuperClass(final String type1, final String type2) {
            Class<?> c, d;
            ClassLoader classLoader = JsClassLoader.this;
            try {
                c = Class.forName(type1.replace('/', '.'), false, classLoader);
                d = Class.forName(type2.replace('/', '.'), false, classLoader);
            } catch (Exception e) {
                throw new RuntimeException(e.toString());
            }
            if (c.isAssignableFrom(d)) {
                return type1;
            }
            if (d.isAssignableFrom(c)) {
                return type2;
            }
            if (c.isInterface() || d.isInterface()) {
                return "java/lang/Object";
            } else {
                do {
                    c = c.getSuperclass();
                } while (!c.isAssignableFrom(d));
                return c.getName().replace('.', '/');
            }
        }        
    }
}
