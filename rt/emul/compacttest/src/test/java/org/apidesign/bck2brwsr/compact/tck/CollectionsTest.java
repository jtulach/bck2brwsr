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
package org.apidesign.bck2brwsr.compact.tck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class CollectionsTest {
    @Compare public String sortStringsInArray() {
        List<String> list = new ArrayList<>();
        list.add("one");
        list.add("two");
        list.add("three");
        list.add("four");
        list.add("five");
        list.add("six");
        list.add("seven");
        list.add("eight");
        list.add("nine");
        list.add("ten");
        
        String[] arr = list.toArray(new String[list.size()]);
        Arrays.sort(arr);
        
        return Arrays.asList(arr).toString();
    }
    
    @Compare public String sortStringsInHashSet() {
        Collection<String> list = new HashSet<>();
        list.add("one");
        list.add("two");
        list.add("three");
        list.add("four");
        list.add("five");
        list.add("six");
        list.add("seven");
        list.add("eight");
        list.add("nine");
        list.add("ten");
        
        String[] arr = list.toArray(new String[0]);
        Arrays.sort(arr);
        
        return Arrays.asList(arr).toString();
    }

    @SuppressWarnings("unchecked")
    @Compare public String sortStringsInHashMapWithComparator() {
        class C implements Comparator<Map.Entry<String,Integer>> {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        }
        
        Map<String,Integer> map = new HashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        map.put("four", 4);
        map.put("five", 5);
        map.put("six", 6);
        map.put("seven", 7);
        map.put("eight", 8);
        map.put("nine", 9);
        map.put("ten", 10);
        
        List<Entry<String,Integer>> arr = new Vector<>();
        arr.addAll(map.entrySet());
        Collections.sort(arr, new C());
        return arr.toString();
    }
    
    @Compare public String jirkaToArray() {
        List<String> appfiles = new ArrayList<>();
        appfiles.add("onefile");
        appfiles.add("2ndfile");
        appfiles.add("3rdfile");
        String[] ret = appfiles.toArray(new String[appfiles.size()]);
        return 
            ret.getClass().getName() + ":" +
            ret.length + ":" +
            Arrays.toString(ret);
    }
    
    @Factory
    public static Object[] create() {
        return VMTest.create(CollectionsTest.class);
    }
    
}
