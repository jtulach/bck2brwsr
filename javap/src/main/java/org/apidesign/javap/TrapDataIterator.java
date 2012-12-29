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
            exStart.put(td.start_pc, td);
            exStop.put(td.end_pc, td);
        }
    }

    public boolean advanceTo(int i) {
        boolean change = false;
        Short s = Short.valueOf((short)i);
        TrapData e = (TrapData) exStart.get(s);
        if (e != null) {
            add(e);
            change = true;
        }
        e = (TrapData) exStop.get(s);
        if (e != null) {
            remove(e);
            change = true;
        }
        return change;
    }

    public boolean useTry() {
        return currentCount > 0;
    }

    public TrapData[] current() {
        return current;
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
