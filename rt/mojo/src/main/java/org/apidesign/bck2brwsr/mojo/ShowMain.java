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

package org.apidesign.bck2brwsr.mojo;

import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.apidesign.bck2brwsr.launcher.Launcher;

final class ShowMain {
    private ShowMain() {
    }

    public static void main(String[] args) throws Exception {
        execute(new ConsoleHandler(), new File(args[0]));
        System.exit(0);
    }

    static void execute(Handler handler, File web) throws SecurityException, IOException {
        final java.util.logging.Logger javaLogger = java.util.logging.Logger.getLogger("org.apidesign.bck2brwsr.launcher.BaseHTTPLauncher");
        javaLogger.setLevel(Level.INFO);
        final java.util.logging.Logger serverLogger = java.util.logging.Logger.getLogger("org.glassfish.grizzly.http.server");
        serverLogger.setLevel(Level.INFO);
        javaLogger.addHandler(handler);
        serverLogger.addHandler(handler);

        try (Closeable httpServer = Launcher.showDir("bck2brwsr", web, null, "index.html")) {
            keepOneHandler(javaLogger, handler);
            keepOneHandler(serverLogger, handler);

            final CountDownLatch finished = new CountDownLatch(1);
            if (httpServer instanceof Flushable) {
                Runnable flushing = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ((Flushable) httpServer).flush();
                        } catch (IOException ex) {
                            final LogRecord rec = new LogRecord(Level.WARNING, ex.getMessage());
                            rec.setThrown(ex);
                            handler.publish(rec);
                        } finally {
                            finished.countDown();
                        }
                    }
                };
                Thread thread = new Thread(flushing, "Flush StdIO");
                thread.setDaemon(true);
                thread.start();
            }
            while (finished.getCount() > 0) {
                handler.flush();
                try {
                    finished.await(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    final LogRecord rec = new LogRecord(Level.WARNING, ex.getMessage());
                    rec.setThrown(ex);
                    handler.publish(rec);
                }
            }
        }
        handler.publish(new LogRecord(Level.INFO, "Finished"));
    }

    private static void keepOneHandler(final java.util.logging.Logger logger, final Handler handler) {
        for (Handler h : logger.getHandlers()) {
            if (h != handler) {
                logger.removeHandler(h);
            }
        }
    }
}
