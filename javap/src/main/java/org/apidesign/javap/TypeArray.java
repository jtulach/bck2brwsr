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

import static org.apidesign.javap.RuntimeConstants.ITEM_Bogus;
import static org.apidesign.javap.RuntimeConstants.ITEM_Integer;
import static org.apidesign.javap.RuntimeConstants.ITEM_Float;
import static org.apidesign.javap.RuntimeConstants.ITEM_Double;
import static org.apidesign.javap.RuntimeConstants.ITEM_Long;
import static org.apidesign.javap.RuntimeConstants.ITEM_Null;
import static org.apidesign.javap.RuntimeConstants.ITEM_InitObject;
import static org.apidesign.javap.RuntimeConstants.ITEM_Object;
import static org.apidesign.javap.RuntimeConstants.ITEM_NewObject;

public final class TypeArray {
    private static final int CAPACITY_INCREMENT = 16;

    private int[] types;
    private int size;

    public TypeArray() {
    }
    
    public TypeArray(final TypeArray initialTypes) {
        setAll(initialTypes);
    }

    public void add(final int newType) {
        ensureCapacity(size + 1);
        types[size++] = newType;
    }

    public void addAll(final TypeArray newTypes) {
        addAll(newTypes.types, 0, newTypes.size);
    }

    public void addAll(final int[] newTypes) {
        addAll(newTypes, 0, newTypes.length);
    }

    public void addAll(final int[] newTypes,
                       final int offset,
                       final int count) {
        if (count > 0) {
            ensureCapacity(size + count);
            arraycopy(newTypes, offset, types, size, count);
            size += count;
        }
    }

    public void set(final int index, final int newType) {
        types[index] = newType;
    }

    public void setAll(final TypeArray newTypes) {
        setAll(newTypes.types, 0, newTypes.size);
    }

    public void setAll(final int[] newTypes) {
        setAll(newTypes, 0, newTypes.length);
    }

    public void setAll(final int[] newTypes,
                       final int offset,
                       final int count) {
        if (count > 0) {
            ensureCapacity(count);
            arraycopy(newTypes, offset, types, 0, count);
            size = count;
        } else {
            clear();
        }
    }

    public void setSize(final int newSize) {
        if (size != newSize) {
            ensureCapacity(newSize);

            for (int i = size; i < newSize; ++i) {
                types[i] = 0;
            }
            size = newSize;
        }
    }

    public void clear() {
        size = 0;
    }

    public int getSize() {
        return size;
    }

    public int get(final int index) {
        return types[index];
    }

    public static String typeString(final int type) {
        switch (type & 0xff) {
            case ITEM_Bogus:
                return "_top_";
            case ITEM_Integer:
                return "_int_";
            case ITEM_Float:
                return "_float_";
            case ITEM_Double:
                return "_double_";
            case ITEM_Long:
                return "_long_";
            case ITEM_Null:
                return "_null_";
            case ITEM_InitObject: // UninitializedThis
                return "_init_";
            case ITEM_Object:
                return "_object_";
            case ITEM_NewObject: // Uninitialized
                return "_new_";
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[");
        if (size > 0) {
            sb.append(typeString(types[0]));
            for (int i = 1; i < size; ++i) {
                sb.append(", ");
                sb.append(typeString(types[i]));
            }
        }

        return sb.append(']').toString();
    }

    private void ensureCapacity(final int minCapacity) {
        if ((minCapacity == 0)
                || (types != null) && (minCapacity <= types.length)) {
            return;
        }

        final int newCapacity =
                ((minCapacity + CAPACITY_INCREMENT - 1) / CAPACITY_INCREMENT)
                    * CAPACITY_INCREMENT;
        final int[] newTypes = new int[newCapacity];

        if (size > 0) {
            arraycopy(types, 0, newTypes, 0, size);
        }

        types = newTypes;
    }

    // no System.arraycopy
    private void arraycopy(final int[] src, final int srcPos,
                           final int[] dest, final int destPos,
                           final int length) {
        for (int i = 0; i < length; ++i) {
            dest[destPos + i] = src[srcPos + i];
        }
    }
}
