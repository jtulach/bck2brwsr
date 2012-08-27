/*
Java 4 Browser Bytecode Translator
Copyright (C) 2012-2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. Look for COPYING file in the top folder.
If not, see http://opensource.org/licenses/GPL-2.0.
*/
package org.apidesign.java4browser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import static org.netbeans.modules.classfile.ByteCodes.*;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.modules.classfile.Code;
import org.netbeans.modules.classfile.Method;
import org.netbeans.modules.classfile.Parameter;

/** Translator of the code inside class files to JavaScript.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class ByteCodeToJavaScript {
    private final ClassFile jc;
    private final Appendable out;

    private ByteCodeToJavaScript(ClassFile jc, Appendable out) {
        this.jc = jc;
        this.out = out;
    }
    
    /** Converts a given class file to a JavaScript version.
     * @param fileName the name of the file we are reading
     * @param classFile input stream with code of the .class file
     * @param out a {@link StringBuilder} or similar to generate the output to
     * @throws IOException if something goes wrong during read or write or translating
     */
    public static void compile(String fileName, InputStream classFile, Appendable out) throws IOException {
        ClassFile jc = new ClassFile(classFile, true);
        ByteCodeToJavaScript compiler = new ByteCodeToJavaScript(jc, out);
        for (Method m : jc.getMethods()) {
            if (m.isStatic()) {
                compiler.generateStaticMethod(m);
            }
        }
    }
    private void generateStaticMethod(Method m) throws IOException {
        out.append("\nfunction ").append(
            jc.getName().getExternalName().replace('.', '_')
        ).append('_').append(
            m.getName()
        );
        out.append(m.getReturnType());
        List<Parameter> args = m.getParameters();
        for (Parameter t : args) {
            out.append(t.getDescriptor());
        }
        out.append('(');
        String space = "";
        for (int index = 0, i = 0; i < args.size(); i++) {
            out.append(space);
            out.append("arg").append(String.valueOf(index));
            space = ",";
            final String desc = args.get(i).getDescriptor();
            if ("D".equals(desc) || "J".equals(desc)) {
                index += 2;
            } else {
                index++;
            }
        }
        out.append(") {").append("\n  var ");
        final Code code = m.getCode();
        int len = code.getMaxLocals();
        space = "";
        for (int i = 0; i < len; i++) {
            out.append(space);
            out.append("var").append(String.valueOf(i));
            space = ",";
        }
        out.append(";\n  var stack = new Array(");
        out.append(Integer.toString(code.getMaxStack()));
        out.append(");\n");
        produceCode(code.getByteCodes());
        out.append("}");
    }

    private void produceCode(byte[] byteCodes) throws IOException {
        for (int i = 0; i < byteCodes.length; i++) {
            int prev = i;
            out.append("  ");
            final int c = (byteCodes[i] + 256) % 256;
            switch (c) {
                case bc_aload_0:
                case bc_iload_0:
                case bc_lload_0:
                case bc_fload_0:
                case bc_dload_0:
                    out.append("stack.push(arg0);");
                    break;
                case bc_aload_1:
                case bc_iload_1:
                case bc_lload_1:
                case bc_fload_1:
                case bc_dload_1:
                    out.append("stack.push(arg1);");
                    break;
                case bc_aload_2:
                case bc_iload_2:
                case bc_lload_2:
                case bc_fload_2:
                case bc_dload_2:
                    out.append("stack.push(arg2);");
                    break;
                case bc_aload_3:
                case bc_iload_3:
                case bc_lload_3:
                case bc_fload_3:
                case bc_dload_3:
                    out.append("stack.push(arg3);");
                    break;
                case bc_iload:
                case bc_lload:
                case bc_fload:
                case bc_dload:
                case bc_aload: {
                    final int indx = (byteCodes[++i] + 256) % 256;
                    out.append("stack.push(arg").append(indx + ");");
                    break;
                }
                case bc_iadd:
                case bc_ladd:
                case bc_fadd:
                case bc_dadd:
                    out.append("stack.push(stack.pop() + stack.pop());");
                    break;
                case bc_isub:
                case bc_lsub:
                case bc_fsub:
                case bc_dsub:
                    out.append("{ var tmp = stack.pop(); stack.push(stack.pop() - tmp); }");
                    break;
                case bc_imul:
                case bc_lmul:
                case bc_fmul:
                case bc_dmul:
                    out.append("stack.push(stack.pop() * stack.pop());");
                    break;
                case bc_idiv:
                case bc_ldiv:
                    out.append("{ var tmp = stack.pop(); stack.push(Math.floor(stack.pop() / tmp)); }");
                    break;
                case bc_fdiv:
                case bc_ddiv:
                    out.append("{ var tmp = stack.pop(); stack.push(stack.pop() / tmp); }");
                    break;
                case bc_ireturn:
                case bc_lreturn:
                case bc_freturn:
                case bc_dreturn:
                    out.append("return stack.pop();");
                    break;
                case bc_i2l:
                case bc_i2f:
                case bc_i2d:
                case bc_l2i:
                    // max int check?
                case bc_l2f:
                case bc_l2d:
                case bc_f2d:
                case bc_d2f:
                    out.append("/* number conversion */");
                    break;
                case bc_f2i:
                case bc_f2l:
                case bc_d2i:
                case bc_d2l:
                    out.append("stack.push(Math.floor(stack.pop()));");
                    break;
                case bc_i2b:
                case bc_i2c:
                case bc_i2s:
                    out.append("/* number conversion */");
                    break;
            }
            out.append(" /*");
            for (int j = prev; j <= i; j++) {
                out.append(" ");
                final int cc = (byteCodes[j] + 256) % 256;
                out.append(Integer.toString(cc));
            }
            out.append("*/\n");
        }
    }
}
