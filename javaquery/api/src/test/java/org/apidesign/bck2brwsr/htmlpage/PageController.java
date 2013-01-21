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
package org.apidesign.bck2brwsr.htmlpage;

import static org.apidesign.bck2brwsr.htmlpage.api.OnEvent.*;
import org.apidesign.bck2brwsr.htmlpage.api.On;
import org.apidesign.bck2brwsr.htmlpage.api.Page;

/** Trivial demo for the bck2brwsr project. First of all start
 * with <a href="TestPage.html">your XHTML page</a>. Include there
 * a script that will <em>boot Java</em> in your browser.
 * <p>
 * Then use <code>@Page</code> annotation to 
 * generate a Java representation of elements with IDs in that page.
 * Depending on the type of the elements, they will have different 
 * methods (e.g. <code>PG_TITLE</code> has <code>setText</code>, etc.).
 * Use <code>@OnClick</code> annotation to associate behavior
 * with existing elements. Use the generated elements
 * (<code>PG_TITLE</code>, <code>PG_TEXT</code>) to modify the page.
 * <p>
 * Everything is type-safe. As soon as somebody modifies the page and
 * removes the IDs or re-assigns them to wrong elements. Java compiler
 * will emit an error.
 * <p>
 * Welcome to the type-safe HTML5 world!
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@Page(xhtml="TestPage.html")
public class PageController {
    private static final TestPage PAGE = new TestPage();
    
    @On(event = CLICK, id="pg.button")
    static void updateTitle() {
        PAGE.PG_TITLE.setText("You want this window to be named " + PAGE.PG_TEXT.getValue());
    }
    
    @On(event = CLICK, id={ "pg.title", "pg.text" })
    static void click(String id) {
        if (!id.equals("pg.title")) {
            throw new IllegalStateException();
        }
        PAGE.PG_TITLE.setText(id);
    }
}
