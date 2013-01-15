/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.bck2brwsr.dew;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Logger;
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
    private static String html = "Nazdar!";

    @Override
    public void service(Request request, Response response) throws Exception {
        
        if ( request.getMethod() == Method.POST ) {
            InputStream is = request.getInputStream();
            JSONTokener tok = new JSONTokener(new InputStreamReader(is));
            JSONObject obj = new JSONObject(tok);
            html = obj.getString("html");
            LOG.info(html);
            
            response.getOutputStream().write("[]".getBytes());
            response.setStatus(HttpStatus.OK_200);
            
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
