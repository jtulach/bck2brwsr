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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class UtilBase {

    static String findOwnVersion() {
        try (final InputStream is = UtilBase.class.getResourceAsStream("/META-INF/maven/org.apidesign.bck2brwsr/bck2brwsr-maven-plugin/pom.properties")) {
            if (is == null) {
                return "1.0-SNAPSHOT";
            }
            Properties p = new Properties();
            p.load(is);
            String version = p.getProperty("version");
            if (version == null) {
                throw new IllegalStateException("Cannot find version");
            }
            return version;
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read version", ex);
        }
    }

    static void verifyIndexHtml(File directory, String mainJS, String mainClass) throws IOException {
        File index = new File(directory, "index.html");
        if (!index.exists()) {
            try (final FileWriter w = new FileWriter(index)) {
                w.write("" +
                    "<html>\n" +
                    "    <head>\n" +
                    "        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                    "    </head>" +
                    "<body>\n" +
                    UtilBase.invokeSnippet(mainJS, mainClass) +
                    "</body>\n" +
                    "</html>\n"
                );
            }
        } else {
            byte[] arr = Files.readAllBytes(index.toPath());
            String data = new String(arr, "UTF-8");
            String newText = UtilBase.augmentedIndexPage(data, mainJS, mainClass);
            if (!newText.equals(data)) {
                try (final Writer w = new OutputStreamWriter(new FileOutputStream(index), "UTF-8")) {
                    w.write(newText);
                }
            }
        }
    }
    private UtilBase() {
    }

    static String augmentedIndexPage(String data, String mainJS, String mainClass) {
        Pattern loadClass = Pattern.compile("loadClass *\\( *[\"']" + mainClass);
        Matcher loadClassMatcher = loadClass.matcher(data);
        if (loadClassMatcher.find()) {
            return data;
        }

        int endOfBody = data.toLowerCase().lastIndexOf("</body>");
        String newText =
            data.substring(0, endOfBody) +
            invokeSnippet(mainJS, mainClass) +
            data.substring(endOfBody);
        return newText;
    }

    static String invokeSnippet(String mainJS, String mainClass) {
        return "\n"
            + "\n"
            + "\n"
            + "<script src='bck2brwsr.js'></script>\n"
            + "<script>\n" + "var vm = bck2brwsr('" + mainJS + "');\n"
            + "vm.loadClass('" + mainClass + "', function(mainClass) {\n"
            + "  mainClass.invoke('main');\n"
            + "});\n"
            + "</script>\n"
            + "\n"
            + "\n";
    }
}
