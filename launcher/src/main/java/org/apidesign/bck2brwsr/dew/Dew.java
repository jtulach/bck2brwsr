/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.bck2brwsr.dew;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
    private String html = "<html><body>\n"
        + " <button id='btn'>Hello!</button>\n"
        + " <hr/>\n"
        + "\n"
        + "\n"
        + "\n"
        + " <script src=\"/bck2brwsr.js\"></script>\n"
        + " <script type=\"text/javascript\">\n"
        + "   function ldCls(res) {\n"
        + "     var request = new XMLHttpRequest();\n"
        + "     request.open('GET', '/dew/classes/' + res, false);\n"
        + "     request.send();\n"
        + "     var arr = eval('(' + request.responseText + ')');\n"
        + "     return arr;\n"
        + "   }\n"
        + " //  var vm = new bck2brwsr(ldCls);\n"
        + " //  vm.loadClass('bck2brwsr.demo.Index');\n"
        + " </script>\n"
        + "</body></html>\n";
    private String java = "package bck2brwsr.demo;\n"
                + "import org.apidesign.bck2brwsr.htmlpage.api.*;\n"
            + "@Page(xhtml=\"index.html\", className=\"Index\")\n"
            + "class X {\n"
            + "   @OnClick(id=\"btn\") static void clcs() {\n"
            + "     Index.BTN.setDisabled(true);\n"
            + "   }\n"
            + "}\n";
    private Compile data;

    @Override
    public void service(Request request, Response response) throws Exception {
        
        if ( request.getMethod() == Method.POST ) {
            InputStream is = request.getInputStream();
            JSONTokener tok = new JSONTokener(new InputStreamReader(is));
            JSONObject obj = new JSONObject(tok);
            html = obj.getString("html");
            java = obj.getString("java");
            
            Compile res = Compile.create(html, java);
            List<Diagnostic<? extends JavaFileObject>> err = res.getErrors();
            if (err.isEmpty()) {
                data = res;
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
            if (data == null) {
                //data = Compile.create(html, java);
            }
            r = r.substring(8);
            byte[] is = data == null ? null : data.get(r);
            if (is == null) {
                is = new byte[0];
            }
            OutputStream os = response.getOutputStream();
            copyStream(new ByteArrayInputStream(is), os, request.getRequestURL().toString() );
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
