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

package org.apidesign.bck2brwsr.truffle;

import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;

final class LoadClassNode extends Node {
    private final TruffleObject vm;
    @Node.Child
    private InteropLibrary invoke = InteropLibrary.getFactory().getUncached();

    LoadClassNode(TruffleObject vm) {
        this.vm = vm;
    }

    TruffleObject loadClass(String name) {
        Object clazz;
        try {
            clazz = invoke.invokeMember(vm, "loadClass", name);
        } catch (InteropException ex) {
            throw VM.raise(ex);
        }
        return (TruffleObject) clazz;
    }
}
