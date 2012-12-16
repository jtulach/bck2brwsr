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
package org.apidesign.vm4brwsr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class BytesLoader {
    private static Set<String> requested = new TreeSet<String>();

    public byte[] get(String name) throws IOException {
        if (!requested.add(name)) {
            throw new IllegalStateException("Requested for second time: " + name);
        }
        InputStream is = BytesLoader.class.getClassLoader().getResourceAsStream(name);
        if (is == null) {
            throw new IOException("Can't find " + name);
        }
        byte[] arr = new byte[is.available()];
        int offset = 0;
        while (offset < arr.length) {
            int len = is.read(arr, offset, arr.length - offset);
            if (len == -1) {
                throw new IOException("Can't read " + name);
            }
            offset += len;
        }
        /*
        System.err.print("loader['" + name + "'] = [");
        for (int i = 0; i < arr.length; i++) {
        if (i > 0) {
        System.err.print(", ");
        }
        System.err.print(arr[i]);
        }
        System.err.println("]");
         */
        return arr;
    }
    
}
