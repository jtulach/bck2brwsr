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
package org.apidesign.bck2brwsr.emul.reflect;

public final class Mangling {
    public static String mangle(CharSequence originalName, boolean replaceDot) {
        int len = originalName.length();
        final int bufSize = Math.max(len * 2, 32);
        char[] buf = new char[bufSize];
        int at = 0;
        for (int i = 0; i < len; i++) {
            if (at > buf.length - 10) {
                char[] copy = new char[buf.length * 2];
                org.apidesign.bck2brwsr.emul.lang.System.arraycopy(buf, 0, copy, 0, buf.length);
                buf = copy;
            }
            final char ch = originalName.charAt(i);
            switch (ch) {
                case '/': buf[at++] = '_'; break;
                case '_': buf[at++] = '_'; buf[at++] = '1'; break;
                case ';': buf[at++] = '_'; buf[at++] = '2'; break;
                case '[': buf[at++] = '_'; buf[at++] = '3'; break;
                case '.':
                    if (replaceDot) {
                        buf[at++] = '_';
                        break;
                    }
                    // fallhrough
                default:
                    boolean valid = i == 0 ?
                            Character.isJavaIdentifierStart(ch) : Character.isJavaIdentifierPart(ch);
                    if (valid) {
                        buf[at++] = ch;
                    } else {
                        buf[at++] = '_';
                        buf[at++] = '0';
                        String hex = Integer.toHexString(ch).toLowerCase();
                        for (int m = hex.length(); m < 4; m++) {
                            buf[at++] = '0';
                        }
                        for (int r = 0; r < hex.length(); r++) {
                            buf[at++] = hex.charAt(r);
                        }
                    }
                break;
            }
        }
        return new String(buf, 0, at);
    }
}
