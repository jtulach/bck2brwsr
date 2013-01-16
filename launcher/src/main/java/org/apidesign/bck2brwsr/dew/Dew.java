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
package org.apidesign.bck2brwsr.dew;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.logging.Logger;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author phrebejk
 */
public class Dew extends HttpHandler {
    private String html = "";
    private Compile data;

    @Override
    public void service(Request request, Response response) throws Exception {
        
        if ( request.getMethod() == Method.POST ) {
            InputStream is = request.getInputStream();
            JSONTokener tok = new JSONTokener(new InputStreamReader(is));
            JSONObject obj = new JSONObject(tok);
            String tmpHtml = obj.getString("html");
            String tmpJava = obj.getString("java");
            
            Compile res = Compile.create(tmpHtml, tmpJava);
            List<Diagnostic<? extends JavaFileObject>> err = res.getErrors();
            if (err.isEmpty()) {
                data = res;
                html = tmpHtml;
                response.getOutputStream().write("[]".getBytes());
                response.setStatus(HttpStatus.OK_200);
            } else {
                response.getOutputStream().write(("[errors:'" + err + "']").getBytes());
                response.setStatus(HttpStatus.PRECONDITION_FAILED_412);
            }
            
            return;
        }
        
        String r = request.getHttpHandlerPath();
        if (r == null || r.equals("/")) {
            r = "index.html";
        }
        if (r.equals("/result.html")) {
            response.setContentType("text/html");
            response.getOutputBuffer().write(html);
            response.setStatus(HttpStatus.OK_200);
            return;
        }
        
        if (r.startsWith("/")) {
            r = r.substring(1);
        }
        if (r.startsWith("classes/")) {
            r = r.substring(8);
            byte[] arr = data == null ? null : data.get(r);
            if (arr == null) {
                response.setError();
                response.setDetailMessage("No data for " + r + " yet!");
                return;
            }
            try (Writer w = response.getWriter()) {
                response.setContentType("text/javascript");
                w.append("[");
                for (int i = 0; i < arr.length; i++) {
                    int b = arr[i];
                    if (b == -1) {
                        break;
                    }
                    if (i > 0) {
                        w.append(", ");
                    }
                    if (i % 20 == 0) {
                        w.write("\n");
                    }
                    if (b > 127) {
                        b = b - 256;
                    }
                    w.append(Integer.toString(b));
                }
                w.append("\n]");
            }
            return;
        }
        
        if (r.endsWith(".html") || r.endsWith(".xhtml")) {
            response.setContentType("text/html");
        }
        OutputStream os = response.getOutputStream();
        try (InputStream is = Dew.class.getResourceAsStream(r) ) {
            copyStream(is, os, request.getRequestURL().toString() );
        } catch (IOException ex) {
            response.setDetailMessage(ex.getLocalizedMessage());
            response.setError();
            response.setStatus(404);
        }
    }
    private static final Logger LOG = Logger.getLogger(Dew.class.getName());
    
    static void copyStream(InputStream is, OutputStream os, String baseURL) throws IOException {
        for (;;) {
            int ch = is.read();
            if (ch == -1) {
                break;
            }
            os.write(ch);            
        }
    }
    
}
