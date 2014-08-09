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

import java.io.InputStream;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Opcodes.ASM4;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class InvokeDynamicTest {
    private static Class<?> invokeDynamicClass;
    
    @Test public void simpleDynamicInJava() throws Exception {
        Method m = invokeDynamicClass.getMethod("dynamicSay");
        Object ret = m.invoke(m);
        assertEquals(ret, "Hello from Dynamic!");
    }
    
    
/*    
    private static TestVM code;

    @BeforeClass
    public void compileTheCode() throws Exception {
        code = TestVM.compileClass(InvokeDynamic.class.getName());
    }

    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }

    private void assertExec(
            String msg, Class clazz, String method, Object expRes, Object... args
    ) throws Exception {
        code.assertExec(msg, clazz, method, expRes, args);
    }
 */

    //
    // the following code is inspired by 
    // https://code.google.com/p/indy-maven-plugin/
    // which I don't want to use, as it is not in a public repository
    //
    @BeforeClass 
    public static void prepareClass() throws Exception {
        InputStream input = InvokeDynamic.class.getResourceAsStream("InvokeDynamic.class");
        assertNotNull(input, "Class found");
        
        ClassReader reader = new ClassReader(input);
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
        input.close();
        final byte[] invokeDynamicBytes = writer.toByteArray();
        ClassLoader l = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.equals(InvokeDynamic.class.getName())) {
                    return defineClass(name, invokeDynamicBytes, 0, invokeDynamicBytes.length);
                }
                return super.loadClass(name);
            }
        };
        invokeDynamicClass = l.loadClass(InvokeDynamic.class.getName());
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
    
    
}
