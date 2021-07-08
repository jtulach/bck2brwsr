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
package org.apidesign.bck2brwsr.ko2brwsr;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import net.java.html.BrwsrCtx;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.Technology;
import org.netbeans.html.json.spi.Transfer;
import org.netbeans.html.json.spi.WSTransfer;
import org.netbeans.html.json.tck.KnockoutTCK;
import org.netbeans.html.ko4j.KO4J;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service = KnockoutTCK.class)
public final class Bck2BrwsrKnockoutImpl extends KnockoutTCK {
    public Bck2BrwsrKnockoutImpl() {
        Bck2BrwsrJavaScriptImpl.init();
    }

    static Class[] createClasses() {
        final Class<?>[] arr = testClasses();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].getSimpleName().startsWith("GC")) {
                arr[i] = Object.class;
            }
        }
        return arr;
    }

    @Override
    public BrwsrCtx createContext() {
        KO4J ko = new KO4J(null);
        return Contexts.newBuilder().
            register(Transfer.class, ko.transfer(), 9).
            register(WSTransfer.class, ko.websockets(), 9).
            register(Technology.class, ko.knockout(), 9).build();
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
            return new URI(prepareWebResource(content, mimeType, parameters));
        } catch (URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String prepareWebResource(String content, String mimeType, String[] parameters) {
        try {
            final URL baseURL = new URL(findBaseURL());
            StringBuilder sb = new StringBuilder();
            sb.append("/dynamic?mimeType=").append(mimeType);
            for (int i = 0; i < parameters.length; i++) {
                sb.append("&param" + i).append("=").append(parameters[i]);
            }
            String mangle = content.replace("\n", "%0a").replace(" ", "%20");
            sb.append("&content=").append(mangle);

            URL query = new URL(baseURL, sb.toString());
            String uri = (String) query.getContent(new Class[] { String.class });
            return uri.trim();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public boolean scheduleLater(int delay, Runnable r) {
        return setTimeout(r, delay);
    }

    @JavaScriptBody(args = { "r", "timeout" }, body =
          ""
        + "if (!!window && !!window.setTimeout) {\n"
        + "  window.setTimeout(function() {\n"
        + "    r.run__V();\n"
        + "  }, timeout);\n"
        + "  return true;\n"
        + "}\n"
        + "return false;\n"
    )
    private static native boolean setTimeout(Runnable r, int timeout);

}
