/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.bck2brwsr.dew;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class JFO implements JavaFileObject {
    private final String text;
    private final String name;

    public JFO(String text, String name) {
        this.text = text;
        this.name = name;
    }

    @Override
    public Kind getKind() {
        return Kind.SOURCE;
    }

    @Override
    public boolean isNameCompatible(String simpleName, Kind kind) {
        return false;
    }

    @Override
    public NestingKind getNestingKind() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Modifier getAccessLevel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public URI toUri() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        return new StringReader(text);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return text;
    }

    @Override
    public Writer openWriter() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public long getLastModified() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean delete() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
