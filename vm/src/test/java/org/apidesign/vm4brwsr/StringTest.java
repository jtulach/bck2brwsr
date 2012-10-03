package org.apidesign.vm4brwsr;

import javax.script.Invocable;
import javax.script.ScriptException;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

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

    @Test public void toStringConcatenation() throws Exception {
        assertExec(
            "Five executions should generate 5Hello World!",
            "org_apidesign_vm4brwsr_StringSample_toStringTestLjava_lang_StringI",
            "5Hello World!", 5
        );
    }
    
    private static void assertExec(String msg, String methodName, Object expRes, Object... args) throws Exception {
        StringBuilder sb = new StringBuilder();
        Invocable i = StaticMethodTest.compileClass(sb, 
            "org/apidesign/vm4brwsr/StringSample",
            "java/lang/String"
        );
        
        Object ret = null;
        try {
            ret = i.invokeFunction(methodName, args);
        } catch (ScriptException ex) {
            fail("Execution failed in " + sb, ex);
        } catch (NoSuchMethodException ex) {
            fail("Cannot find method in " + sb, ex);
        }
        if (ret == null && expRes == null) {
            return;
        }
        if (expRes.equals(ret)) {
            return;
        }
        assertEquals(ret, expRes, msg + "was: " + ret + "\n" + sb);
        
    }
    
}
