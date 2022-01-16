package org.psc.random;

import java.util.SplittableRandom;

public class Oddity {
    private static final int[] o = new SplittableRandom().ints()
            .limit(1000000)
            .parallel()
            .filter(i -> (i & 1) == 1).toArray();

    public static void main(String[] args) {
        System.out.println(o.length);
    }
}

