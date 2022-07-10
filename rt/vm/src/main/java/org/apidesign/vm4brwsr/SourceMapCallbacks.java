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

import org.apidesign.bck2brwsr.core.ExtraJavaScript;

/**
 * Implements {@link ByteCodePositionCallbacks} by converting bytecode offsets
 * to lines and passing the latter to a {@link SourceMapGenerator}.
 * One instance per Java method.
 */
@ExtraJavaScript(processByteCode = false, resource="")
class SourceMapCallbacks extends ByteCodePositionCallbacks {
    private final SourceMapGenerator srcmap;
    private final String file;
    private final long[] lineNumberTable;
    private final long[] localVariableTableKeys;
    private final String[] localVariableTableValues;
    private int pos;

    SourceMapCallbacks(SourceMapGenerator srcmap, String file, long[] lineNumberTable,
            long[] localVariableTableKeys, String[] localVariableTableValues) {
        this.srcmap = srcmap;
        this.file = file;
        this.lineNumberTable = lineNumberTable;
        this.localVariableTableKeys = localVariableTableKeys;
        this.localVariableTableValues = localVariableTableValues;
    }

    private int next(int byteCodeIndex) {
        for (;; pos++) {
            if (pos >= lineNumberTable.length)
                return -1;
            long entry = lineNumberTable[pos];
            int key = (int) (entry >> 32);
            if (key > byteCodeIndex)
                return -1;
            if (key == byteCodeIndex)
                return (int) entry - 1; // value, convert 1-based to 0-based
        }
    }

    private String findLocalVar(int followingByteCodeIndex, int searchedSlot) {
        int n = localVariableTableKeys.length;
        for (int i = 0; i < n; i++) {
            long key = localVariableTableKeys[i];
            int slot = (int) (key & 0xffff);
            if (slot != searchedSlot)
                continue;
            int start = (int) (key >> 32 & 0xffff);
            if (followingByteCodeIndex < start)
                continue;
            int length = (int) (key >> 16 & 0xffff);
            if (followingByteCodeIndex > start + length)
                continue;
            return localVariableTableValues[i];
        }
        return null;
    }

    @Override
    void reportPosition(int byteCodeIndex) {
        int lineNumber = next(byteCodeIndex);
        if (lineNumber == -1)
            return;
        srcmap.addItem(file, lineNumber, 0);
    }

    @Override
    void reportLocalVariable(int followingByteCodeIndex, int localVariableSlot) {
        String name = findLocalVar(followingByteCodeIndex, localVariableSlot);
        if (name == null)
            return;
        srcmap.extendLastItem(name);
    }

    @Override
    void reportLocalVariable(String name) {
        srcmap.addItem();
        srcmap.extendLastItem(name);
    }

    @Override
    void reportEmptyPosition() {
        srcmap.addItem();
    }
}
