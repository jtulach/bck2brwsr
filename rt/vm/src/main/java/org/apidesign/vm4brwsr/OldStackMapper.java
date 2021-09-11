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
package org.apidesign.vm4brwsr;

import java.io.IOException;

final class OldStackMapper extends AbstractStackMapper {
    int counter;
    int flushed;
    
    OldStackMapper() {
    }

    @Override
    public void clear() {
    }

    @Override
    public void syncWithFrameStack(ByteCodeParser.TypeArray frameStack) {
    }

    @Override
    int initCode(Appendable out) throws IOException {
        out.append("    var gt = 0;\n");
        out.append("    var stack = [];\n");
        out.append("    X_0: for (;;) {\n");
        return 1;
    }

    @Override
    public CharSequence pushT(int type) {
        return Variable.getStackVariable(VarType.REFERENCE, ++counter);
    }

    @Override
    void assign(Appendable out, int varType, CharSequence s) throws IOException {
        out.append("stack.push(").append(s).append(");");
    }

    @Override
    void replace(Appendable out, int varType, String format, CharSequence... arr) throws IOException {
        out.append("stack[stack.length - 1] = ");
        ByteCodeToJavaScript.emitImpl(out, format, arr);
        out.append(";\n");
    }

    @Override
    void flush(Appendable out) throws IOException {
    }

    @Override
    void finishStatement(Appendable out) throws IOException {
        while (flushed < counter) {
            out.append("stack.push(").append(Variable.getStackVariable(VarType.REFERENCE, ++flushed)).append(");\n");
        }
        out.append("}\n");
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public CharSequence popT(Appendable out, int type) throws IOException {
        Variable v = Variable.getStackVariable(VarType.REFERENCE, ++counter);
        out.append("var ").append(v).append(" = stack.pop();\n");
        flushed = counter;
        return v;
    }

    @Override
    public CharSequence popValue(Appendable out) throws IOException {
        return pop(out);
    }

    @Override
    public Variable pop(Appendable out) throws IOException {
        Variable v = Variable.getStackVariable(VarType.REFERENCE, ++counter);
        out.append("var ").append(v).append(" = stack.pop();\n");
        flushed = counter;
        return v;
    }

    @Override
    public void pop(Appendable out, int count) throws IOException {
        while (count-- > 0) {
            out.append("stack.pop();");
        }
    }

    @Override
    public CharSequence getT(Appendable out, int indexFromTop, int type, boolean clear) throws IOException {
        Variable v = Variable.getStackVariable(VarType.REFERENCE, ++counter);
        out.append("var ").append(v).append(" = stack[stack.length - " + (indexFromTop + 1) + "];\n");
        flushed = counter;
        return v;
    }

    @Override
    public Variable get(Appendable out, int indexFromTop) throws IOException {
        Variable v = Variable.getStackVariable(VarType.REFERENCE, ++counter);
        out.append("var ").append(v).append(" = stack[stack.length - " + (indexFromTop + 1) + "];\n");
        flushed = counter;
        return v;
    }

    @Override
    boolean alwaysUseGt() {
        return true;
    }

    @Override
    void caughtException(Appendable out, String e) throws IOException {
        out.append("stack = [").append(e).append("];");
    }
}
