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
package org.apidesign.vm4brwsr;

import javax.script.ScriptException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

public abstract class VMLazyAbstract {
    abstract TestVM findCode();

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

        Object res = findCode().invokeFunction("checkKO");
        assertEquals(res, true, "KO is defined on a global level");
    }

    private void assertExec(String msg, String methodName, Object expRes, Object... args) throws Exception {
        Object ret = null;
        final TestVM code = findCode();
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
