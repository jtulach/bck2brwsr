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

import org.apidesign.vm4brwsr.ByteCodeParser.TypeArray;

final class StackMapper {
    private final TypeArray stackTypeIndexPairs;
    private int[] typeCounters;
    private int[] typeMaxCounters;

    public StackMapper() {
        stackTypeIndexPairs = new TypeArray();
        typeCounters = new int[VarType.LAST + 1];
        typeMaxCounters = new int[VarType.LAST + 1];
    }

    public void clear() {
        for (int type = 0; type <= VarType.LAST; ++type) {
            typeCounters[type] = 0;
        }
        stackTypeIndexPairs.clear();
    }

    public void syncWithFrameStack(final TypeArray frameStack) {
        clear();

        final int size = frameStack.getSize();
        for (int i = 0; i < size; ++i) {
            pushTypeImpl(VarType.fromStackMapType(frameStack.get(i)));
        }
    }

    public Variable pushI() {
        return pushT(VarType.INTEGER);
    }

    public Variable pushL() {
        return pushT(VarType.LONG);
    }

    public Variable pushF() {
        return pushT(VarType.FLOAT);
    }

    public Variable pushD() {
        return pushT(VarType.DOUBLE);
    }

    public Variable pushA() {
        return pushT(VarType.REFERENCE);
    }

    public Variable pushT(final int type) {
        return getVariable(pushTypeImpl(type));
    }

    public Variable popI() {
        return popT(VarType.INTEGER);
    }

    public Variable popL() {
        return popT(VarType.LONG);
    }

    public Variable popF() {
        return popT(VarType.FLOAT);
    }

    public Variable popD() {
        return popT(VarType.DOUBLE);
    }

    public Variable popA() {
        return popT(VarType.REFERENCE);
    }

    public Variable popT(final int type) {
        final Variable variable = getT(0, type);
        popImpl(1);
        return variable;
    }

    public Variable pop() {
        final Variable variable = get(0);
        popImpl(1);
        return variable;
    }

    public void pop(final int count) {
        final int stackSize = stackTypeIndexPairs.getSize();
        if (count > stackSize) {
            throw new IllegalStateException("Stack underflow");
        }
        popImpl(count);
    }

    public Variable getI(final int indexFromTop) {
        return getT(indexFromTop, VarType.INTEGER);
    }

    public Variable getL(final int indexFromTop) {
        return getT(indexFromTop, VarType.LONG);
    }

    public Variable getF(final int indexFromTop) {
        return getT(indexFromTop, VarType.FLOAT);
    }

    public Variable getD(final int indexFromTop) {
        return getT(indexFromTop, VarType.DOUBLE);
    }

    public Variable getA(final int indexFromTop) {
        return getT(indexFromTop, VarType.REFERENCE);
    }

    public Variable getT(final int indexFromTop, final int type) {
        final int stackSize = stackTypeIndexPairs.getSize();
        if (indexFromTop >= stackSize) {
            throw new IllegalStateException("Stack underflow");
        }
        final int stackValue =
                stackTypeIndexPairs.get(stackSize - indexFromTop - 1);
        if ((stackValue & 0xff) != type) {
            throw new IllegalStateException("Type mismatch");
        }

        return getVariable(stackValue);
    }

    public Variable get(final int indexFromTop) {
        final int stackSize = stackTypeIndexPairs.getSize();
        if (indexFromTop >= stackSize) {
            throw new IllegalStateException("Stack underflow");
        }
        final int stackValue =
                stackTypeIndexPairs.get(stackSize - indexFromTop - 1);

        return getVariable(stackValue);
    }

    private int pushTypeImpl(final int type) {
        final int count = typeCounters[type];
        final int value = (count << 8) | (type & 0xff);
        incCounter(type);
        stackTypeIndexPairs.add(value);

        return value;
    }

    private void popImpl(final int count) {
        final int stackSize = stackTypeIndexPairs.getSize();
        for (int i = stackSize - count; i < stackSize; ++i) {
            final int value = stackTypeIndexPairs.get(i);
            decCounter(value & 0xff);
        }

        stackTypeIndexPairs.setSize(stackSize - count);
    }

    private void incCounter(final int type) {
        final int newValue = ++typeCounters[type];
        if (typeMaxCounters[type] < newValue) {
            typeMaxCounters[type] = newValue;
        }
    }

    private void decCounter(final int type) {
        --typeCounters[type];
    }

    public Variable getVariable(final int typeAndIndex) {
        final int type = typeAndIndex & 0xff;
        final int index = typeAndIndex >> 8;

        return Variable.getStackVariable(type, index);
    }
}
