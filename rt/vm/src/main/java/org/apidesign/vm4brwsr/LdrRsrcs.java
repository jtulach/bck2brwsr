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
package org.apidesign.vm4brwsr;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/** Implementation of Resources that delegates to some class loader.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class LdrRsrcs implements Bck2Brwsr.Resources {
    private final ClassLoader loader;
    private final boolean skipRtJar;

    LdrRsrcs(ClassLoader loader, boolean skipRtJar) {
        this.loader = loader;
        this.skipRtJar = skipRtJar;
    }

    @Override
    public InputStream get(String name) throws IOException {
        Enumeration<URL> en = loader.getResources(name);
        URL u = null;
        while (en.hasMoreElements()) {
            u = en.nextElement();
        }
        if (u == null) {
            throw new IOException("Can't find " + name);
        }
        if (skipRtJar && u.toExternalForm().contains("lib/rt.jar!")) {
            return null;
        }
        return u.openStream();
    }
}
