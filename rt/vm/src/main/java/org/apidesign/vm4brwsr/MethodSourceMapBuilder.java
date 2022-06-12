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

@ExtraJavaScript(processByteCode = false, resource="")
class MethodSourceMapBuilder extends BytecodeIndexCallback {
    private final SourceMapBuilder smb;
    private final String file;
    private final long[] lineNumberTable;
    private int pos;

    MethodSourceMapBuilder(SourceMapBuilder smb, String file, long[] lineNumberTable) {
        this.smb = smb;
        this.file = file;
        this.lineNumberTable = lineNumberTable;
    }

    private int next(int bytecodeIndex) {
        for (;; pos++) {
            if (pos >= lineNumberTable.length)
                return -1;
            long entry = lineNumberTable[pos];
            int key = (int) (entry >> 32);
            if (key > bytecodeIndex)
                return -1;
            if (key == bytecodeIndex)
                return (int) entry - 1; // value, convert 1-based to 0-based
        }
    }

    @Override
    void call(int bytecodeIndex) {
        int lineNumber = next(bytecodeIndex);
        if (lineNumber == -1)
            return;
        smb.addItem(file, lineNumber, 0);
    }

    @Override
    void call() {
        smb.addItem();
    }
}
