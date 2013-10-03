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
