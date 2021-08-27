/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2021 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.bck2brwsr.emul.lang;

import org.apidesign.bck2brwsr.core.JavaScriptBody;

public final class Heartbeat {
    private Heartbeat() {
    }
    private static final Object[] lifeCycle = {null};
    static {
        initLifeCycle(lifeCycle);
    }

    public static void initialize() {
    }

    public static boolean exit(int exitCode) {
        if (lifeCycle[0] != null) {
            lifeCycleExit(lifeCycle[0], exitCode);
            return true;
        } else {
            return false;
        }

    }

    @JavaScriptBody(args = { "socket" }, body = "\n" +
"        socket[0] = null;\n" +
"        try {\n" +
"            if (location.href.startsWith('http://localhost:')) {\n" +
"                var s = new WebSocket('ws://' + location.host + '/heartbeat');\n" +
"                s.onopen = function(ev) {\n" +
"                    socket[0] = s;\n" +
"                    s.send('Application is running');\n" +
"                }\n" +
"                s.onmessage = function(ev) {\n" +
"                    console.log(ev.data);\n" +
"                    if ('reload' === ev.data) {\n" +
"                        window.location.reload();\n" +
"                    }\n" +
"                }\n" +
"            }\n" +
"        } catch (_) {\n" +
"        }\n" +
    "")
    private static void initLifeCycle(Object[] socket) {
        socket[0] = null;
    }

    @JavaScriptBody(args = {"s", "exitCode"}, body = "\n"
            + "         s.send('exit: ' + exitCode);\n"
            + "         window.close();\n"
    )
    private static void lifeCycleExit(Object lifeCycle, int exitCode) {
    }
}
