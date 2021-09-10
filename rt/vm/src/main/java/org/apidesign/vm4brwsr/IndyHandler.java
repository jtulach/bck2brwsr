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

import java.io.IOException;

abstract class IndyHandler {
    final String factoryClazz;
    final String factoryMethod;

    IndyHandler(String clazz, String method) {
        this.factoryClazz = clazz;
        this.factoryMethod = method;
    }

    protected abstract boolean handle(Ctx ctx) throws IOException ;

    static class Ctx {
        final Appendable out;
        final AbstractStackMapper stackMapper;
        final ByteCodeToJavaScript byteCodeToJavaScript;
        final ByteCodeParser.BootMethodData bm;
        final String[] mt;

        Ctx(Appendable out, AbstractStackMapper m, ByteCodeToJavaScript bc, String[] methodAndType, ByteCodeParser.BootMethodData bm) {
            this.out = out;
            this.stackMapper = m;
            this.byteCodeToJavaScript = bc;
            this.mt = methodAndType;
            this.bm = bm;
        }
    }
}
