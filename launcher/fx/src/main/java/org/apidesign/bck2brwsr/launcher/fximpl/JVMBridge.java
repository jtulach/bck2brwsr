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

import java.util.TooManyListenersException;
import javafx.beans.value.ChangeListener;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class JVMBridge {
    private static ClassLoader[] ldrs;
    private static ChangeListener<Void> onBck2BrwsrLoad;
        
    public static void registerClassLoaders(ClassLoader[] loaders) {
        ldrs = loaders.clone();
    }
    
    public static void addBck2BrwsrLoad(ChangeListener<Void> l) throws TooManyListenersException {
        if (onBck2BrwsrLoad != null) {
            throw new TooManyListenersException();
        }
        onBck2BrwsrLoad = l;
    }

    public static void onBck2BrwsrLoad() {
        ChangeListener<Void> l = onBck2BrwsrLoad;
        if (l != null) {
            l.changed(null, null, null);
        }
    }
    
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        System.err.println("trying to load " + name);
        ClassNotFoundException ex = null;
        if (ldrs != null) for (ClassLoader l : ldrs) {
            try {
                return Class.forName(name, true, l);
            } catch (ClassNotFoundException ex2) {
                ex = ex2;
            }
        }
        if (ex == null) {
            ex = new ClassNotFoundException("No loaders");
        }
        throw ex;
    }
}
