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
package org.apidesign.bck2brwsr.compact.tck;

import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.apidesign.bck2brwsr.core.JavaScriptPrototype;
import org.apidesign.bck2brwsr.vmtest.BrwsrTest;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

public class NativeTest {
    @BrwsrTest
    public void callNativeMethodReimplementedInJavaScript() throws Exception {
        // invoke constructor of NativeStub to
        // apply methods in NativeStub on top of Native and replace its
        // native method nTexSubImage2D0 with a JavaScript implementation
        NativeStub applyStub = new NativeStub();

        // method Native#nTexSubImage2D0 is now replaced by NativeStub#nTexSubImage2D0
        int value = Native.callTextSubImage();

        if (value != 42) {
            throw new IllegalStateException("Expecting 42, but was: " + value);
        }
    }

    @Factory
    public static Object[] create() {
        return VMTest.create(NativeTest.class);
    }
}

final class Native {
    private static native int nTexSubImage2D0(int target, int level,
        int xoffset, int yoffset, int width, int height, int format,
        int type, Object pixels, int pixelsByteOffset);

    public static int callTextSubImage() {
        return nTexSubImage2D0(0, 0, 0, 0, 0, 0, 0, 0, null, 0);
    }
}


@JavaScriptPrototype(prototype = "", container = "vm.org_apidesign_bck2brwsr_compact_tck_Native(false)")
final class NativeStub {
    public NativeStub() {
    }

    @JavaScriptBody(body = "return 42;", args = { "target", "level", "xoffset", "yoffset", "width", "height", "format", "type", "pixels", "pixelsByteOffset" })
    private static native int nTexSubImage2D0(int target, int level,
            int xoffset, int yoffset, int width, int height, int format,
            int type, Object pixels, int pixelsByteOffset);

}
