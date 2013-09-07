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
package java.lang;

/** Poor man's re-implementation of most important System methods.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class System {
    private System() {
    }
    
    public static void arraycopy(Object value, int srcBegin, Object dst, int dstBegin, int count) {
        org.apidesign.bck2brwsr.emul.lang.System.arraycopy(value, srcBegin, dst, dstBegin, count);
    }
    
    public static long currentTimeMillis() {
        return org.apidesign.bck2brwsr.emul.lang.System.currentTimeMillis();
    }
    
    public static int identityHashCode(Object obj) {
        return obj.defaultHashCode();
    }

    public static String getProperty(String name) {
        return null;
    }
    
    public static String getProperty(String key, String def) {
        return def;
    }
    
    /**
     * Returns the system-dependent line separator string.  It always
     * returns the same value - the initial value of the {@linkplain
     * #getProperty(String) system property} {@code line.separator}.
     *
     * <p>On UNIX systems, it returns {@code "\n"}; on Microsoft
     * Windows systems it returns {@code "\r\n"}.
     */
    public static String lineSeparator() {
        return "\n";
    }
}
