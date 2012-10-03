package org.apidesign.vm4brwsr;

import org.apidesign.bck2brwsr.core.ExtraJavaScript;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ExtraJavaScript(resource="/org/apidesign/vm4brwsr/emulation/java_lang_String.js")
public class StringSample {
    public static final String HELLO = "Hello World!";
    private static int counter;
    
    private final int cnt;
    public StringSample() {
        cnt = ++counter;
    }
    
    
    public static char sayHello(int indx) {
        return HELLO.charAt(indx);
    }
    
    public static String fromChars(char a, char b, char c) {
        char[] arr = { a, b, c };
        return new String(arr).toString();
    }
    
    public static String toStringTest(int howMuch) {
        StringSample ss = null;
        for (int i = 0; i < howMuch; i++) {
            ss = new StringSample();
        }
        return ss.toString();
    }

    @Override
    public String toString() {
        return HELLO + cnt;
    }
}
