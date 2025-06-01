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
package org.apidesign.bck2brwsr.vmtest.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.Http;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach
 */
@GenerateZip(name = "readAnEntry.zip", contents = { 
    "my/main/file.txt", "Hello World!"
})
public class ZipFileTest {
    
    @Compare public String readAnEntry() throws IOException {
        InputStream is = ZipFileTest.class.getResourceAsStream("readAnEntry.zip");
        ZipInputStream zip = new ZipInputStream(is);
        ZipEntry entry = zip.getNextEntry();
        assertEquals(entry.getName(), "my/main/file.txt", "Correct entry");

        byte[] arr = new byte[4096];
        int len = zip.read(arr);
        
        assertEquals(zip.getNextEntry(), null, "No next entry");
        
        final String ret = new String(arr, 0, len, "UTF-8");
        return ret;
    }
    
    @JavaScriptBody(args = { "res", "path" }, body = 
          "var myvm = bck2brwsr.apply(null, path);\n"
        + "var cls = myvm['loadClass']('java.lang.String');\n"
        + "return cls['getClass__Ljava_lang_Class_2']()['getResourceAsStream__Ljava_io_InputStream_2Ljava_lang_String_2'](res);\n"
    )
    private static native Object loadVMResource(String res, String...path);

    @Http({
        @Http.Resource(path = "/readAnEntry.jar", mimeType = "x-application/zip", content = "", resource="readAnEntry.zip")
    })
    @BrwsrTest  public void canVmLoadResourceFromZip() throws IOException {
        Object res = loadVMResource("/my/main/file.txt", "/readAnEntry.jar");
        assert res instanceof InputStream : "Got array of bytes: " + res;
        InputStream is = (InputStream)res;
        
        byte[] arr = new byte[4096];
        int len = is.read(arr);
        
        final String ret = new String(arr, 0, len, "UTF-8");

        assertEquals(ret, "Hello World!", "Can read the bytes");
    }
    
    @GenerateZip(name = "cpattr.zip", contents = { 
        "META-INF/MANIFEST.MF", "Manifest-Version: 1.0\n"
        + "Created-By: hand\n"
        + "Class-Path: realJar.jar\n\n\n"
    })
    @Http({
        @Http.Resource(path = "/readComplexEntry.jar", mimeType = "x-application/zip", content = "", resource="cpattr.zip"),
        @Http.Resource(path = "/realJar.jar", mimeType = "x-application/zip", content = "", resource="readAnEntry.zip"),
    })
    @BrwsrTest  public void understandsClassPathAttr() throws IOException {
        Object res = loadVMResource("/my/main/file.txt", "/readComplexEntry.jar");
        assert res instanceof InputStream : "Got array of bytes: " + res;
        InputStream is = (InputStream)res;
        
        byte[] arr = new byte[4096];
        int len = is.read(arr);
        
        final String ret = new String(arr, 0, len, "UTF-8");

        assertEquals(ret, "Hello World!", "Can read the bytes from secondary JAR");
    }
    
    private static void assertEquals(Object real, Object exp, String msg) {
        if (real == null) {
            if (exp == null) {
                return;
            }
        } else {
            if (real.equals(exp)) {
                return;
            }
        }
        assert false : msg + " exp: " + exp + " real: " + real;
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(ZipFileTest.class);
    }
}
