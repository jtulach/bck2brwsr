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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.apidesign.vm4brwsr.Bck2Brwsr;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author phrebejk
 */
final class Dew extends HttpHandler implements Bck2Brwsr.Resources {
    private Compile data;

    public static void main(String... args) throws Exception {
        DewLauncher l = new DewLauncher(null);
        l.addClassLoader(DewLauncher.class.getClassLoader());
        final Dew dew = new Dew();
        HttpServer s = l.initServer(dew);
        s.getServerConfiguration().addHttpHandler(dew, "/dew/");
        l.launchServerAndBrwsr(s, "/dew/");
        System.in.read();
    }
    
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
                response.getOutputStream().write("[]".getBytes());
                response.setStatus(HttpStatus.OK_200);
            } else {
                
                JSONArray errors = new JSONArray();
                
                for (Diagnostic<? extends JavaFileObject> d : err) {
                    JSONObject e = new JSONObject();
                    e.put("col", d.getColumnNumber());
                    e.put("line", d.getLineNumber());
                    e.put("kind", d.getKind().toString());
                    e.put("msg", d.getMessage(Locale.ENGLISH));
                    errors.put(e);
                }
                
                errors.write(response.getWriter());                
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
            if (data != null) {
                response.getOutputBuffer().write(data.getHtml());
            }
            response.setStatus(HttpStatus.OK_200);
            return;
        }
        
        if (r.startsWith("/")) {
            r = r.substring(1);
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
    
    static void copyStream(InputStream is, OutputStream os, String baseURL) throws IOException {
        for (;;) {
            int ch = is.read();
            if (ch == -1) {
                break;
            }
            os.write(ch);            
        }
    }

    @Override
    public InputStream get(String r) throws IOException {
        byte[] arr = data == null ? null : data.get(r);
        return arr == null ? null : new ByteArrayInputStream(arr);
    }
}
