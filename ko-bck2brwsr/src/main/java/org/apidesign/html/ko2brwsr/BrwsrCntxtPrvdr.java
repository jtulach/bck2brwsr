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

import net.java.html.json.Context;
import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.html.json.spi.ContextProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
@ServiceProvider(service = ContextProvider.class)
public final class BrwsrCntxtPrvdr implements ContextProvider {
    @Override
    public Context findContext(Class<?> requestor) {
        return bck2BrwsrVM() ? BrwsrCntxt.DEFAULT : null;
    }
    
    @JavaScriptBody(args = {  }, body = "return true;")
    private static boolean bck2BrwsrVM() {
        return false;
    }
}
