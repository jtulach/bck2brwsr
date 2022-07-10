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
package org.apidesign.bck2brwsr.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Lightweight server to launch Bck2Brwsr applications and tests.
 * Supports execution in native browser as well as Java's internal 
 * execution engine.
 */
final class Bck2BrwsrLauncher extends BaseHTTPLauncher {
    private Set<String> testClasses = new HashSet<String>();
    
    public Bck2BrwsrLauncher(String cmd) {
        super(cmd);
    }
    
    @Override
    String harnessResource() {
        return "org/apidesign/bck2brwsr/launcher/harness.html";
    }

    @Override
    Object compileJar(URL jar, URL precompiled) throws IOException {
        if (precompiled != null) {
            LOG.log(Level.INFO, "Found precompiled JAR version of {0} at {1}. Using.", new Object[]{jar, precompiled});
            return precompiled.openStream();
        }
        File f;
        try {
            f = new File(jar.toURI());
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
        final String precompile = System.getProperty("vmtest.precompiled");
        if (precompile != null && Pattern.compile(precompile).matcher(jar.toString()).find()) {
            throw new IOException("Compilation of " + jar + " forbidden");
        }
        LOG.log(Level.INFO, "No precompiled version for {0} found. Compiling.", jar);
        return CompileCP.compileJAR(f, testClasses);
    }

    @Override
    public InvocationContext createInvocation(Class<?> clazz, String method) {
        testClasses.add(clazz.getName().replace('.', '/'));
        return super.createInvocation(clazz, method);
    }

    @Override String compileFromClassPath(URL f, Res loader) throws IOException {
        return CompileCP.compileFromClassPath(f, loader);
    }
    
    @Override
    void generateBck2BrwsrJS(StringBuilder sb, final Res loader, String url, boolean unitTestMode) throws IOException {
        String b2b = System.getProperty("bck2brwsr.js");
        if (b2b != null) {
            LOG.log(Level.INFO, "Serving bck2brwsr.js from {0}", b2b);
            URL bu;
            try {
                bu = new URL(b2b);
            } catch (MalformedURLException ex) {
                File f = new File(b2b);
                if (f.exists()) {
                    bu = f.toURI().toURL();
                } else {
                    throw ex;
                }
            }
            try (Reader r = new InputStreamReader(bu.openStream())) {
                char[] arr = new char[4096];
                for (;;) {
                   int len = r.read(arr);
                   if (len == -1) {
                       break;
                   }
                   sb.append(arr, 0, len);
                }
            }
        } else {
            LOG.log(Level.INFO, "Generating bck2brwsr.js from scratch", b2b);
            CompileCP.compileVM(sb, loader);
        }
        sb.append("""
            (function WrapperVM(global) {
              var cache = {};
              var empty = {};
              function ldCls(res, skip) {
                var c = cache[res];
                if (c) {
                  if (c[skip] === empty) return null;
                  if (c[skip] !== undefined) return c[skip];
                } else {
                  cache[res] = c = new Array();
                }
                var request = new XMLHttpRequest();
                request.open('GET', '/classes/' + res + '?skip=' + skip, false);
                request.send();
                if (request.status !== 200) {
                  c[skip] = null;
                  return null;
                }
                var arr = eval(request.responseText);
                if (arr === null) c[skip] = empty;
                else c[skip] = arr;
                return arr;
              }
              var prevvm = global.bck2brwsr;
              global.bck2brwsr = function() {
                var args = Array.prototype.slice.apply(arguments);
                args.unshift(ldCls);
                return prevvm.apply(null, args);
              };
              global.bck2brwsr.register = prevvm.register;
            })(this);
            """);

        if (unitTestMode) {
            sb.append("var vm = bck2brwsr();\n");
            sb.append("var cnsl = vm.loadClass('org.apidesign.bck2brwsr.launcher.impl.Console');\n");
            int last = url.lastIndexOf('/');
            url = url.substring(0, last + 1);
            sb.append("var res = cnsl.invoke('harness', '").append(url).append("/data');");
        }

        LOG.log(Level.INFO, "Serving bck2brwsr.js", b2b);
    }

}
