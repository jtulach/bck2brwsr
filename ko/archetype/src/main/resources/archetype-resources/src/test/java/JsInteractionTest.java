package ${package};

import java.io.Closeable;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apidesign.html.boot.spi.Fn;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Tests for behavior of @JavaScriptBody methods. Set your JavaScript 
 * environment up (for example define <code>alert</code> or use some
 * emulation library like <em>env.js</em>), register script presenter 
 * and then you can call methods that deal with JavaScript in your tests.
 */
public class JsInteractionTest {
    private Closeable jsEngine;
    @BeforeMethod public void initializeJSEngine() throws Exception {
        jsEngine = Fn.activate(new ScriptPresenter());
    }
    
    @AfterMethod public void shutdownJSEngine() throws Exception {
        jsEngine.close();
    }
    
    @Test public void testCallbackFromJavaScript() throws Exception {
        class R implements Runnable {
            int called;

            @Override
            public void run() {
                called++;
            }
        }
        R callback = new R();
        
        DataModel.confirmByUser("Hello", callback);
        
        assertEquals(callback.called, 1, "One immediate callback");
    }

    private static class ScriptPresenter implements Fn.Presenter {
        private final ScriptEngine eng;
        
        public ScriptPresenter() throws ScriptException {
            eng = new ScriptEngineManager().getEngineByName("javascript");
            eng.eval("function alert(msg) { Packages.java.lang.System.out.println(msg); };");
        }

        @Override
        public Fn defineFn(String code, String... names) {
            StringBuilder sb = new StringBuilder();
            sb.append("(function() {");
            sb.append("  return function(");
            String sep = "";
            for (String n : names) {
                sb.append(sep).append(n);
                sep = ",";
            }
            sb.append(") {\n");
            sb.append(code);
            sb.append("};");
            sb.append("})()");
            
            final Object fn;
            try {
                fn = eng.eval(sb.toString());
            } catch (ScriptException ex) {
                throw new IllegalStateException(ex);
            }
            return new Fn(this) {
                @Override
                public Object invoke(Object thiz, Object... args) throws Exception {
                    List<Object> all = new ArrayList<Object>(args.length + 1);
                    all.add(thiz == null ? fn : thiz);
                    for (int i = 0; i < args.length; i++) {
                        all.add(args[i]);
                    }
                    Object ret = ((Invocable)eng).invokeMethod(fn, "call", all.toArray()); // NOI18N
                    return fn.equals(ret) ? null : thiz;
                }
            };
        }

        @Override
        public void displayPage(URL page, Runnable onPageLoad) {
            // not really displaying anything
            onPageLoad.run();
        }

        @Override
        public void loadScript(Reader code) throws Exception {
            eng.eval(code);
        }
    }
}
