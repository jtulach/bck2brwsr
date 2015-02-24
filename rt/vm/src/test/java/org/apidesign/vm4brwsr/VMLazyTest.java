/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
import javax.script.ScriptException;
import org.testng.annotations.BeforeClass;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/** Implements loading class by class.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class VMLazyTest {
    private static TestVM code;

    @BeforeClass
    public static void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("\nvar data = {};");
        sb.append("\nfunction test(clazz, method) {");
        sb.append("\n  if (!data.bck2brwsr) data.bck2brwsr = bck2brwsr(function(name) { return loader.get(name); });");
        sb.append("\n  var c = data.bck2brwsr.loadClass(clazz);");
        sb.append("\n  return c[method]();");
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
    @AfterClass
    public static void releaseTheCode() {
        code = null;
    }
    
    @Test public void invokeStaticMethod() throws Exception {
        assertExec("Trying to get -1", "test", Double.valueOf(-1),
            StaticMethod.class.getName(), "minusOne__I"
        );
    }

    @Test public void loadDependantClass() throws Exception {
        assertExec("Expecting zero", "test", Double.valueOf(0),
            InstanceSub.class.getName(), "recallDbl__D"
        );
    }

    @Test public void loadClassWithAssociatedScript() throws Exception {
        assertExec("ko is defined", "test", true,
            Script.class.getName(), "checkNotNull__Z"
        );
        
        Object res = code.invokeFunction("checkKO");
        assertEquals(res, true, "KO is defined on a global level");
    }

    private static void assertExec(String msg, String methodName, Object expRes, Object... args) throws Exception {
        Object ret = null;
        try {
            ret = code.invokeFunction(methodName, args);
        } catch (ScriptException ex) {
            fail("Execution failed in\n" + code.toString(), ex);
        } catch (NoSuchMethodException ex) {
            fail("Cannot find method in\n" + code.toString(), ex);
        }
        if (ret == null && expRes == null) {
            return;
        }
        if (expRes instanceof Double && ret instanceof Number) {
            ret = ((Number)ret).doubleValue();
        }
        if (expRes.equals(ret)) {
            return;
        }
        assertEquals(ret, expRes, msg + "was: " + ret + "\n" + code.toString());
    }
}
