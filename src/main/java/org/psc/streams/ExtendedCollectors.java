package org.psc.streams;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;

public class ExtendedCollectors {

    public static <K, T, R extends Map<K, T>> Collector<T, R, R> associateBy(Function<T, K> keyFunction) {
        return associateBy(keyFunction, (a, b) -> {
            throw new RuntimeException("key already present");
        });
    }

    public static <K, T, R extends Map<K, T>> Collector<T, R, R> associateBy(Function<T, K> keyFunction,
            BinaryOperator<T> remappingFunction) {
        return Collector.of(() -> (R) new ConcurrentHashMap<K, T>(),
                (accumulator, current) -> accumulator.put(keyFunction.apply(current), current),
                (a, b) -> {
                    b.forEach((key, value) -> a.merge(key, value, remappingFunction));
                    return a;
                }
        );
    }

    public static <V, T, R extends Map<T, V>> Collector<T, R, R> associateWith(Function<T, V> valueFunction) {
        return associateWith(valueFunction, (a, b) -> {
            throw new RuntimeException("key already present");
        });
    }

    public static <V, T, R extends Map<T, V>> Collector<T, R, R> associateWith(Function<T, V> valueFunction,
            BinaryOperator<V> remappingFunction) {
        return Collector.of(() -> (R) new ConcurrentHashMap<T, V>(),
                (accumulator, current) -> accumulator.put(current, valueFunction.apply(current)),
                (a, b) -> {
                    b.forEach((key, value) -> a.merge(key, value, remappingFunction));
                    return a;
                }
        );
    }
}
