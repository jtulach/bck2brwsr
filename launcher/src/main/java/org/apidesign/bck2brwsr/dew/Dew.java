/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.bck2brwsr.dew;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

/**
 *
 * @author phrebejk
 */
public class Dew extends HttpHandler {

    @Override
    public void service(Request request, Response response) throws Exception {
        String r = request.getHttpHandlerPath();
        if (r == null || r.equals("/")) {
            r = "index.html";
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
    
}
