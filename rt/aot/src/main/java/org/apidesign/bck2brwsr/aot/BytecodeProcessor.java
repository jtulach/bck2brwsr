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
package org.apidesign.bck2brwsr.aot;

import java.io.IOException;
import java.util.Map;
import org.apidesign.vm4brwsr.Bck2Brwsr;

/** Replace bytecode of a single class with many new bytecodes.
 *
 * @author Jaroslav Tulach
 */
interface BytecodeProcessor {
    /** Does the conversion.
     * 
     * @param className the resource of the class to replace
     * @param byteCode the bytecode of the class
     * @param resources access to other resources in the system
     * @return map of resource to bytecode which must include at least 
     *   one element of name <code>className</code>
     * @throws IOException 
     */
    public Map<String,byte[]> process(String className, byte[] byteCode, Bck2Brwsr.Resources resources)
    throws IOException;
}
