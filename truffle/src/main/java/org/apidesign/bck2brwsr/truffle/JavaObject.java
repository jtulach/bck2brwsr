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

import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;

@MessageResolution(receiverType = JavaObject.class)
final class JavaObject implements TruffleObject {
    final TruffleObject jsObj;

    public JavaObject(TruffleObject instance) {
        this.jsObj = instance;
    }

    static boolean isInstance(TruffleObject obj) {
        return obj instanceof JavaObject;
    }

    @Override
    public ForeignAccess getForeignAccess() {
        return JavaObjectForeign.ACCESS;
    }

    @Resolve(message = "INVOKE")
    static abstract class StaticMethodCall extends Node {

        @Node.Child
        private FindKeysNode find = new FindKeysNode(true, true);
        @Node.Child
        private Node invoke;

        protected Object access(JavaObject obj, String name, Object... args) {
            String n = find.findKey(obj.jsObj, name);
            if (invoke == null) {
                invoke = Message.createInvoke(args.length).createNode();
            }
            FindKeysNode.unwrapArgs(args);
            try {
                return ForeignAccess.sendInvoke(invoke, obj.jsObj, (String) n, args);
            } catch (InteropException ex) {
                throw ex.raise();
            }
        }
    }

}
