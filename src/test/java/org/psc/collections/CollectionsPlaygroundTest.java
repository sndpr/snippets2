package org.psc.collections;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class CollectionsPlaygroundTest {

    @Test
    void disjoint() {
        CollectionsPlayground.disjoint();
    }

    @Test
    void testOrderedMap() {
        var orderedMap = CollectionsPlayground.orderedMap();
        orderedMap.forEach((key, value) -> log.info("{}: {}", key, value));

        assertThat(orderedMap).isNotEmpty();
    }

    @Test
    void testBidiMapFromString() {
        var bidiMapInput = """
                a:1
                B:2222
                d:-454
                x:213123
                C:3424
                H:77
                """;

        BidiMap<String, Integer> bidiMap = CollectionsPlayground.bidiMapFromString(bidiMapInput);

        assertThat(bidiMap.get("C")).isEqualTo(3424);
        assertThat(bidiMap.getKey(77)).isEqualTo("H");
    }

    @Test
    void testBidiMapFromStringDuplicateValue() {
        var bidiMapInput = """
                a:1
                B:2222
                d:-454
                x:213123
                C:3424
                H:77
                F:1
                """;

        BidiMap<String, Integer> bidiMap = CollectionsPlayground.bidiMapFromString(bidiMapInput);

        // a gets override by F
        assertThat(bidiMap.get("a")).isNull();
        assertThat(bidiMap.getKey(1)).isEqualTo("F");
    }

    @Test
    void testTrie() {
        var trie = CollectionsPlayground.trie();
        assertThat(trie).isNotEmpty();
    }

    @Test
    void testCollect() {
        assertThat(CollectionsPlayground.collect()).isNotEmpty();
    }

    @Test
    void testDisjunction() {
        assertThat(CollectionsPlayground.disjunction()).contains("A", "B", "D", "E");
    }

    @Test
    void testCollate() {
        List<Integer> first = List.of(2, 4, 7, 9);
        List<Integer> second = List.of(1, 3, 5, 6, 7, 8, 9, 10);
        Collection<Integer> collated = CollectionUtils.collate(first, second);
        collated.forEach(i -> log.info("{}", i));
        assertThat(collated.iterator().next()).isEqualTo(1);
        assertThat(collated.size()).isEqualTo(first.size() + second.size());
    }

}