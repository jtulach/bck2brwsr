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

package org.apidesign.bck2brwsr.mojo;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import org.apidesign.bck2brwsr.launcher.Launcher;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.Project;

public class ShowTask extends DefaultTask {
    void show(final Project p) throws IOException {
        File web = new File(p.getBuildDir(), "web");
        web.mkdirs();
        File index = new File(web, "index.html");
        if (!index.exists()) {
            String mainClass = (String) p.getProperties().get("mainClassName");
            if (mainClass == null) {
                throw new GradleScriptException("Define property ext.mainClassName in the project", null);
            }
            try (FileWriter w = new FileWriter(index)) {
                w.write(""
                    + "<html>\n"
                    + "<body>\n"
                    + "<script src='bck2brwsr.js'></script>\n"
                    + "<script>\n"
                    + "var vm = bck2brwsr('main.js');\n"
                    + "vm.loadClass('" + mainClass + "', function(mainClass) {\n"
                    + "  mainClass.invoke('main');\n"
                    + "});\n"
                    + "</script>\n"
                    + "</body>\n"
                    + "</html>\n"
                );
            }
        }

        try (Closeable httpServer = Launcher.showDir("bck2brwsr", web, null, "index.html")) {
            if (httpServer instanceof Flushable) {
                ((Flushable) httpServer).flush();
            } else {
                System.in.read();
            }
        }
    }
}
