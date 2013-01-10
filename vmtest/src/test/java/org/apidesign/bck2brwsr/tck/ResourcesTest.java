/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.bck2brwsr.tck;

import java.io.InputStream;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ResourcesTest {
    
    @Compare public String readResourceAsStream() throws Exception {
        InputStream is = getClass().getResourceAsStream("Resources.txt");
        byte[] b = new byte[30];
        int len = is.read(b);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append((char)b[i]);
        }
        return sb.toString();
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(ResourcesTest.class);
    }
}
