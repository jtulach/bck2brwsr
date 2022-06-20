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

abstract class BytecodeIndexCallback {
    public static final BytecodeIndexCallback NOOP = new BytecodeIndexCallback() {
        @Override
        void call(int bytecodeIndex) { }

        @Override
        void call(int bytecodeIndex, int localVariableSlot) { }

        @Override
        void call(String name) { }

        @Override
        void call() { }
    };

    abstract void call(int bytecodeIndex);
    abstract void call(int bytecodeIndex, int localVariableSlot);
    abstract void call(String name);
    abstract void call();
}
