/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.apidesign.javap;

import static org.apidesign.javap.RuntimeConstants.ITEM_Integer;
import static org.apidesign.javap.RuntimeConstants.ITEM_Float;
import static org.apidesign.javap.RuntimeConstants.ITEM_Double;
import static org.apidesign.javap.RuntimeConstants.ITEM_Long;
import static org.apidesign.javap.RuntimeConstants.ITEM_Object;

public final class StackMapIterator {
    private final StackMapTableData[] stackMapTable;
    private final TypeArray argTypes;
    private final TypeArray localTypes;
    private final TypeArray stackTypes;

    private int nextFrameIndex;
    private int lastFrameByteCodeOffset;

    private int byteCodeOffset;

    StackMapIterator(final MethodData methodData) {
        this(methodData.getStackMapTable(),
             methodData.getInternalSig(),
             methodData.isStatic());
    }

    StackMapIterator(final StackMapTableData[] stackMapTable,
                     final String methodSignature,
                     final boolean isStaticMethod) {
        this.stackMapTable = (stackMapTable != null)
                                 ? stackMapTable
                                 : new StackMapTableData[0];

        argTypes = getArgTypes(methodSignature, isStaticMethod);
        localTypes = new TypeArray();
        stackTypes = new TypeArray();

        localTypes.addAll(argTypes);

        lastFrameByteCodeOffset = -1;
        advanceBy(0);
    }

    public String getFrameAsString() {
        return (nextFrameIndex == 0)
                   ? StackMapTableData.toString("INITIAL", 0, null, null)
                   : stackMapTable[nextFrameIndex - 1].toString();
    }

    public int getFrameIndex() {
        return nextFrameIndex;
    }

    public TypeArray getFrameStack() {
        return stackTypes;
    }

    public TypeArray getFrameLocals() {
        return localTypes;
    }

    public TypeArray getArguments() {
        return argTypes;
    }

    public void advanceBy(final int numByteCodes) {
        if (numByteCodes < 0) {
            throw new IllegalStateException("Forward only iterator");
        }

        byteCodeOffset += numByteCodes;
        while ((nextFrameIndex < stackMapTable.length)
                    && ((byteCodeOffset - lastFrameByteCodeOffset)
                            >= (stackMapTable[nextFrameIndex].offsetDelta
                                    + 1))) {
            final StackMapTableData nextFrame = stackMapTable[nextFrameIndex];

            lastFrameByteCodeOffset += nextFrame.offsetDelta + 1;
            nextFrame.applyTo(localTypes, stackTypes);

            ++nextFrameIndex;
        }
    }

    public void advanceTo(final int nextByteCodeOffset) {
        advanceBy(nextByteCodeOffset - byteCodeOffset);
    }

    private static TypeArray getArgTypes(final String methodSignature,
                                         final boolean isStaticMethod) {
        final TypeArray argTypes = new TypeArray();

        if (!isStaticMethod) {
            argTypes.add(ITEM_Object);
        }

        if (methodSignature.charAt(0) != '(') {
            throw new IllegalArgumentException("Invalid method signature");
        }

        final int length = methodSignature.length();
        int skipType = 0;
        int argType;
        for (int i = 1; i < length; ++i) {
            switch (methodSignature.charAt(i)) {
                case 'B':
                case 'C':
                case 'S':
                case 'Z':
                case 'I':
                    argType = ITEM_Integer;
                    break;
                case 'J':
                    argType = ITEM_Long;
                    break;
                case 'F':
                    argType = ITEM_Float;
                    break;
                case 'D':
                    argType = ITEM_Double;
                    break;
                case 'L': {
                    i = methodSignature.indexOf(';', i + 1);
                    if (i == -1) {
                        throw new IllegalArgumentException(
                                      "Invalid method signature");
                    }
                    argType = ITEM_Object;
                    break;
                }
                case ')':
                    // not interested in the return value type
                    return argTypes;
                case '[':
                    if (skipType == 0) {
                        argTypes.add(ITEM_Object);
                    }
                    ++skipType;
                    continue;

                default:
                    throw new IllegalArgumentException(
                                  "Invalid method signature");
            }

            if (skipType == 0) {
                argTypes.add(argType);
            } else {
                --skipType;
            }
        }

        return argTypes;
    }
}
