/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2018 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.kosample.js;

import java.io.Closeable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import net.java.html.boot.script.Scripts;
import org.netbeans.html.boot.spi.Fn;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
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
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine graalJs = sem.getEngineByName("Graal.js");
        assertNotNull("Graal.js engine found", graalJs);
        jsEngine = Fn.activate(Scripts.newPresenter().engine(graalJs).build());
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
        
        Dialogs.confirmByUser("Hello", callback);
        
        assertEquals(callback.called, 1, "One immediate callback");
    }
}
