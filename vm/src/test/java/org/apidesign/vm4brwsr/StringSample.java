package org.apidesign.vm4brwsr;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class StringSample {
    public static final String HELLO = "Hello World!";
    
    public static char sayHello(int indx) {
        return HELLO.charAt(indx);
    }
}
