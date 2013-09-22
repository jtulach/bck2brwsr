package org.apidesign.bck2brwsr.tck;

import java.util.EnumMap;
import java.util.EnumSet;
import org.apidesign.bck2brwsr.vmtest.Compare;
import org.apidesign.bck2brwsr.vmtest.VMTest;
import org.testng.annotations.Factory;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
public class EnumsTest {
    enum Color {
        B, W;
    }

    /*
    @Compare public String enumSet() {
        try { throw new Exception(); } catch (Exception ex) {}
        EnumSet<Color> c = EnumSet.allOf(Color.class);
        return c.toString();
    }

    @Compare public String enumSetOneByOne() {
        EnumSet<Color> c = EnumSet.of(Color.B, Color.W);
        return c.toString();
    }
    */

    @Compare public boolean enumFirstContains() {
        EnumSet<Color> c = EnumSet.of(Color.B);
        return c.contains(Color.B);
    }

    @Compare public boolean enumFirstDoesNotContains() {
        EnumSet<Color> c = EnumSet.of(Color.B);
        return c.contains(Color.W);
    }

    @Compare public boolean enumSndContains() {
        EnumSet<Color> c = EnumSet.of(Color.W);
        return c.contains(Color.W);
    }

    @Compare public boolean enumSecondDoesNotContains() {
        EnumSet<Color> c = EnumSet.of(Color.W);
        return c.contains(Color.B);
    }

    @Compare public String enumMap() {
        EnumMap<Color,String> c = new EnumMap(Color.class);
        c.put(Color.B, "Black");
        c.put(Color.W, "White");
        return c.toString();
    }
    
    @Factory public static Object[] create() {
        return VMTest.create(EnumsTest.class);
    }
}
