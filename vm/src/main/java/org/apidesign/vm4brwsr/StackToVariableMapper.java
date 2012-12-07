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

import org.apidesign.javap.TypeArray;

import static org.apidesign.javap.RuntimeConstants.ITEM_Bogus;
import static org.apidesign.javap.RuntimeConstants.ITEM_Integer;
import static org.apidesign.javap.RuntimeConstants.ITEM_Float;
import static org.apidesign.javap.RuntimeConstants.ITEM_Double;
import static org.apidesign.javap.RuntimeConstants.ITEM_Long;
import static org.apidesign.javap.RuntimeConstants.ITEM_Null;
import static org.apidesign.javap.RuntimeConstants.ITEM_InitObject;
import static org.apidesign.javap.RuntimeConstants.ITEM_Object;
import static org.apidesign.javap.RuntimeConstants.ITEM_NewObject;

public final class StackToVariableMapper {
    private final TypeArray stackTypeIndexPairs;
    private int[] typeCounters;
    private int[] typeMaxCounters;

    public StackToVariableMapper() {
        stackTypeIndexPairs = new TypeArray();
        typeCounters = new int[Variable.LAST_TYPE + 1];
        typeMaxCounters = new int[Variable.LAST_TYPE + 1];
    }

    public void clear() {
        for (int type = 0; type <= Variable.LAST_TYPE; ++type) {
            typeCounters[type] = 0;
        }
        stackTypeIndexPairs.clear();
    }

    public void syncWithFrameStack(final TypeArray frameStack) {
        clear();

        final int size = frameStack.getSize();
        for (int i = 0; i < size; ++i) {
            final int frameStackValue = frameStack.get(i);
            switch (frameStackValue & 0xff) {
                case ITEM_Integer:
                    pushTypeImpl(Variable.TYPE_INT);
                    break;
                case ITEM_Float:
                    pushTypeImpl(Variable.TYPE_FLOAT);
                    break;
                case ITEM_Double:
                    pushTypeImpl(Variable.TYPE_DOUBLE);
                    break;
                case ITEM_Long:
                    pushTypeImpl(Variable.TYPE_LONG);
                    break;
                case ITEM_Object:
                    pushTypeImpl(Variable.TYPE_REF);
                    break;

                case ITEM_Bogus:
                case ITEM_Null:
                case ITEM_InitObject:
                case ITEM_NewObject:
                    /* unclear how to handle for now */
                default:
                    throw new IllegalStateException(
                                  "Unhandled frame stack type");
            }
        }
    }

    public Variable pushI() {
        return pushT(Variable.TYPE_INT);
    }

    public Variable pushL() {
        return pushT(Variable.TYPE_LONG);
    }

    public Variable pushF() {
        return pushT(Variable.TYPE_FLOAT);
    }

    public Variable pushD() {
        return pushT(Variable.TYPE_DOUBLE);
    }

    public Variable pushA() {
        return pushT(Variable.TYPE_REF);
    }

    public Variable pushT(final int type) {
        return getVariable(pushTypeImpl(type));
    }

    public Variable popI() {
        return popT(Variable.TYPE_INT);
    }

    public Variable popL() {
        return popT(Variable.TYPE_LONG);
    }

    public Variable popF() {
        return popT(Variable.TYPE_FLOAT);
    }

    public Variable popD() {
        return popT(Variable.TYPE_DOUBLE);
    }

    public Variable popA() {
        return popT(Variable.TYPE_REF);
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
        return getT(indexFromTop, Variable.TYPE_INT);
    }

    public Variable getL(final int indexFromTop) {
        return getT(indexFromTop, Variable.TYPE_LONG);
    }

    public Variable getF(final int indexFromTop) {
        return getT(indexFromTop, Variable.TYPE_FLOAT);
    }

    public Variable getD(final int indexFromTop) {
        return getT(indexFromTop, Variable.TYPE_DOUBLE);
    }

    public Variable getA(final int indexFromTop) {
        return getT(indexFromTop, Variable.TYPE_REF);
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
