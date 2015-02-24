/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2015 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.htmlpage.api;

import java.io.Closeable;
import org.apidesign.bck2brwsr.core.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class Timer implements Closeable {
    private final Object t;
    
    private Timer(Object t) {
        this.t = t;
    }
    
    /** Creates a timer that invokes provided runnable on a fixed interval
     * 
     * @param r the runnable to execute
     * @param time milliseconds to invoke the timer periodically
     */
    public static Timer create(Runnable r, int time) {
        return new Timer(interval(r, time));
    }
    
    @JavaScriptBody(args = { "r", "time" }, body = 
        "return window.setInterval(function() { r.run__V(); }, time);"
    )
    private static native Object interval(Runnable r, int time);

    @JavaScriptBody(args = { "self" }, body = 
        "window.clearInterval(self);"
    )
    private static native void close(Object self);
    
    /** Cancels this timer.
     */
    @Override
    public void close() {
        close(t);
    }
}
