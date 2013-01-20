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
package org.apidesign.bck2brwsr.mavenhtml;

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
    @Property(name = "display", type = double.class)
})
public class App {
    private static double memory;
    private static String operation;
    
    @On(event = CLICK, id="clear")
    static void clear() {
        memory = 0;
        operation = null;
        Calculator.setDisplay(0);
    }
    
    @On(event = CLICK, id= { "plus", "minus", "mul", "div" })
    static void applyOp(String op) {
        memory = Calculator.getDisplay();
        operation = op;
        Calculator.setDisplay(0);
    }
    
    @On(event = CLICK, id="result")
    static void computeTheValue() {
        switch (operation) {
            case "plus": Calculator.setDisplay(memory + Calculator.getDisplay()); break;
            case "minus": Calculator.setDisplay(memory - Calculator.getDisplay()); break;
            case "mul": Calculator.setDisplay(memory * Calculator.getDisplay()); break;
            case "div": Calculator.setDisplay(memory / Calculator.getDisplay()); break;
            default: throw new IllegalStateException(operation);
        }
    }
    
    @On(event = CLICK, id={"n0", "n1", "n2", "n3", "n4", "n5", "n6", "n7", "n8", "n9"}) 
    static void addDigit(String digit) {
        digit = digit.substring(1);
        
        double v = Calculator.getDisplay();
        if (v == 0.0) {
            Calculator.setDisplay(Integer.parseInt(digit));
        } else {
            String txt = Double.toString(v) + digit;
            Calculator.setDisplay(Double.parseDouble(txt));
        }
    }
    
    
    static {
        Calculator.setDisplay(10.0);
        Calculator.applyBindings();
    }
}
