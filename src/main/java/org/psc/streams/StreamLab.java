package org.psc.streams;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class StreamLab {

    public static void main(String[] args) {
        Stream<Object> objects = Stream.of("test", "!", 1);
        List<String> filterCastStrings = objects
                .mapMulti(filterCast(String.class))
                .toList();

        List<String> filterCastStrings1 = objects
                .flatMap(it -> filterCast(it, String.class))
                .toList();
}

    public static <I, O> Stream<O> filterCast(I inputElement, Class<O> outputType) {
        return outputType.isInstance(inputElement) ? Stream.of(outputType.cast(inputElement)) : Stream.empty();
    }

    public static <I, O> BiConsumer<I, Consumer<O>> filterCast(Class<O> outputType) {
        return (i, c) -> {
            if (outputType.isInstance(i)) {
                c.accept(outputType.cast(i));
            }
        };
    }

}
