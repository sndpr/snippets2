package org.psc.retry;

import java.util.function.Function;
import java.util.function.Supplier;

public class Retry {

    public static <I, O> Function<I, O> retry(Function<I, O> action) {
        return input -> {
            try {
                return action.apply(input);
            } catch (Exception e) {
                return retry(action).apply(input);
            }
        };
    }

    public static <T> Supplier<T> retry(Supplier<T> action) {
        return () -> {
            try {
                return action.get();
            } catch (Exception e) {
                return retry(action).get();
            }
        };
    }

}
