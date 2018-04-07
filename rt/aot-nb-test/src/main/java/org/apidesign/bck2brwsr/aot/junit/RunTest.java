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
package org.apidesign.bck2brwsr.aot.junit;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import junit.framework.AssertionFailedError;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.textui.ResultPrinter;

public class RunTest extends ResultPrinter {
    public static void main(String... args) throws UnsupportedEncodingException {
        System.err.println(run());
    }

    public static String run() throws UnsupportedEncodingException {
        TestResult tr = new TestResult();
        class L implements TestListener {
            StringBuilder sb = new StringBuilder();

            @Override
            public void addError(Test test, Throwable e) {
                sb.append(test.toString()).append("\n");
            }

            @Override
            public void addFailure(Test test, AssertionFailedError e) {
                sb.append(test.toString()).append("\n");;
            }

            @Override
            public void endTest(Test test) {
                sb.append(test.toString()).append("\n");;
            }

            @Override
            public void startTest(Test test) {
                sb.append(test.toString()).append("\n");;
            }
        }
        L listener = new L();
        tr.addListener(listener);
        listener.sb.append("Starting the test run\n");
        JUnit4TestAdapter suite = new JUnit4TestAdapter(TestedTest.class);
        suite.run(tr);
        listener.sb.append("End of test run\n");
        return listener.sb.toString();
    }

    RunTest(PrintStream writer) {
        super(writer);
    }
}
