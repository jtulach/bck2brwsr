package org.apidesign.bck2brwsr.htmlpage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

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
}
