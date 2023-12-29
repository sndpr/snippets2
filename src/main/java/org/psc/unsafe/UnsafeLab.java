package org.psc.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeLab {

    public static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        return (Unsafe) theUnsafe.get(null);
    }


}
