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

import org.apidesign.bck2brwsr.htmlpage.api.OnClick;
import org.apidesign.bck2brwsr.htmlpage.api.Page;

/** HTML5 & Java demo showing the power of 
 * <a href="http://wiki.apidesign.org/wiki/AnnotationProcessor">annotation processors</a>
 * as well as other goodies.
 * 
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
@Page(xhtml="Calculator.xhtml")
public class App {
    private static double memory;
    private static String operation;
    
    @OnClick(id="clear")
    static void clear() {
        memory = 0;
        operation = null;
        Calculator.DISPLAY.setValue("0");
    }
    
    @OnClick(id= { "plus", "minus", "mul", "div" })
    static void applyOp(String op) {
        memory = getValue();
        operation = op;
        Calculator.DISPLAY.setValue("0");
    }
    
    @OnClick(id="result")
    static void computeTheValue() {
        switch (operation) {
            case "plus": setValue(memory + getValue()); break;
            case "minus": setValue(memory - getValue()); break;
            case "mul": setValue(memory * getValue()); break;
            case "div": setValue(memory / getValue()); break;
            default: throw new IllegalStateException(operation);
        }
    }
    
    @OnClick(id={"n0", "n1", "n2", "n3", "n4", "n5", "n6", "n7", "n8", "n9"}) 
    static void addDigit(String digit) {
        digit = digit.substring(1);
        String v = Calculator.DISPLAY.getValue();
        if (getValue() == 0.0) {
            Calculator.DISPLAY.setValue(digit);
        } else {
            Calculator.DISPLAY.setValue(v + digit);
        }
    }
    
    private static void setValue(double v) {
        StringBuilder sb = new StringBuilder();
        sb.append(v);
        if (sb.toString().endsWith(".0")) {
            final int l = sb.length();
            sb.delete(l - 2, l);
        }
        Calculator.DISPLAY.setValue(sb.toString());
    }

    private static double getValue() {
        try {
            return Double.parseDouble(Calculator.DISPLAY.getValue());
        } catch (NumberFormatException ex) {
            Calculator.DISPLAY.setValue("err");
            return 0.0;
        }
    }
}
