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

final class StackMapper extends AbstractStackMapper {
    private final TypeArray stackTypeIndexPairs;
    private final StringArray stackValues;
    private boolean dirty;

    public StackMapper() {
        stackTypeIndexPairs = new TypeArray();
        stackValues = new StringArray();
    }

    public void clear() {
        stackTypeIndexPairs.clear();
        stackValues.clear();
        dirty = false;
    }

    public void syncWithFrameStack(final TypeArray frameStack) {
        clear();

        final int size = frameStack.getSize();
        for (int i = 0; i < size; ++i) {
            pushTypeImpl(VarType.fromStackMapType(frameStack.get(i)));
        }
    }

    @Override
    public Variable pushT(final int type) {
        return getVariable(pushTypeImpl(type));
    }

    @Override
    void assign(Appendable out, int varType, CharSequence s) throws IOException {
        pushTypeAndValue(varType, s);
    }
    
    @Override
    void replace(Appendable out, int varType, String format, CharSequence... arr) 
    throws IOException {
        StringBuilder sb = new StringBuilder();
        ByteCodeToJavaScript.emitImpl(sb, format, arr);
        String[] values = stackValues.toArray();
        final int last = stackTypeIndexPairs.getSize() - 1;
        values[last] = sb.toString();
        dirty = true;
        final int value = (last << 8) | (varType & 0xff);
        stackTypeIndexPairs.set(last, value);
    }

    @Override
    void finishStatement(Appendable out) throws IOException {
    }
    
    @Override
    void flush(Appendable out) throws IOException {
        int count = stackTypeIndexPairs.getSize();
        for (int i = 0; i < count; i++) {
            String val = stackValues.getAndClear(i, true);
            if (val == null) {
                continue;
            }
            CharSequence var = getVariable(stackTypeIndexPairs.get(i));
            ByteCodeToJavaScript.emitImpl(out, "var @1 = @2;", var, val);
        }
        dirty = false;
    }
    
    @Override
    public boolean isDirty() {
        return dirty;
    }
    
    @Override
    public CharSequence popT(Appendable out, final int type) throws IOException {
        final CharSequence variable = getT(out, 0, type);
        popImpl(1);
        return variable;
    }

    @Override
    public CharSequence popValue(Appendable out) throws IOException {
        final CharSequence variable = getT(out, 0, -1);
        popImpl(1);
        return variable;
    }
    @Override
    public Variable pop(Appendable out) throws IOException {
        flush(out);
        final Variable variable = get(out, 0);
        popImpl(1);
        return variable;
    }

    @Override
    public void pop(Appendable out, final int count) {
        final int stackSize = stackTypeIndexPairs.getSize();
        if (count > stackSize) {
            throw new IllegalStateException("Stack underflow");
        }
        popImpl(count);
    }

    @Override
    public CharSequence getT(Appendable out, final int indexFromTop, final int type, boolean clear) {
        final int stackSize = stackTypeIndexPairs.getSize();
        if (indexFromTop >= stackSize) {
            throw new IllegalStateException("Stack underflow");
        }
        final int stackValue =
                stackTypeIndexPairs.get(stackSize - indexFromTop - 1);
        if (type != -1 && (stackValue & 0xff) != type) {
            throw new IllegalStateException("Type mismatch");
        }
        String value =
            stackValues.getAndClear(stackSize - indexFromTop - 1, clear);
        if (value != null) {
            return value;
        }
        return getVariable(stackValue);
    }

    @Override
    public Variable get(Appendable out, final int indexFromTop) {
        final int stackSize = stackTypeIndexPairs.getSize();
        if (indexFromTop >= stackSize) {
            throw new IllegalStateException("Stack underflow");
        }
        final int stackValue =
                stackTypeIndexPairs.get(stackSize - indexFromTop - 1);

        return getVariable(stackValue);
    }

    private int pushTypeImpl(final int type) {
        final int count = stackTypeIndexPairs.getSize();
        final int value = (count << 8) | (type & 0xff);
        stackTypeIndexPairs.add(value);
        
        addStackValue(count, null);
        return value;
    }

    private void pushTypeAndValue(final int type, CharSequence v) {
        final int count = stackTypeIndexPairs.getSize();
        final int value = (count << 8) | (type & 0xff);
        stackTypeIndexPairs.add(value);
        final String val = v.toString();
        addStackValue(count, val);
    }

    private void addStackValue(int at, final String val) {
        final String[] arr = stackValues.toArray();
        if (arr.length > at) {
            arr[at] = val;
        } else {
            stackValues.add(val);
        }
        dirty = true;
    }

    private void popImpl(final int count) {
        final int stackSize = stackTypeIndexPairs.getSize();
        stackTypeIndexPairs.setSize(stackSize - count);
    }

    @Override
    boolean alwaysUseGt() {
        return false;
    }

    @Override
    int initCode(Appendable out) throws IOException {
        return 0;
    }

    @Override
    void caughtException(Appendable out, String e) throws IOException {
        out.append("var stA0 = ").append(e).append(";");
    }
}
