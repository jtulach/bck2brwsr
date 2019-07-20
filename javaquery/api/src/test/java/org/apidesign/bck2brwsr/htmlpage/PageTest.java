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
package org.apidesign.bck2brwsr.htmlpage;

import java.io.IOException;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.apidesign.bck2brwsr.core.ExtraJavaScript;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/** Verify errors emitted by the processor.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ExtraJavaScript(processByteCode = false, resource = "")
public class PageTest {
    @Test public void verifyWrongType() throws IOException {
        String html = "<html><body>"
            + "</body></html>";
        String code = "package x.y.z;\n"
            + "import org.apidesign.bck2brwsr.htmlpage.api.*;\n"
            + "@Page(xhtml=\"index.xhtml\", className=\"Model\", properties={\n"
            + "  @Property(name=\"prop\", type=Runnable.class)\n"
            + "})\n"
            + "class X {\n"
            + "}\n";
        
        Compile c = Compile.create(html, code);
        assertFalse(c.getErrors().isEmpty(), "One error: " + c.getErrors());
        for (Diagnostic<? extends JavaFileObject> e : c.getErrors()) {
            String msg = e.getMessage(Locale.ENGLISH);
            if (!msg.contains("Runnable")) {
                fail("Should contain warning about Runnable: " + msg);
            }
        }
    }
    
}
