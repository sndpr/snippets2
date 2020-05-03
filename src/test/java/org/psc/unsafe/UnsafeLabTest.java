package org.psc.unsafe;

import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import static org.junit.jupiter.api.Assertions.*;

class UnsafeLabTest {

    @Test
    void testGetUnsafe() throws NoSuchFieldException, IllegalAccessException {
        Unsafe unsafe = UnsafeLab.getUnsafe();
        assertNotNull(unsafe);
    }

}