package org.psc.consec;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Slf4j
class ConsecutiveSequencesCalulatorTest {

    @Test
    void testCalcConsecutiveSequences() {
        List<List<Integer>> consecutiveSequences = ConsecutiveSequencesCalculator.calcConsecutiveSequences(5);

        consecutiveSequences.stream()
                .map(e -> e.stream().map(String::valueOf).collect(Collectors.joining(", ")))
                .forEach(log::info);

        assertThat(consecutiveSequences.size(), is(1));

        consecutiveSequences = ConsecutiveSequencesCalculator.calcConsecutiveSequences(100);

        consecutiveSequences.stream()
                .map(e -> e.stream().map(String::valueOf).collect(Collectors.joining(", ")))
                .forEach(log::info);

        assertThat(consecutiveSequences.size(), is(2));
    }

}
