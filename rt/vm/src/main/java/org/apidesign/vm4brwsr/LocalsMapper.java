/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

final class LocalsMapper {
    private final TypeArray argTypeRecords;
    private final TypeArray localTypeRecords;

    public LocalsMapper(final TypeArray stackMapArgs) {
        final TypeArray initTypeRecords = new TypeArray();
        updateRecords(initTypeRecords, stackMapArgs);

        argTypeRecords = initTypeRecords;
        localTypeRecords = new TypeArray(initTypeRecords);
    }

    void outputUndefinedCheck(Appendable out) throws IOException {
        final int argRecordCount = argTypeRecords.getSize();
        for (int i = 0; i < argRecordCount;) {
            Variable varI = getVariable(argTypeRecords, i);
            out.append("  if (").append(varI).append(" === undefined) ").append(varI).append(" = null;\n");
            i += varI.isCategory2() ? 2 : 1;
        }
    }

    public void outputArguments(final Appendable out, boolean isStatic) throws IOException {
        final int argRecordCount = argTypeRecords.getSize();
        int first = isStatic ? 0 : 1;
        if (argRecordCount > first) {
            Variable variable = getVariable(argTypeRecords, first);
            out.append(variable);

            int i = first + (variable.isCategory2() ? 2 : 1);
            while (i < argRecordCount) {
                variable = getVariable(argTypeRecords, i);
                out.append(", ");
                out.append(variable);
                i += variable.isCategory2() ? 2 : 1;
            }
        }
    }

    public void syncWithFrameLocals(final TypeArray frameLocals) {
        updateRecords(localTypeRecords, frameLocals);
    }

    public Variable setI(final int index) {
        return setT(index, VarType.INTEGER);
    }

    public Variable setL(final int index) {
        return setT(index, VarType.LONG);
    }

    public Variable setF(final int index) {
        return setT(index, VarType.FLOAT);
    }

    public Variable setD(final int index) {
        return setT(index, VarType.DOUBLE);
    }

    public Variable setA(final int index) {
        return setT(index, VarType.REFERENCE);
    }

    public Variable setT(final int index, final int type) {
        updateRecord(localTypeRecords, index, type);
        return Variable.getLocalVariable(type, index);
    }

    public Variable getI(final int index) {
        return getT(index, VarType.INTEGER);
    }

    public Variable getL(final int index) {
        return getT(index, VarType.LONG);
    }

    public Variable getF(final int index) {
        return getT(index, VarType.FLOAT);
    }

    public Variable getD(final int index) {
        return getT(index, VarType.DOUBLE);
    }

    public Variable getA(final int index) {
        return getT(index, VarType.REFERENCE);
    }

    public Variable getT(final int index, final int type) {
        final int oldRecordValue = localTypeRecords.get(index);
        if ((oldRecordValue & 0xff) != type) {
            throw new IllegalStateException("Type mismatch");
        }

        return Variable.getLocalVariable(type, index);
    }

    private static void updateRecords(final TypeArray typeRecords,
                                      final TypeArray stackMapTypes) {
        final int srcSize = stackMapTypes.getSize();
        for (int i = 0, dstIndex = 0; i < srcSize; ++i) {
            final int smType = stackMapTypes.get(i);
            if (smType == ByteCodeParser.ITEM_Bogus) {
                ++dstIndex;
                continue;
            }
            final int varType = VarType.fromStackMapType(smType);
            updateRecord(typeRecords, dstIndex, varType);
            dstIndex += VarType.isCategory2(varType) ? 2 : 1;
        }
    }

    private static void updateRecord(final TypeArray typeRecords,
                                     final int index, final int type) {
        if (typeRecords.getSize() < (index + 1)) {
            typeRecords.setSize(index + 1);
        }

        final int oldRecordValue = typeRecords.get(index);
        final int usedTypesMask =
                (oldRecordValue >> 8) | (1 << type);
        typeRecords.set(index, (usedTypesMask << 8) | type);
    }

    private static Variable getVariable(final TypeArray typeRecords,
                                        final int index) {
        return Variable.getLocalVariable(typeRecords.get(index) & 0xff, index);
    }

}
