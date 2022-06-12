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
package org.apidesign.vm4brwsr;

import java.io.IOException;
import org.apidesign.bck2brwsr.core.ExtraJavaScript;

@ExtraJavaScript(processByteCode = false, resource="")
final class LineCountingAppendable implements Appendable {
    private final Appendable wrapped;
    private int lineNumber;
    private int columnNumber;

    LineCountingAppendable(Appendable wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        wrapped.append(csq);
        count(csq, 0, csq.length());
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        wrapped.append(csq, start, end);
        count(csq, start, end);
        return this;
    }

    @Override
    public Appendable append(char c) throws IOException {
        wrapped.append(c);
        count(c);
        return this;
    }

    private void count(CharSequence csq, int start, int end) {
        int lineNumber = this.lineNumber;
        int columnNumber = this.columnNumber;
        for(int i = start; i < end; i++) {
            if(csq.charAt(i) == '\n') {
                lineNumber++;
                columnNumber = 0;
            } else {
                columnNumber++;
            }
        }
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    private void count(char c) {
        if(c == '\n') {
            lineNumber++;
            columnNumber = 0;
        } else {
            columnNumber++;
        }
    }

    int getLineNumber() {
        return lineNumber;
    }

    int getColumnNumber() {
        return columnNumber;
    }
}
