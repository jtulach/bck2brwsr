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
package org.apidesign.bck2brwsr.demo.calc;

import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Demonstrating POJO testing of HTML page model.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class CalcTest {
    private Calculator model;
    

    @BeforeMethod
    public void initModel() {
        model = new Calculator().applyBindings();
    }

    @Test
    public void testSomeMethod() {
        model.setDisplay(10);
        Calc.applyOp(model, "plus");
        assertEquals(0.0, model.getDisplay(), "Cleared after pressing +");
        model.setDisplay(5);
        Calc.computeTheValue(model);
        assertEquals(15.0, model.getDisplay(), "Shows fifteen");
    }
}
