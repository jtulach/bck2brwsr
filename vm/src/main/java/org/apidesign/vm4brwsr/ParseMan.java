/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
import org.apidesign.bck2brwsr.emul.lang.ManifestInputStream;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class ParseMan extends ManifestInputStream {
    private String cp;
    private String mc;

    public ParseMan(InputStream is) throws IOException {
        super(is);
        readAttributes(new byte[512]);
    }

    @Override
    protected String putValue(String key, String value) {
        if ("Class-Path".equals(key)) {
            cp = value;
        }
        if ("Main-Class".equals(key)) {
            mc = value;
        }
        return null;
    }
    
    String getMainClass() {
        return mc;
    }

    @Override
    public String toString() {
        return cp;
    }
    
}
