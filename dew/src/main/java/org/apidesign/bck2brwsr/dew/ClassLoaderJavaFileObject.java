/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apidesign.bck2brwsr.dew;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 *
 * @author Tomas Zezula
 */
class ClassLoaderJavaFileObject extends BaseFileObject {

    ClassLoaderJavaFileObject(final String path) {
        super(path, getKind(path));
    }    

    @Override
    public InputStream openInputStream() throws IOException {
        final InputStream in = getClass().getClassLoader().getResourceAsStream(path.substring(1));
        if (in == null) {
            throw new FileNotFoundException(path);
        }
        return in;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        throw new UnsupportedOperationException("Read Only FileObject");    //NOI18N
    }

    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        return new InputStreamReader(openInputStream());
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        final BufferedReader in = new BufferedReader(openReader(ignoreEncodingErrors));
        try {
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append('\n');    //NOI18N
            }
            return sb.toString();
        } finally {
            in.close();
        }
    }

    @Override
    public Writer openWriter() throws IOException {
        return new OutputStreamWriter(openOutputStream());
    }

    @Override
    public long getLastModified() {
        return System.currentTimeMillis();
    }

    @Override
    public boolean delete() {
        return false;
    }

}
