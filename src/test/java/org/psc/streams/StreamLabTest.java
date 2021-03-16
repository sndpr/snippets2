package org.psc.streams;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StreamLabTest {

    @Test
    void testFilterCast(){
        Stream<Object> objects = Stream.of("test", 1);
        List<String> strings = objects
                .flatMap(it -> StreamLab.filterCast(it, String.class))
                .collect(Collectors.toList());

        assertThat(strings).containsExactly("test");

    }

}