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
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.nodes.Node;

@MessageResolution(receiverType = ClassObject.class)
final class ClassObject implements TruffleObject {
    final TruffleObject jsClass;

    ClassObject(TruffleObject jsClass) {
        this.jsClass = jsClass;
    }

    static boolean isInstance(TruffleObject obj) {
        return obj instanceof ClassObject;
    }

    @Override
    public ForeignAccess getForeignAccess() {
        return ClassObjectForeign.ACCESS;
    }


    @Resolve(message = "INVOKE")
    static abstract class InstanceMethodCall extends Node {
        @Child
        private FindKeysNode find = new FindKeysNode(false);
        @Child
        private Node invoke;

        protected Object access(ClassObject clazz, String name, Object... args) {
            String n = find.findKey(clazz.jsClass, name);
            if (invoke == null) {
                invoke = Message.createInvoke(args.length).createNode();
            }
            FindKeysNode.unwrapArgs(args);
            try {
                return ForeignAccess.sendInvoke(invoke, clazz.jsClass, (String) n, args);
            } catch (InteropException ex) {
                throw ex.raise();
            }
        }
    }

    @Resolve(message = "NEW")
    static abstract class New extends Node {
        @Child
        private Node constructor = Message.READ.createNode();
        @Child
        private Node newInst = Message.createExecute(1).createNode();
        @Child
        private Node cons__V = Message.READ.createNode();
        @Child
        private Node initInst = Message.createExecute(1).createNode();

        protected Object access(ClassObject clazz, Object... args) {
            try {
                TruffleObject cnstr = (TruffleObject) ForeignAccess.sendRead(constructor, clazz.jsClass, "constructor");
//                TruffleObject d = (TruffleObject) ForeignAccess.sendRead(cons__V, cnstr, "cons__V");
                TruffleObject instance = (TruffleObject) ForeignAccess.sendExecute(newInst, cnstr);
//                ForeignAccess.sendExecute(initInst, d, instance);
                return new JavaObject(instance);
            } catch (UnknownIdentifierException ex) {
                throw UnknownIdentifierException.raise("<init>");
            } catch (InteropException ex) {
                throw ex.raise();
            }
        }
    }

    @Resolve(message = "READ")
    static abstract class StaticFieldRead extends Node {
        @Child
        private FindKeysNode find = new FindKeysNode(false);
        @Child
        private Node read = Message.READ.createNode();

        protected Object access(ClassObject clazz, String name) {
            String n = find.findKey(clazz.jsClass, name);
            try {
                return ForeignAccess.sendInvoke(read, clazz.jsClass, (String) n);
            } catch (InteropException ex) {
                throw ex.raise();
            }
        }
    }
}
