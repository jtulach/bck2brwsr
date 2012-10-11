package org.apidesign.vm4brwsr;

import javax.script.Invocable;
import javax.script.ScriptException;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeClass;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class StringTest {
    @Test public void firstChar() throws Exception {
        assertExec(
            "First char in Hello is H",
            "org_apidesign_vm4brwsr_StringSample_sayHelloCI",
            "H", 0
        );
    }

    @Test public void fromChars() throws Exception {
        assertExec(
            "First char in Hello is ABC",
            "org_apidesign_vm4brwsr_StringSample_fromCharsLjava_lang_StringCCC",
            "ABC", 'A', 'B', 'C'
        );
    }

    @Test(timeOut=10000) public void toStringConcatenation() throws Exception {
        assertExec(
            "Five executions should generate 5Hello World!",
            "org_apidesign_vm4brwsr_StringSample_toStringTestLjava_lang_StringI",
            "Hello World!5", 5
        );
    }
    @Test public void toStringConcatenationJava() throws Exception {
        assertEquals("Hello World!5", StringSample.toStringTest(5));
    }
    private static CharSequence codeSeq;
    private static Invocable code;
    
    @BeforeClass 
    public void compileTheCode() throws Exception {
        StringBuilder sb = new StringBuilder();
        code = StaticMethodTest.compileClass(sb, 
            "org/apidesign/vm4brwsr/StringSample",
            "java/lang/String"
        );
        codeSeq = sb;
    }
    
    private static void assertExec(String msg, String methodName, Object expRes, Object... args) throws Exception {
        Object ret = null;
        try {
            ret = code.invokeFunction(methodName, args);
        } catch (ScriptException ex) {
            fail("Execution failed in\n" + codeSeq, ex);
        } catch (NoSuchMethodException ex) {
            fail("Cannot find method in\n" + codeSeq, ex);
        }
        if (ret == null && expRes == null) {
            return;
        }
        if (expRes.equals(ret)) {
            return;
        }
        assertEquals(ret, expRes, msg + "was: " + ret + "\n" + codeSeq);
        
    }
    
}
