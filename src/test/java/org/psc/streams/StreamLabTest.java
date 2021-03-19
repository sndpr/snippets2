package org.psc.streams;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StreamLabTest {

    @Test
    void testMapMultiFilterCast() {
        Stream<Object> objects = Stream.of("test", "!", 1);
        List<String> filterCastStrings = objects
                .mapMulti(StreamLab.filterCast(String.class))
                .toList();

        assertThat(filterCastStrings).containsExactly("test", "!");
    }

    @Test
    void testMapMultiFilterCast2() {
        Stream<Object> objects = Stream.of("test", "!", 1);

        List<String> instanceOfPatternMatched = objects
                .mapMulti((obj, downstream) -> {
                    if (obj instanceof String s) {
                        downstream.accept(s);
                    }
                })
                .map(String.class::cast)
                .toList();

        assertThat(instanceOfPatternMatched).containsExactly("test", "!");
    }

    @Test
    void testFilterCast() {
        Stream<Object> objects = Stream.of("test", 1);
        List<String> strings = objects
                .flatMap(it -> StreamLab.filterCast(it, String.class))
                .toList();

        assertThat(strings).containsExactly("test");

    }

}