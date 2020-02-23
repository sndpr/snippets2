package org.psc.streams;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
class ExtendedCollectorsTest {

    @Test
    void associateBy() {
        List<String> someData = List.of("1:abc", "1123:hasjkda", "25:LSasdq", "9:ASKDL");

        Map<String, String> associatedData = someData.stream()
                .collect(ExtendedCollectors.associateBy(s -> StringUtils.substringBefore(s, ":")));

        associatedData.forEach((key, value) -> log.info("{} : {}", key, value));
        assertThat(associatedData.keySet(), org.hamcrest.Matchers.containsInAnyOrder("1", "1123", "25", "9"));
    }
}