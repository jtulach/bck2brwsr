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

public class DejsniReaderTest {

    @Test
    public void test1() throws Exception {
        String s = "class Test {\n" +
                "    /** javadoc */\n" +
                "    public native void test() /*-{\n" +
                "        // body\n" +
                "    }-*/;\n" +
                "}\n";

        String expected = " import org.apidesign.bck2brwsr.core.JavaScriptBody;\n"
              + "class Test {\n" +
                "\n" +
                "    /** javadoc */\n" +
                "    @JavaScriptBody(args = {}, body = \"\\n        // body\\n  \")\n" +
                "    public native void test();\n" +
                "}\n";
        
          HintTest.create()
                .input(s)
                .classpath(FileUtil.getArchiveRoot(JavaScriptBody.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(JSNI2JavaScriptBody.class)
                .findWarning("2:23-2:27:verifier:" + Bundle.ERR_JSNI2JavaScriptBody())
                .applyFix()
                .assertCompilable()
                .assertOutput(expected);
    }


    @Test
    public void test2() throws Exception {
        String s = "class Test {\n" +
                "    /** javadoc */\n" +
                "    @SuppressWarnings(\"unused\")\n" +
                "    // comment\n" +
                "    public native void test() /*-{\n" +
                "        // body\n" +
                "    }-*/;\n" +
                "}\n";

        String expected = " import org.apidesign.bck2brwsr.core.JavaScriptBody;\n"
              + "class Test {\n" +
                "\n" +
                "    /** javadoc */\n" +
                "    @SuppressWarnings(\"unused\")\n" +
                "    // comment\n" +
                "    @JavaScriptBody(args = {}, body = \"\\n        // body\\n  \")\n" +
                "    public native void test();\n" +
                "}\n";
          HintTest.create()
                .input(s)
                .classpath(FileUtil.getArchiveRoot(JavaScriptBody.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(JSNI2JavaScriptBody.class)
                .findWarning("4:23-4:27:verifier:" + Bundle.ERR_JSNI2JavaScriptBody())
                .applyFix()
                .assertCompilable()
                .assertOutput(expected);
    }
}