/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;

import static org.apidesign.javap.RuntimeConstants.*;

/* represents one entry of StackMapTable attribute
 */
abstract class StackMapTableData {
    final int frameType;
    int offsetDelta;

    StackMapTableData(int frameType) {
        this.frameType = frameType;
    }

    abstract void applyTo(TypeArray localTypes, TypeArray stackTypes);

    protected static String toString(
            final String frameType,
            final int offset,
            final int[] localTypes,
            final int[] stackTypes) {
        final StringBuilder sb = new StringBuilder(frameType);

        sb.append("(off: +").append(offset);
        if (localTypes != null) {
            sb.append(", locals: ");
            appendTypes(sb, localTypes);
        }
        if (stackTypes != null) {
            sb.append(", stack: ");
            appendTypes(sb, stackTypes);
        }
        sb.append(')');

        return sb.toString();
    }

    private static void appendTypes(final StringBuilder sb, final int[] types) {
        sb.append('[');
        if (types.length > 0) {
            sb.append(TypeArray.typeString(types[0]));
            for (int i = 1; i < types.length; ++i) {
                sb.append(", ");
                sb.append(TypeArray.typeString(types[i]));
            }
        }
        sb.append(']');
    }

    static class SameFrame extends StackMapTableData {
        SameFrame(int frameType, int offsetDelta) {
            super(frameType);
            this.offsetDelta = offsetDelta;
        }

        @Override
        void applyTo(TypeArray localTypes, TypeArray stackTypes) {
            stackTypes.clear();
        }

        @Override
        public String toString() {
            return toString("SAME" + ((frameType == SAME_FRAME_EXTENDED)
                                          ? "_FRAME_EXTENDED" : ""),
                            offsetDelta,
                            null, null);
        }
    }

    static class SameLocals1StackItem extends StackMapTableData {
        final int[] stack;
        SameLocals1StackItem(int frameType, int offsetDelta, int[] stack) {
            super(frameType);
            this.offsetDelta = offsetDelta;
            this.stack = stack;
        }

        @Override
        void applyTo(TypeArray localTypes, TypeArray stackTypes) {
            stackTypes.setAll(stack);
        }

        @Override
        public String toString() {
            return toString(
                       "SAME_LOCALS_1_STACK_ITEM"
                           + ((frameType == SAME_LOCALS_1_STACK_ITEM_EXTENDED)
                                  ? "_EXTENDED" : ""),
                       offsetDelta,
                       null, stack);
        }
    }

    static class ChopFrame extends StackMapTableData {
        ChopFrame(int frameType, int offsetDelta) {
            super(frameType);
            this.offsetDelta = offsetDelta;
        }

        @Override
        void applyTo(TypeArray localTypes, TypeArray stackTypes) {
            localTypes.setSize(localTypes.getSize()
                                   - (SAME_FRAME_EXTENDED - frameType));
            stackTypes.clear();
        }

        @Override
        public String toString() {
            return toString("CHOP", offsetDelta, null, null);
        }
    }

    static class AppendFrame extends StackMapTableData {
        final int[] locals;
        AppendFrame(int frameType, int offsetDelta, int[] locals) {
            super(frameType);
            this.offsetDelta = offsetDelta;
            this.locals = locals;
        }

        @Override
        void applyTo(TypeArray localTypes, TypeArray stackTypes) {
            localTypes.addAll(locals);
            stackTypes.clear();
        }

        @Override
        public String toString() {
            return toString("APPEND", offsetDelta, locals, null);
        }
    }

    static class FullFrame extends StackMapTableData {
        final int[] locals;
        final int[] stack;
        FullFrame(int offsetDelta, int[] locals, int[] stack) {
            super(FULL_FRAME);
            this.offsetDelta = offsetDelta;
            this.locals = locals;
            this.stack = stack;
        }

        @Override
        void applyTo(TypeArray localTypes, TypeArray stackTypes) {
            localTypes.setAll(locals);
            stackTypes.setAll(stack);
        }

        @Override
        public String toString() {
            return toString("FULL", offsetDelta, locals, stack);
        }
    }

    static StackMapTableData getInstance(DataInputStream in, MethodData method)
                  throws IOException {
        int frameType = in.readUnsignedByte();

        if (frameType < SAME_FRAME_BOUND) {
            // same_frame
            return new SameFrame(frameType, frameType);
        } else if (SAME_FRAME_BOUND <= frameType && frameType < SAME_LOCALS_1_STACK_ITEM_BOUND) {
            // same_locals_1_stack_item_frame
            // read additional single stack element
            return new SameLocals1StackItem(frameType,
                                            (frameType - SAME_FRAME_BOUND),
                                            StackMapData.readTypeArray(in, 1, method));
        } else if (frameType == SAME_LOCALS_1_STACK_ITEM_EXTENDED) {
            // same_locals_1_stack_item_extended
            return new SameLocals1StackItem(frameType,
                                            in.readUnsignedShort(),
                                            StackMapData.readTypeArray(in, 1, method));
        } else if (SAME_LOCALS_1_STACK_ITEM_EXTENDED < frameType  && frameType < SAME_FRAME_EXTENDED) {
            // chop_frame or same_frame_extended
            return new ChopFrame(frameType, in.readUnsignedShort());
        } else if (frameType == SAME_FRAME_EXTENDED) {
            // chop_frame or same_frame_extended
            return new SameFrame(frameType, in.readUnsignedShort());
        } else if (SAME_FRAME_EXTENDED < frameType  && frameType < FULL_FRAME) {
            // append_frame
            return new AppendFrame(frameType, in.readUnsignedShort(),
                                   StackMapData.readTypeArray(in, frameType - SAME_FRAME_EXTENDED, method));
        } else if (frameType == FULL_FRAME) {
            // full_frame
            int offsetDelta = in.readUnsignedShort();
            int locals_size = in.readUnsignedShort();
            int[] locals = StackMapData.readTypeArray(in, locals_size, method);
            int stack_size = in.readUnsignedShort();
            int[] stack = StackMapData.readTypeArray(in, stack_size, method);
            return new FullFrame(offsetDelta, locals, stack);
        } else {
            throw new ClassFormatError("unrecognized frame_type in StackMapTable");
        }
    }
}
