package org.apidesign.bck2brwsr.mavenhtml;

import org.apidesign.bck2brwsr.htmlpage.api.OnClick;
import org.apidesign.bck2brwsr.htmlpage.api.Page;

@Page(xhtml="Calculator.xhtml")
public class App {
    private static final int OP_PLUS = 1;
    private static final int OP_MINUS = 2;
    private static final int OP_MUL = 3;
    private static final int OP_DIV = 4;
    
    static double memory = 0;
    static int operation = 0;
    
    
    
    @OnClick(id="clear")
    static void clear() {
        setValue(0.0);
    }
    
    private static void setValue(double v) {
        StringBuilder sb = new StringBuilder();
        sb.append(v);
        Calculator.DISPLAY.setValue(sb.toString());
    }
    
    private static double getValue() {
        return Double.parseDouble(Calculator.DISPLAY.getValue());
    }
    
    @OnClick(id="plus")
    static void plus() {
        memory = getValue();
        operation = OP_PLUS;
        setValue(0.0);
    }
    
    @OnClick(id="minus")
    static void minus() {
        memory = getValue();
        operation = OP_MINUS;
        setValue(0.0);
    }
    
    @OnClick(id="mul")
    static void mul() {
        memory = getValue();
        operation = OP_MUL;
        setValue(0.0);
    }
    
    @OnClick(id="result")
    static void computeTheValue() {
        switch (operation) {
            case 0: break;
            case OP_PLUS: setValue(memory + getValue()); break;
            case OP_MINUS: setValue(memory - getValue()); break;
            case OP_MUL: setValue(memory * getValue()); break;
        }
    }
    
    @OnClick(id={"n0", "n1", "n2", "n3", "n4", "n5", "n6", "n7", "n8", "n9"}) 
    static void addDigit(String digit) {
        digit = digit.substring(1);
        String v = Calculator.DISPLAY.getValue();
        if ("0".equals(v) || v == null) {
            Calculator.DISPLAY.setValue(digit);
        } else {
            Calculator.DISPLAY.setValue(v + digit);
        }
    }
}
