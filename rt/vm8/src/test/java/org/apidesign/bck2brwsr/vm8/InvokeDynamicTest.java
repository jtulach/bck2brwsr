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
package org.apidesign.bck2brwsr.vm8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import org.apidesign.vm4brwsr.Bck2Brwsr;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Opcodes.ASM4;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class InvokeDynamicTest {
    private static Class<?> invokeDynamicClass;
    private static byte[] invokeDynamicBytes;
    private static TestVM code;
    
    @Test public void simpleDynamicInJava() throws Exception {
        Method m = invokeDynamicClass.getMethod("dynamicSay");
        Object ret = m.invoke(m);
        assertEquals(ret, "Hello from Dynamic!");
    }
    
    @Test public void simpleDynamicInJS() throws Exception {
        code.assertExec(
            "Invoke dynamic can return a value", InvokeDynamic.class,
            "dynamicSay__Ljava_lang_String_2",
            "Hello from Dynamic!"
        );
    }
    

    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }

    //
    // the following code is inspired by 
    // https://code.google.com/p/indy-maven-plugin/
    // which I don't want to use, as it is not in a public repository
    //
    @BeforeClass 
    public static void prepareClass() throws Exception {
        InputStream is = InvokeDynamic.class.getResourceAsStream("InvokeDynamic.class");
        assertNotNull(is, "Class found");
        
        ClassReader reader = new ClassReader(is) {
            @Override
            public short readShort(int index) {
                if (index == 6) {
                    return Opcodes.V1_7;
                }
                return super.readShort(index);
            }
        };
        ClassWriter writer = new ClassWriter(reader, 0);

        reader.accept(
                new ClassVisitor(ASM4, writer) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions
                    ) {
                        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                        return new InvokeDynamicProcessor(mv);
                    }
                },
                0);
        is.close();
        invokeDynamicBytes = writer.toByteArray();
        final boolean[] loaded = { false };
        ClassLoader l = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.equals(InvokeDynamic.class.getName())) {
                    loaded[0] = true;
                    return defineClass(name, invokeDynamicBytes, 0, invokeDynamicBytes.length);
                }
                return super.loadClass(name);
            }
        };
        invokeDynamicClass = l.loadClass(InvokeDynamic.class.getName());
        assertTrue(loaded[0], "InvokeDynamic class should be loaded!");

        final EmulResWithInvDyn emul = new EmulResWithInvDyn();
        code = TestVM.compileClass(
            null, null, emul,
            InvokeDynamic.class.getName().replace('.', '/')
        );
        
        assertTrue(emul.loaded, "InvokeDynamic class should be processed!");
    }
    
    
    private static class InvokeDynamicProcessor extends MethodVisitor {
        InvokeDynamicProcessor(MethodVisitor mv) {
            super(ASM4, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            if (opcode == INVOKESTATIC) {
                if (name.startsWith("TEST_dynamic_")) {
                    final String shortName = name.substring(13);
                    Handle mh = new Handle(
                        Opcodes.H_INVOKESTATIC, owner, shortName,
                        MethodType.methodType(
                            CallSite.class,
                            MethodHandles.Lookup.class,
                            String.class, 
                            MethodType.class
                        ).toMethodDescriptorString()
                    );
                    super.visitInvokeDynamicInsn(shortName, desc, mh);
                    return;
                }
            }
            super.visitMethodInsn(opcode, owner, name, desc);
        }
    }
    
    private static class EmulResWithInvDyn implements Bck2Brwsr.Resources {
        boolean loaded;
        
        @Override
        public InputStream get(String name) throws IOException {
            if ("org/apidesign/bck2brwsr/vm8/InvokeDynamic.class".equals(name)) {
                loaded = true;
                return new ByteArrayInputStream(invokeDynamicBytes);
            }
            Enumeration<URL> en = InvokeDynamicTest.class.getClassLoader().getResources(name);
            URL u = null;
            while (en.hasMoreElements()) {
                u = en.nextElement();
            }
            if (u == null) {
                throw new IOException("Can't find " + name);
            }
            if (u.toExternalForm().contains("rt.jar!")) {
                throw new IOException("No emulation for " + u);
            }
            return u.openStream();
        }
    }
    
}
