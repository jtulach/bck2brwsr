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
package org.apidesign.bck2brwsr.launcher;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class MethodInvocation {
    final CountDownLatch wait = new CountDownLatch(1);
    final String className;
    final String methodName;
    final String html;
    private String result;
    private Throwable exception;

    MethodInvocation(String className, String methodName, String html) {
        this.className = className;
        this.methodName = methodName;
        this.html = html;
    }
    
    void await(long timeOut) throws InterruptedException {
        wait.await(timeOut, TimeUnit.MILLISECONDS);
    }
    
    void result(String r, Throwable e) {
        this.result = r;
        this.exception = e;
        wait.countDown();
    }

    @Override
    public String toString() {
        if (exception != null) {
            return exception.toString();
        }
        return result;
    }
    
}
