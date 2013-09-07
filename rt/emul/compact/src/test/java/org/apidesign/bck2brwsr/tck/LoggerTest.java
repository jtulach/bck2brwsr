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
package org.apidesign.bck2brwsr.tck;

import java.util.logging.Logger;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class LoggerTest {
    @Compare public String parentLogger() {
        Logger lx = Logger.getLogger("x");
        assert lx != null;
        assert lx.getName().equals("x") : "Right name: " + lx.getName();
        Logger lxyz = Logger.getLogger("x.y.z");
        assert lxyz != null;
        assert lxyz.getName().equals("x.y.z") : "xyz name: " + lxyz.getName();
        return lxyz.getParent().getName();
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(LoggerTest.class);
    }
}
