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

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class Resources {
    public static String loadKO() throws IOException {
        InputStream is = Resources.class.getResourceAsStream("ko.js");
        if (is == null) {
            return "No resource found!";
        }
        byte[] arr = new byte[4092];
        int len = is.read(arr);
        if (len == -1) {
            return "No data read!";
        }
        return new String(arr, 0, len, "UTF-8");
    }
}
