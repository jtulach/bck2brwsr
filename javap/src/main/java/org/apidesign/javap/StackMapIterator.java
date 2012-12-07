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

public final class StackMapIterator {
    private static final StackMapTableData INITIAL_FRAME =
            new StackMapTableData(-1) {
                @Override
                void applyTo(TypeArray localTypes, TypeArray stackTypes) {
                    localTypes.clear();
                    stackTypes.clear();
                }

                @Override
                public String toString() {
                    return toString("INITIAL", 0, null, null);
                }

            };

    private final StackMapTableData[] stackMapTable;
    private final TypeArray localTypes;
    private final TypeArray stackTypes;

    private int nextFrameIndex;
    private int lastFrameByteCodeOffset;

    private int byteCodeOffset;

    StackMapIterator(final StackMapTableData[] stackMapTable) {
        this.stackMapTable = (stackMapTable != null)
                                 ? stackMapTable
                                 : new StackMapTableData[0];

        localTypes = new TypeArray();
        stackTypes = new TypeArray();
        lastFrameByteCodeOffset = -1;
        advanceBy(0);
    }

    public String getFrameAsString() {
        return getCurrentFrame().toString();
    }

    public int getFrameIndex() {
        return nextFrameIndex;
    }

    public TypeArray getFrameStack() {
        return stackTypes;
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

    private StackMapTableData getCurrentFrame() {
        return (nextFrameIndex == 0)
                ? INITIAL_FRAME
                : stackMapTable[nextFrameIndex - 1];
    }
}
