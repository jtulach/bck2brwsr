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
package org.apidesign.benchmark.jbox2d;

import java.io.IOException;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

public class JBox2dSimulationTest {
    public JBox2dSimulationTest() {
    }

    @Compare(slowdown = 30.0)
    public int fewIter() throws IOException {
        Scene s = new Scene();
        for (int i = 0; i < 100; i++) {
            s.calculate();
        }
        return s.getWorld().getBodyCount();
    }

    @Factory
    public static Object[] create() {
        return VMTest.create(JBox2dSimulationTest.class);
    }
}
