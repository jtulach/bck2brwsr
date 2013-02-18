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

import org.apidesign.bck2brwsr.htmlpage.api.ComputedProperty;
import org.apidesign.bck2brwsr.htmlpage.api.On;
import static org.apidesign.bck2brwsr.htmlpage.api.OnEvent.*;
import org.apidesign.bck2brwsr.htmlpage.api.Page;
import org.apidesign.bck2brwsr.htmlpage.api.Property;

/** HTML5 & Java demo showing the power of 
 * <a href="http://wiki.apidesign.org/wiki/AnnotationProcessor">annotation processors</a>
 * as well as other goodies.
 * 
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
@Page(xhtml="Calculator.xhtml", properties = {
    @Property(name = "memory", type = double.class),
    @Property(name = "display", type = double.class),
    @Property(name = "operation", type = String.class),
    @Property(name = "hover", type = boolean.class),
    @Property(name = "results", type = String.class, array = true)
})
public class Calc {
    static {
        Calculator m = new Calculator();
        m.applyBindings();
    }
    
    @On(event = CLICK, id="clear")
    static void clear(Calculator c) {
        c.setMemory(0);
        c.setOperation(null);
        c.setDisplay(0);
    }
    
    @On(event = CLICK, id= { "plus", "minus", "mul", "div" })
    static void applyOp(Calculator c, String op) {
        c.setMemory(c.getDisplay());
        c.setOperation(op);
        c.setDisplay(0);
    }

    @On(event = MOUSE_OVER, id= { "result" })
    static void attemptingIn(Calculator c, String op) {
        c.setHover(true);
    }
    @On(event = MOUSE_OUT, id= { "result" })
    static void attemptingOut(Calculator c, String op) {
        c.setHover(false);
    }
    
    @On(event = CLICK, id="result")
    static void computeTheValue(Calculator c) {
        final double val = compute(
            c.getOperation(), 
            c.getMemory(), 
            c.getDisplay()
        );
        c.getResults().add("another result " + val);
        c.setDisplay(val);
        c.setMemory(0);
    }
    
    private static double compute(String op, double memory, double display) {
        switch (op) {
            case "plus": return memory + display;
            case "minus": return memory - display;
            case "mul": return memory * display;
            case "div": return memory / display;
            default: throw new IllegalStateException(op);
        }
    }
    
    @On(event = CLICK, id={"n0", "n1", "n2", "n3", "n4", "n5", "n6", "n7", "n8", "n9"}) 
    static void addDigit(String digit, Calculator c) {
        digit = digit.substring(1);
        
        double v = c.getDisplay();
        if (v == 0.0) {
            c.setDisplay(Integer.parseInt(digit));
        } else {
            String txt = Double.toString(v);
            if (txt.endsWith(".0")) {
                txt = txt.substring(0, txt.length() - 2);
            }
            txt = txt + digit;
            c.setDisplay(Double.parseDouble(txt));
        }
    }

    @ComputedProperty
    public static String displayPreview(
        double display, boolean hover, double memory, String operation
    ) {
        if (!hover) {
            return "Type numbers and perform simple operations! Press '=' to get result.";
        }
        return "Attempt to compute " + memory + " " + operation + " " + display + " = " + compute(operation, memory, display);
    }
}
