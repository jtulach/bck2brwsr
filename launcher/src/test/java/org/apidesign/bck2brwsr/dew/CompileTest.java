/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.bck2brwsr.dew;

import java.io.IOException;
import java.util.Map;
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
                + "class X {"
                + "}";
        Map<String,byte[]> result = Compile.compile(html, java);

        assertNotNull(result.get("x/y/z/X.class"), "Class X is compiled: " + result);
    }
}
