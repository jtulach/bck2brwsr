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
package org.apidesign.bck2brwsr.launcher.b2b;

import java.awt.Desktop;
import org.apidesign.bck2brwsr.launcher.Launcher;

/** This is a launcher for the <a href="http://bck2brwsr.apidesign.org">Bck2Brwsr</a>
 * project that is using {@link Desktop} (or {@link Process}) to display the 
 * external browser in separate process. Use {@link Launcher} methods to access this
 * functionality via public, supported methods.
 *
 * @author Jaroslav Tulach
 */
public final class LauncherBck2Brwsr {
    private LauncherBck2Brwsr() {
    }
}
