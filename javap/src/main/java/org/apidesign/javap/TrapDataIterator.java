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
    private final java.util.Deque<TrapData> current = new java.util.ArrayDeque<TrapData>();
    
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
            current.addFirst(e);
        }
        e = exStop.get((short) i);
        if (e != null) {
            current.remove(e);
        }
    }

    public boolean useTry() {
        return !current.isEmpty();
    }

    public TrapData[] current() {
        return current.toArray(new TrapData[0]);
    }
}
