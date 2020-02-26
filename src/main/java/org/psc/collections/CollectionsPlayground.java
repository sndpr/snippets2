package org.psc.collections;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.bidimap.TreeBidiMap;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.apache.commons.lang3.StringUtils;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Stream;

@Slf4j
public class CollectionsPlayground {

    public static OrderedMap<String, String> orderedMap() {
        OrderedMap<String, String> orderedMap = new LinkedMap<>();

        orderedMap.put("first", "abcac");
        orderedMap.put("second", "56451");
        orderedMap.put("third", "nothing");
        orderedMap.put("fourth", "f");
        orderedMap.put("fifth", "pld");

        String secondValue = orderedMap.get(orderedMap.previousKey("third"));

        log.info(secondValue);

        return orderedMap;
    }

    public static BidiMap<String, Integer> bidiMapFromString(String input) {
        return Stream.of(StringUtils.split(input, '\n'))
                .map(CollectionsPlayground::fromSimpleMapping)
                .collect(Collector.of(TreeBidiMap::new,
                        (accumulator, current) -> accumulator.put(current.getKey(), current.getValue()),
                        (a, b) -> {
                            a.putAll(b);
                            return a;
                        }));
    }

    public static Trie<String, String> trie() {
        Trie<String, String> trie = new PatriciaTrie<>();
        trie.put("A", "1");
        trie.put("ABC", "12");
        trie.put("AAAA", "4412");
        trie.put("ABABAB", "test");
        trie.put("BYBE", "lok");
        trie.put("AABC", "asdas");

        Map<String, String> prefixMap = trie.prefixMap("AB");
        prefixMap.forEach((key, value) -> log.info("{}: {}", key, value));
        log.info("----");

        Map<String, String> prefixMap2 = trie.prefixMap("AA");
        prefixMap2.forEach((key, value) -> log.info("{}: {}", key, value));

        return trie;
    }

    public static Collection<Double> collect() {
        List<Integer> input = List.of(13, 7, 28, 94, 1, 9, 44, 8);
        Collection<Double> output = CollectionUtils.collect(input, i -> i / 3d);
        output.forEach(d -> log.info("{}", d));
        return output;
    }

    private static Map.Entry<String, Integer> fromSimpleMapping(String input) {
        String[] splitInput = StringUtils.split(input, ':');
        return new AbstractMap.SimpleEntry<>(splitInput[0], Integer.valueOf(splitInput[1]));
    }

}
