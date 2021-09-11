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
import org.apidesign.vm4brwsr.ByteCodeParser.TypeArray;

abstract class AbstractStackMapper {
    private int uniqueVariableCounter;

    public abstract void clear();

    public abstract void syncWithFrameStack(final TypeArray frameStack);

    public final CharSequence pushI() {
        return pushT(VarType.INTEGER);
    }

    public final CharSequence pushL() {
        return pushT(VarType.LONG);
    }

    public final CharSequence pushF() {
        return pushT(VarType.FLOAT);
    }

    public final CharSequence pushD() {
        return pushT(VarType.DOUBLE);
    }

    public final CharSequence pushA() {
        return pushT(VarType.REFERENCE);
    }

    public abstract CharSequence pushT(final int type);

    abstract void assign(Appendable out, int varType, CharSequence s) throws IOException;
    
    abstract void replace(Appendable out, int varType, String format, CharSequence... arr)
    throws IOException;
    
    abstract void flush(Appendable out) throws IOException;
    
    public abstract boolean isDirty();
    
    public final CharSequence popI(Appendable out) throws IOException {
        return popT(out, VarType.INTEGER);
    }

    public final CharSequence popL(Appendable out) throws IOException {
        return popT(out, VarType.LONG);
    }

    public final CharSequence popF(Appendable out) throws IOException {
        return popT(out, VarType.FLOAT);
    }

    public final CharSequence popD(Appendable out) throws IOException {
        return popT(out, VarType.DOUBLE);
    }

    public final CharSequence popA(Appendable out) throws IOException {
        return popT(out, VarType.REFERENCE);
    }

    public abstract CharSequence popT(Appendable out, final int type) throws IOException;

    public abstract CharSequence popValue(Appendable out) throws IOException;

    public abstract Variable pop(Appendable out) throws IOException;

    public abstract void pop(Appendable out, final int count) throws IOException;

    public final CharSequence getI(Appendable out, final int indexFromTop) throws IOException {
        return getT(out, indexFromTop, VarType.INTEGER);
    }

    public final CharSequence getL(Appendable out, final int indexFromTop) throws IOException {
        return getT(out, indexFromTop, VarType.LONG);
    }

    public final CharSequence getF(Appendable out, final int indexFromTop) throws IOException {
        return getT(out, indexFromTop, VarType.FLOAT);
    }

    public final CharSequence getD(Appendable out, final int indexFromTop) throws IOException {
        return getT(out, indexFromTop, VarType.DOUBLE);
    }

    public final CharSequence getA(Appendable out, final int indexFromTop) throws IOException {
        return getT(out, indexFromTop, VarType.REFERENCE);
    }

    public final CharSequence getT(Appendable out, final int indexFromTop, final int type) throws IOException {
        return getT(out, indexFromTop, type, true);
    }
    public abstract CharSequence getT(Appendable out, final int indexFromTop, final int type, boolean clear) throws IOException;

    public abstract Variable get(Appendable out, final int indexFromTop) throws IOException;

    public static Variable getVariable(final int typeAndIndex) {
        final int type = typeAndIndex & 0xff;
        final int index = typeAndIndex >> 8;

        return Variable.getStackVariable(type, index);
    }

    abstract boolean alwaysUseGt();

    abstract int initCode(Appendable out) throws IOException;

    abstract void finishStatement(Appendable out) throws IOException;

    abstract void caughtException(Appendable out, String e) throws IOException;

    final String allocUniqueVariablePrefix() {
        return "t" + (uniqueVariableCounter++) + "_";
    }
}
