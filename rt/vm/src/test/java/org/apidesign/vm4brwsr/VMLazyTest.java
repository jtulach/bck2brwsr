/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/** Implements loading class by class.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class VMLazyTest extends VMLazyAbstract {
    private static TestVM code;

    @BeforeClass
    public static void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("\nvar data = {};");
        sb.append("\nfunction test(clazz, method) {");
        sb.append("\n  if (!data.bck2brwsr) data.bck2brwsr = bck2brwsr(function(name) { return loader.get(name); });");
        sb.append("\n  var c = data.bck2brwsr.loadClass(clazz);");
        sb.append("\n  return c.invoke(method.split('__')[0]);");
        sb.append("\n}");
        
        sb.append("\nfunction checkKO() {");
        sb.append("\n  return ko !== null;");
        sb.append("\n}");
       
        ScriptEngine[] arr = { null };
        code = TestVM.compileClass(sb, arr,
            new String[]{"org/apidesign/vm4brwsr/VM", "org/apidesign/vm4brwsr/StaticMethod"}
        );
        arr[0].getContext().setAttribute("loader", new BytesLoader(), ScriptContext.ENGINE_SCOPE);
    }

    @Override
    TestVM findCode() {
        assertNotNull(code);
        return code;
    }

    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }
    
}
