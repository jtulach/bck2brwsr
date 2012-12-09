/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apidesign.javap;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class TrapDataIterator {
    private final java.util.Map<Short, TrapData> exStart = new java.util.HashMap<Short, TrapData>();
    private final java.util.Map<Short, TrapData> exStop = new java.util.HashMap<Short, TrapData>();
    private TrapData[] current = new TrapData[10];
    private int currentCount;
    
    TrapDataIterator(Vector exceptionTable) {
        for (int i=0 ; i < exceptionTable.size(); i++) {
            final TrapData td = (TrapData)exceptionTable.elementAt(i);
            exStart.put(td.start_pc, td);
            exStop.put(td.end_pc, td);
        }
    }

    public void advanceTo(int i) {
        TrapData e = exStart.get((short) i);
        if (e != null) {
            add(e);
        }
        e = exStop.get((short) i);
        if (e != null) {
            remove(e);
        }
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
