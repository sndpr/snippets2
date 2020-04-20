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
import static org.hamcrest.Matchers.is;

public class StreamDebuggingTest {

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
