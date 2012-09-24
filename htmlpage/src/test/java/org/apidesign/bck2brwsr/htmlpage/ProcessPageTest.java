/**
 * Java 4 Browser Bytecode Translator
 * Copyright (C) 2012-2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
import java.io.InputStream;
import java.util.Set;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import org.apidesign.bck2brwsr.htmlpage.api.*;

public class ProcessPageTest {
    
    
    @Test public void findsThreeIds() throws IOException {
        InputStream is = ProcessPageTest.class.getResourceAsStream("TestPage.xhtml");
        assertNotNull(is, "Sample HTML page found");
        ProcessPage res = ProcessPage.readPage(is);
        final Set<String> ids = res.ids();
        assertEquals(ids.size(), 3, "Three ids found: " + ids);
        
        assertEquals(res.tagNameForId("pg.title"), "title");
        assertEquals(res.tagNameForId("pg.button"), "button");
        assertEquals(res.tagNameForId("pg.text"), "input");
    }
    
    void testWhetherWeCanCallTheGeneratedIdFields() {
        Title t = TestPage.PG_TITLE;
    }
}
