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

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
class VMinVM extends ByteCodeToJavaScript {
    private VMinVM(Appendable out) {
        super(out);
    }
    
    static String toJavaScript(byte[] is) throws IOException {
        StringBuilder sb = new StringBuilder();
        new VMinVM(sb).compile(new ByteArrayInputStream(is));
        return sb.toString().toString();
    }

    @Override
    protected boolean requireReference(String internalClassName) {
        return false;
    }

    @Override
    protected void requireScript(String resourcePath) {
    }

    @Override
    String getVMObject() {
        return "global";
    }
}
