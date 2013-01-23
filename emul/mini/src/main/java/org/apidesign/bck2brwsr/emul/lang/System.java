/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.emul.lang;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class System {
    private System() {
    }

    public static void arraycopy(char[] value, int srcBegin, char[] dst, int dstBegin, int count) {
        if (srcBegin < dstBegin) {
            while (count-- > 0) {
                dst[dstBegin + count] = value[srcBegin + count];
            }
        } else {
            while (count-- > 0) {
                dst[dstBegin++] = value[srcBegin++];
            }
        }
    }
    
    public static void arraycopy(byte[] value, int srcBegin, byte[] dst, int dstBegin, int count) {
        if (srcBegin < dstBegin) {
            while (count-- > 0) {
                dst[dstBegin + count] = value[srcBegin + count];
            }
        } else {
            while (count-- > 0) {
                dst[dstBegin++] = value[srcBegin++];
            }
        }
    }

    public static void arraycopy(Object[] value, int srcBegin, Object[] dst, int dstBegin, int count) {
        if (srcBegin < dstBegin) {
            while (count-- > 0) {
                dst[dstBegin + count] = value[srcBegin + count];
            }
        } else {
            while (count-- > 0) {
                dst[dstBegin++] = value[srcBegin++];
            }
        }
    }
    
}
