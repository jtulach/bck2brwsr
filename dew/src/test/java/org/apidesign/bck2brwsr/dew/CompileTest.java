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
package org.apidesign.bck2brwsr.dew;

import java.io.IOException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class CompileTest  {
    @Test public void testCompile() throws IOException {
        String html = "<html><body>"
                + " <button id='btn'>Hello!</button>"
                + "</body></html>";
        String java = "package x.y.z;"
                + "import org.apidesign.bck2brwsr.htmlpage.api.*;"
                + "import static org.apidesign.bck2brwsr.htmlpage.api.OnEvent.*;"
            + "@Page(xhtml=\"index.html\", className=\"Index\")"
            + "class X { "
            + "   @On(event=CLICK, id=\"btn\") static void clcs() {}"
            + "}";
        Compile result = Compile.create(html, java);

        assertNotNull(result.get("x/y/z/X.class"), "Class X is compiled: " + result);
        assertNotNull(result.get("x/y/z/Index.class"), "Class Index is compiled: " + result);
    }
}
