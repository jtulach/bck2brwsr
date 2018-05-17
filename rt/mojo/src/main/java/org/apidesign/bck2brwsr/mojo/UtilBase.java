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

package org.apidesign.bck2brwsr.mojo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class UtilBase {
    private UtilBase() {
    }

    static String mangleIndexPage(String data, String mainClass) {
        Pattern loadClass = Pattern.compile("loadClass *\\( *[\"']" + mainClass);
        Matcher loadClassMatcher = loadClass.matcher(data);
        if (loadClassMatcher.find()) {
            return data;
        }

        int endOfBody = data.toLowerCase().lastIndexOf("</body>");
        String newText =
            data.substring(0, endOfBody) +
            invokeSnippet(mainClass) +
            data.substring(endOfBody);
        return newText;
    }

    static String invokeSnippet(String mainClass) {
        return "\n"
            + "\n"
            + "\n"
            + "<script src='bck2brwsr.js'></script>\n"
            + "<script>\n" + "var vm = bck2brwsr('main.js');\n"
            + "vm.loadClass('" + mainClass + "', function(mainClass) {\n"
            + "  mainClass.invoke('main');\n"
            + "});\n"
            + "</script>\n"
            + "\n"
            + "\n";
    }
}
