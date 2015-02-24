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
package org.apidesign.vm4brwsr;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class Under_Score {
    public static int under_field = 10;
    public int instance_field = 5;
    
    public static int one() {
        return 1;
    }
    
    public static int one_plus_one() {
        return 1 + 1;
    }
    
    public static int two() {
        return one_plus_one();
    }
    
    public static int staticField() {
        return under_field;
    }
    
    public static int instance() {
        return new Under_Score().get_fld();
    }
    
    private int get_fld() {
        return instance_field;
    }
}
