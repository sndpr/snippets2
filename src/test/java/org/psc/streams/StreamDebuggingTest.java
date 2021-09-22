package org.psc.streams;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class StreamDebuggingTest {

    @Test
    void testDebugStream() {
        List<String> dataPoints1 = List.of("first", "second", "third", "end");
        var firstEntity = new TestEntity()
                .withDataPoints(dataPoints1)
                .withInfo("firstEntity");

        List<String> dataPoints2 = List.of("second", "end", "unique");
        var secondEntity = new TestEntity()
                .withDataPoints(dataPoints2)
                .withInfo("secondEntity");

        String collected = Stream.of(firstEntity, secondEntity)
                .flatMap(te -> te.getDataPoints().stream())
                .filter(s -> s.length() >= 5)
                .map(s -> StringUtils.substring(s, 0, 1))
                .collect(Collectors.joining());

        assertThat(collected, is("fstsu"));
    }

    @Test
    void testMapMulti() {
        List<Character> chars = Stream.of("abc", "def", "ghi", "zzz", "111")
                .mapMulti((s, consumer) -> s.chars()
                        .mapToObj(i -> (char) i)
                        .forEach(consumer))
                .map(o -> (Character) o)
                .toList();

        assertThat(chars, contains('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'z', 'z', 'z', '1', '1', '1'));
    }

    @Test
    void testTeeing() {
        List<String> dataPoints = List.of("first", "second", "third", "end", "aaa", "bbb", "defg");
        // this makes no sense, but just to demonstrate what teeing does
        List<String> collected = dataPoints.stream()
                .map(String::toUpperCase)
                .collect(Collectors.teeing(Collectors.groupingBy(String::length), Collectors.toList(),
                        (map, list) -> {
                            list.addAll(map.entrySet()
                                    .stream()
                                    .map(it -> it.getKey() + ":" + it.getValue())
                                    .collect(Collectors.toList()));
                            return list;
                        }));
        assertThat(collected,
                containsInAnyOrder("FIRST", "SECOND", "THIRD", "END", "AAA", "BBB", "DEFG", "3:[END, AAA, BBB]",
                        "4:[DEFG]", "5:[FIRST, THIRD]", "6:[SECOND]"));
    }

    @Data
    @With
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TestEntity {
        private List<String> dataPoints;
        private String info;
        private long id;
    }
}
