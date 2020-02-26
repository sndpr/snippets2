package org.psc.collections;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.BidiMap;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.not;

@Slf4j
class CollectionsPlaygroundTest {

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
    void testTrie(){
        var trie = CollectionsPlayground.trie();

        assertThat(trie).isNotEmpty();
    }

    @Test
    void testCollect(){
        assertThat(CollectionsPlayground.collect()).isNotEmpty();

    }
}