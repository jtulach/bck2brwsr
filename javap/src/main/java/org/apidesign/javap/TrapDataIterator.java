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
package org.apidesign.javap;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class TrapDataIterator {
    private final Hashtable exStart = new Hashtable();
    private final Hashtable exStop = new Hashtable();
    private TrapData[] current = new TrapData[10];
    private int currentCount;
    
    TrapDataIterator(Vector exceptionTable) {
        for (int i=0 ; i < exceptionTable.size(); i++) {
            final TrapData td = (TrapData)exceptionTable.elementAt(i);
            put(exStart, td.start_pc, td);
            put(exStop, td.end_pc, td);
        }
    }
    
    private static void put(Hashtable h, short key, TrapData td) {
        Short s = Short.valueOf((short)key);
        Vector v = (Vector) h.get(s);
        if (v == null) {
            v = new Vector(1);
            h.put(s, v);
        }
        v.add(td);
    }
    
    private boolean processAll(Hashtable h, Short key, boolean add) {
        boolean change = false;
        Vector v = (Vector)h.get(key);
        if (v != null) {
            int s = v.size();
            for (int i = 0; i < s; i++) {
                TrapData td = (TrapData)v.elementAt(i);
                if (add) {
                    add(td);
                    change = true;
                } else {
                    remove(td);
                    change = true;
                }
            }
        }
        return change;
    }
    
    public boolean advanceTo(int i) {
        Short s = Short.valueOf((short)i);
        boolean ch1 = processAll(exStart, s, true);
        boolean ch2 = processAll(exStop, s, false);
        return ch1 || ch2;
    }

    public boolean useTry() {
        return currentCount > 0;
    }

    public TrapData[] current() {
        TrapData[] copy = new TrapData[currentCount];
        for (int i = 0; i < currentCount; i++) {
            copy[i] = current[i];
        }
        return copy;
    }

    private void add(TrapData e) {
        if (currentCount == current.length) {
            TrapData[] data = new TrapData[currentCount * 2];
            for (int i = 0; i < currentCount; i++) {
                data[i] = current[i];
            }
            current = data;
        }
        current[currentCount++] = e;
    }

    private void remove(TrapData e) {
        if (currentCount == 0) {
            return;
        }
        int from = 0;
        while (from < currentCount) {
            if (e == current[from++]) {
                break;
            }
        }
        while (from < currentCount) {
            current[from - 1] = current[from];
            current[from] = null;
            from++;
        }
        currentCount--;
    }
}
