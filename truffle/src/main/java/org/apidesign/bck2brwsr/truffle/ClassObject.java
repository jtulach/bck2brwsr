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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(InteropLibrary.class)
final class ClassObject implements TruffleObject {
    final TruffleObject jsClass;

    ClassObject(TruffleObject jsClass) {
        this.jsClass = jsClass;
    }

    @ExportMessage
    static boolean hasMembers(ClassObject clazz) {
        return true;
    }

    @ExportMessage 
    static Object getMembers(ClassObject clazz, boolean includeInternal) {
        throw new UnsupportedOperationException();
    }

    @ExportMessage
    static boolean isInstantiable(ClassObject clazz) {
        return true;
    }

    @ExportMessage
    static boolean isMemberInvocable(ClassObject clazz, String name) {
        return true;
    }

    @ExportMessage
    static Object invokeMember(ClassObject clazz, String name, Object[] args,
        @Cached("createFindKeysNode(0)") FindKeysNode find,
        @CachedLibrary(limit = "3") InteropLibrary interop
    ) throws UnsupportedMessageException, ArityException, UnknownIdentifierException, UnsupportedTypeException  {
        String n = find.findKey(clazz.jsClass, name);
        FindKeysNode.unwrapArgs(args);
        return interop.invokeMember(clazz.jsClass, n, args);
    }

    static FindKeysNode createFindKeysNode(int b) {
        return new FindKeysNode(b > 0, true);
    }

    static FindKeysNode getUncached() {
        return new FindKeysNode(false, false);
    }

    @ExportMessage
    static Object instantiate(ClassObject clazz, Object[] args,
        @CachedLibrary(limit = "3") InteropLibrary interop
    ) throws UnsupportedMessageException, ArityException, UnsupportedTypeException {
        try {
            Object cnstr = interop.readMember(clazz.jsClass, "constructor");
            Object instance = interop.execute(cnstr);
            return new JavaObject((TruffleObject) instance);
        } catch (UnknownIdentifierException ex) {
            throw ex.raise();
        }
    }
}
