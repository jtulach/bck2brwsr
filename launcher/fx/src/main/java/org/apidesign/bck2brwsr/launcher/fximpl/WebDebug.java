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
package org.apidesign.bck2brwsr.launcher.fximpl;

import com.sun.javafx.scene.web.Debugger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.util.Callback;

/** Simulates WebKit protocol over WebSockets.
 *
 * @author Jaroslav Tulach
 */
abstract class WebDebug extends OutputStream 
implements Callback<String, Void>, Runnable {
    private static final Logger LOG = Logger.getLogger(WebDebug.class.getName());

    private final Debugger debug;
    private final StringBuilder cmd = new StringBuilder();
    private final ToDbgInputStream toDbg;
    private final String ud;
    
    WebDebug(Debugger debug, String ud) throws Exception {
        this.debug = debug;
        this.ud = ud;
        toDbg = new ToDbgInputStream();
        debug.setEnabled(true);
        debug.setMessageCallback(this);
    }
    
    static WebDebug create(Debugger debug, String ud) throws Exception {
        WebDebug web = new WebDebug(debug, ud) {};
        
        Executors.newFixedThreadPool(1).execute(web);
        
        return web;
    }

    @Override
    public void run() {
        try {
            String p = System.getProperty("startpage.file");
            File f;
            if (p != null && (f = new File(p)).exists()) {
                String[] args = {"--livehtml", f.getAbsolutePath()};
                File dir = f.getParentFile();
                cliHandler(args, toDbg, this, System.err, dir);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void close() {
        try {
            toDbg.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Void call(String p) {
        assert p.indexOf('\n') == -1 : "No new line: " + p;
        LOG.log(Level.INFO, "toDbgr: {0}", p);
        toDbg.pushMsg(p);
        return null;
    }

    @Override
    public void write(int b) throws IOException {
        if (b == '\n') {
            final String msg = cmd.toString();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    LOG.log(Level.INFO, "toView: {0}", msg);
                    debug.sendMessage(msg);
                }
            });
            cmd.setLength(0);
        } else {
            if (cmd.length() > 100000) {
                LOG.log(Level.WARNING, "Too big:\n{0}", cmd);
            }
            cmd.append((char)b);
        }
    }   

    private void cliHandler(
        String[] args, InputStream is, OutputStream os, OutputStream err, File dir
    ) {
        try {
            Class<?> main = Class.forName("org.netbeans.MainImpl");
            Method m = main.getDeclaredMethod("execute", String[].class, InputStream.class, 
                OutputStream.class, OutputStream.class, AtomicReference.class
            );
            m.setAccessible(true);
            System.setProperty("netbeans.user", ud);
            int ret = (Integer)m.invoke(null, args, is, os, err, null);
            LOG.log(Level.INFO, "Return value: {0}", ret);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            LOG.info("Communication is over");
        }
        
    }

    private class ToDbgInputStream extends InputStream {
        private byte[] current;
        private int currentPos;
        private final ArrayBlockingQueue<byte[]> pending = new ArrayBlockingQueue<byte[]>(64);

        public ToDbgInputStream() {
        }

        @Override
        public int read() throws IOException {
            return read(null, 0, 1);
        }
        
        @Override
        public int read(byte[] arr, int offset, int len) throws IOException {
            if (current == null || current.length <= currentPos) {
                for (;;) {
                    WebDebug.this.flush();
                    try {
                        current = pending.poll(5, TimeUnit.MILLISECONDS);
                        if (current == null) {
                            return 0;
                        }
                        break;
                    } catch (InterruptedException ex) {
                        throw (InterruptedIOException)new InterruptedIOException().initCause(ex);
                    }
                }
                LOG.info("Will return: " + new String(current));
                currentPos = 0;
            }
            int cnt = 0;
            while (len-- > 0 && currentPos < current.length) {
                final byte nextByte = current[currentPos++];
                if (arr == null) {
                    return nextByte;
                }
                arr[offset + cnt++] = nextByte;
            }
            LOG.log(Level.INFO, "read returns: {0}", new String(arr, offset, cnt));
            return cnt;
        }

        private void pushMsg(String p) {
            try {
                pending.offer((p + '\n').getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
}
