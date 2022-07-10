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

/**
 * Called by {@link LoopCode} and {@link LocalsMapper#outputArguments}
 * when printing to output.
 * @see SourceMapGeneratorCallbacks the non-empty implementation
 */
abstract class ByteCodePositionCallbacks {
    public static final ByteCodePositionCallbacks NOOP = new ByteCodePositionCallbacks() {
        @Override
        void reportPosition(int byteCodeIndex) { }

        @Override
        void reportLocalVariable(int byteCodeIndex, int localVariableSlot) { }

        @Override
        void reportLocalVariable(String name) { }

        @Override
        void reportEmptyPosition() { }
    };

    abstract void reportPosition(int byteCodeIndex);
    abstract void reportLocalVariable(int byteCodeIndex, int localVariableSlot);
    abstract void reportLocalVariable(String name);
    abstract void reportEmptyPosition();
}
