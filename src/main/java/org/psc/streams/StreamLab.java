package org.psc.streams;

import java.util.stream.Stream;

public class StreamLab {

    public static void main(String[] args) {
        // wait for java 16
        Stream<Object> objects = Stream.of("test", "!", 1);
    }

    public static <I, O> Stream<O> filterCast(I inputElement, Class<O> outputType) {
        return outputType.isInstance(inputElement) ? Stream.of(outputType.cast(inputElement)) : Stream.empty();
    }

}
