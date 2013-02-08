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

import javax.script.Invocable;
import javax.script.ScriptException;
import static org.testng.Assert.*;

class TestUtils {

    static Object execCode(Invocable code, CharSequence codeSeq,
        String msg, Class<?> clazz, String method, Object expRes, Object... args) 
            throws Exception
    {
        Object ret = null;
        try {
            ret = code.invokeFunction("bck2brwsr");
            ret = code.invokeMethod(ret, "loadClass", clazz.getName());
            ret = code.invokeMethod(ret, method, args);
        } catch (ScriptException ex) {
            fail("Execution failed in " + StaticMethodTest.dumpJS(codeSeq), ex);
        } catch (NoSuchMethodException ex) {
            fail("Cannot find method in " + StaticMethodTest.dumpJS(codeSeq), ex);
        }
        if (ret == null && expRes == null) {
            return null;
        }
        if (expRes.equals(ret)) {
            return null;
        }
        if (expRes instanceof Number) {
            // in case of Long it is necessary convert it to number
            // since the Long is represented by two numbers in JavaScript
            try {
                ret = code.invokeMethod(ret, "toFP");
                ret = code.invokeFunction("Number", ret);
            } catch (ScriptException ex) {
                fail("Conversion to number failed in " + StaticMethodTest.dumpJS(codeSeq), ex);
            } catch (NoSuchMethodException ex) {
                fail("Cannot find global Number(x) function in " + StaticMethodTest.dumpJS(codeSeq), ex);
            }
        }
        return ret;
    }
}
