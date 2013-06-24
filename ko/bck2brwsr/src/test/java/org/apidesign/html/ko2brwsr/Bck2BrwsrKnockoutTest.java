/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */
package org.apidesign.html.ko2brwsr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import net.java.html.BrwsrCtx;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;
import org.apidesign.html.json.tck.KOTest;
import org.apidesign.html.json.tck.KnockoutTCK;
import org.openide.util.lookup.ServiceProvider;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service = KnockoutTCK.class)
public final class Bck2BrwsrKnockoutTest extends KnockoutTCK {
    @Factory public static Object[] create() {
        return VMTest.newTests().
            withClasses(testClasses()).
            withLaunchers("bck2brwsr").
            withTestAnnotation(KOTest.class).
            build();
    }
    
    @Override
    public BrwsrCtx createContext() {
        return Contexts.newBuilder().
            register(Transfer.class, BrwsrCtxImpl.DEFAULT, 9).
            register(Technology.class, BrwsrCtxImpl.DEFAULT, 9).build();
    }


    
    @Override
    public Object createJSON(Map<String, Object> values) {
        Object json = createJSON();
        
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            putValue(json, entry.getKey(), entry.getValue());
        }
        return json;
    }

    @JavaScriptBody(args = {}, body = "return new Object();")
    private static native Object createJSON();

    @JavaScriptBody(args = { "json", "key", "value" }, body = "json[key] = value;")
    private static native void putValue(Object json, String key, Object value);

    @Override
    public Object executeScript(String script, Object[] arguments) {
        return execScript(script, arguments);
    }
    
    @JavaScriptBody(args = { "s", "args" }, body = 
        "var f = new Function(s); return f.apply(null, args);"
    )
    private static native Object execScript(String s, Object[] arguments);
    
    @JavaScriptBody(args = {  }, body = 
          "var h;"
        + "if (!!window && !!window.location && !!window.location.href)\n"
        + "  h = window.location.href;\n"
        + "else "
        + "  h = null;"
        + "return h;\n"
    )
    private static native String findBaseURL();
    
    @Override
    public URI prepareURL(String content, String mimeType, String[] parameters) {
        try {
            final URL baseURL = new URL(findBaseURL());
            StringBuilder sb = new StringBuilder();
            sb.append("/dynamic?mimeType=").append(mimeType);
            for (int i = 0; i < parameters.length; i++) {
                sb.append("&param" + i).append("=").append(parameters[i]);
            }
            String mangle = content.replace("\n", "%0a")
                .replace("\"", "\\\"").replace(" ", "%20");
            sb.append("&content=").append(mangle);

            URL query = new URL(baseURL, sb.toString());
            String uri = (String) query.getContent(new Class[] { String.class });
            URI connectTo = new URI(uri.trim());
            return connectTo;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        } catch (URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }    
}
