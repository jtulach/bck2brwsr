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
package org.apidesign.bck2brwsr.truffle;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import java.util.Base64;

@ExportLibrary(InteropLibrary.class)
final class AtoB implements TruffleObject {
    @ExportMessage
    static boolean isExecutable(AtoB obj) {
        return true;
    }

    @ExportMessage
    static String execute(AtoB obj, Object[] args) {
        return parseBase64Binary((String) args[0]);
    }

    @CompilerDirectives.TruffleBoundary
    private static String parseBase64Binary(String s) {
        final byte[] arr = Base64.getDecoder().decode(s);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            int ch = arr[i];
            sb.append((char) ch);
        }
        return sb.toString();
    }
}
