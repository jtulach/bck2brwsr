/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2017 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
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
package org.apidesign.vm4brwsr;

/**
 * Defines obfuscation level of produced JavaScript files.
 *
 * @since 0.5
 */
public enum ObfuscationLevel {
    /** Generated JavaScript is (sort of) human readable. Useful for debugging.
     * Dynamic capabilities of the virtual machine work on all classes.
     */
    NONE,
    /** White spaces are removed. Names of external symbols remain unchanged.
     * Dynamic capabilities of the virtual machine work on all classes.
     */
    MINIMAL,
// temporarily commented out before merge. not well defined yet:
//    MEDIUM,
    /** Aggressive obfuscation of everything. Compact, unreadable "one-liner".
     * One cannot load classes dynamically. Useful mostly for static compilation
     * of self contained application.
     */
    FULL
}
