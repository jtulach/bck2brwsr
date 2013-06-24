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

import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.html.context.spi.Contexts;
import org.apidesign.html.json.spi.Technology;
import org.apidesign.html.json.spi.Transfer;
import org.openide.util.lookup.ServiceProvider;

/** This is an implementation package - just
 * include its JAR on classpath and use official {@link Context} API
 * to access the functionality.
 * <p>
 * Provides binding between models and <a href="http://bck2brwsr.apidesign.org">
 * Bck2Brwsr</a> VM.
 * Registers {@link ContextProvider}, so {@link ServiceLoader} can find it.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service = Contexts.Provider.class)
public final class BrwsrCtxPrvdr implements Contexts.Provider {

    @Override
    public void fillContext(Contexts.Builder context, Class<?> requestor) {
        if (bck2BrwsrVM()) {
            context.register(Technology.class, BrwsrCtxImpl.DEFAULT, 50).
            register(Transfer.class, BrwsrCtxImpl.DEFAULT, 50);
        }
    }
    
    @JavaScriptBody(args = {  }, body = "return true;")
    private static boolean bck2BrwsrVM() {
        return false;
    }
}
