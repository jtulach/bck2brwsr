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

public class TypeArray {
    private static final int CAPACITY_INCREMENT = 16;

    private int[] types;
    private int size;

    public void add(final int newType) {
        ensureCapacity(size + 1);
        types[size++] = newType;
    }

    public void addAll(final int[] newTypes) {
        if (newTypes.length > 0) {
            ensureCapacity(size + newTypes.length);
            arraycopy(newTypes, 0, types, size, newTypes.length);
            size += newTypes.length;
        }
    }

    public void setAll(final int[] newTypes) {
        if (newTypes.length > 0) {
            ensureCapacity(newTypes.length);
            arraycopy(newTypes, 0, types, 0, newTypes.length);
            size = newTypes.length;
        } else {
            clear();
        }
    }

    public void setSize(final int newSize) {
        ensureCapacity(newSize);
        size = newSize;
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
