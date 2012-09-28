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
