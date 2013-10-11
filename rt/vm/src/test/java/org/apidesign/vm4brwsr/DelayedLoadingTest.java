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
package org.apidesign.vm4brwsr;

import java.net.URL;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class DelayedLoadingTest {
    private static TestVM code;
    
    @Test public void verifyUsageOf() throws Exception {
        code.register(new BytesLoader());
        
        URL u = new URL("http://apidesign.org");
        
        Object str = code.execCode("Access URI", 
            DelayedLoading.class, "toStrViaURI__Ljava_lang_String_2Ljava_lang_String_2",
            u.toExternalForm(), u.toExternalForm()
        );
    }
    
    
    @BeforeClass 
    public static void compileTheCode() throws Exception {
        code = TestVM.compileClass(
            "org/apidesign/vm4brwsr/DelayedLoading");
    }
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }
    
}

