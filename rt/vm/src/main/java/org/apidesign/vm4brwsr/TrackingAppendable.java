/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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

final class TrackingAppendable implements Appendable {
    private final Appendable out;
    private final Runnable onChange;

    TrackingAppendable(Appendable out, Runnable onChange) {
        this.out = out;
        this.onChange = onChange;
    }

    @Override
    public final Appendable append(CharSequence csq) throws IOException {
        out.append(csq);
        onChange.run();
        return this;
    }

    @Override
    public final Appendable append(CharSequence csq, int start, int end) throws IOException {
        out.append(csq, start, end);
        onChange.run();
        return this;
    }

    @Override
    public final Appendable append(char c) throws IOException {
        out.append(c);
        onChange.run();
        return this;
    }

}
