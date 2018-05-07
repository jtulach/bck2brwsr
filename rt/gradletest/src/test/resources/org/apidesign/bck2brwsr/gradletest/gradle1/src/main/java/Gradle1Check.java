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

import net.java.html.js.JavaScriptBody;
import java.util.concurrent.Callable;

public class Gradle1Check implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return compute();
    }

    @JavaScriptBody(args = {}, body = "return 42;")
    private static int compute() {
        return -1;
    }

    public static void main(String... args) throws Exception {
        Gradle1Check g = new Gradle1Check();
        Integer value = g.call();
        System.err.println("Gradle1Check value: " + value);
        if (value == 42) {
            System.exit(0);
        } else {
            System.err.println("Wrong value. Expecting 42.");
            System.exit(1);
        }
    }
}
