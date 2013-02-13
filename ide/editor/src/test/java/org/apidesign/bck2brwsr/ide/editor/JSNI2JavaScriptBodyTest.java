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
package org.apidesign.bck2brwsr.ide.editor;

import org.apidesign.bck2brwsr.core.JavaScriptBody;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.filesystems.FileUtil;
import org.testng.annotations.Test;

public class JSNI2JavaScriptBodyTest {

    @Test
    public void testFixWorking() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public native void run(int a) /*-{ this.a = a; }-*/;\n" +
                       "}\n")
                .classpath(FileUtil.getArchiveRoot(JavaScriptBody.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(JSNI2JavaScriptBody.class)
                .findWarning("2:23-2:26:verifier:" + Bundle.ERR_JSNI2JavaScriptBody())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import org.apidesign.bck2brwsr.core.JavaScriptBody;\n" +
                              "public class Test {\n" +
                              "    @JavaScriptBody(body = \" this.a = a; \", args = {\"this\", \"a\"})\n" +
                              "    public native void run(int a);\n" +
                              "}\n");
    }
}
