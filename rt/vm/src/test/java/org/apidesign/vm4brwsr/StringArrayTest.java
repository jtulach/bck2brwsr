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

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class StringArrayTest {
    @Test public void deleteMinusIndex() throws Exception {
        String[] arr = { "Ahoj", "Kluci" };
        StringArray list = StringArray.asList(arr);
        list.delete(-1);
        assertEquals(list.toArray().length, 2, "No element removed");
    }
    @Test public void deleteTooHighIndex() throws Exception {
        String[] arr = { "Ahoj", "Kluci" };
        StringArray list = StringArray.asList(arr);
        list.delete(5);
        assertEquals(list.toArray().length, 2, "No element removed");
    }
    @Test public void deleteFirst() throws Exception {
        String[] arr = { "Ahoj", "Kluci" };
        StringArray list = StringArray.asList(arr);
        list.delete(0);
        assertEquals(list.toArray().length, 1, "First element removed");
        assertEquals(list.toArray()[0], "Kluci");
    }
    @Test public void deleteSecond() throws Exception {
        String[] arr = { "Ahoj", "Kluci" };
        StringArray list = StringArray.asList(arr);
        list.delete(1);
        assertEquals(list.toArray().length, 1, "Second element removed");
        assertEquals(list.toArray()[0], "Ahoj");
    }
}
