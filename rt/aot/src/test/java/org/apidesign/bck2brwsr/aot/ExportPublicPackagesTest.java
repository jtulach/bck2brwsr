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
package org.apidesign.bck2brwsr.aot;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import org.apidesign.vm4brwsr.Bck2Brwsr;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class ExportPublicPackagesTest {
    
    public ExportPublicPackagesTest() {
    }

    @Test public void classicOSGIHeaders() throws Exception {
        Attributes attr = new Attributes();
        attr.putValue("Export-Package", 
            "net.java.html.json;uses:=\"net.java.html\";version=\"1.0.0\","
          + "org.netbeans.html.json.spi;uses:=\"net.java.html\";version=\"1.0.0\""
        );
        Set<String> keep = new HashSet<>();
        Bck2BrwsrJars.exportPublicPackages(attr, keep);
        
        assertEquals(keep.size(), 2, "Two: " + keep);
        assertTrue(keep.contains("net/java/html/json/"), "json pkg: " + keep);
        assertTrue(keep.contains("org/netbeans/html/json/spi/"), "SPI pkg: " + keep);
    }
    
    @Test public void mylynOSGIHeaders() throws Exception {
        Attributes attr = new Attributes();
        attr.putValue("Export-Package", 
"org.eclipse.mylyn.commons.core,org.eclipse.mylyn.commo" +
"ns.core.io,org.eclipse.mylyn.commons.core.net,org.eclipse.mylyn.commo" +
"ns.core.operations,org.eclipse.mylyn.commons.core.storage;x-internal:" +
"=true,org.eclipse.mylyn.internal.commons.core;x-internal:=true,org.ec" +
"lipse.mylyn.internal.commons.core.operations;x-internal:=true"                
        );
        Set<String> keep = new TreeSet<>();
        Bck2BrwsrJars.exportPublicPackages(attr, keep);
        
        assertEquals(keep.size(), 7, "Two: " + keep);
        assertEquals(keep.toString(), "[org/eclipse/mylyn/commons/core/, org/eclipse/mylyn/commons/core/io/, org/eclipse/mylyn/commons/core/net/, org/eclipse/mylyn/commons/core/operations/, org/eclipse/mylyn/commons/core/storage/, org/eclipse/mylyn/internal/commons/core/, org/eclipse/mylyn/internal/commons/core/operations/]");
    }

}
