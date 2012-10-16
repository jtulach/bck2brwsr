package org.apidesign.vm4brwsr;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
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
        counter = 0;
        StringSample ss = null;
        for (int i = 0; i < howMuch; i++) {
            ss = new StringSample();
        }
        return ss.toString().toString();
    }
    
    public static String concatStrings() {
        return (toStringTest(1) + "Ahoj").toString();
    }

    @Override
    public String toString() {
        return HELLO + cnt;
    }
}
