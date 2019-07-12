package org.psc.streams;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class GroupingReducer {

    public List<TypeWithList> reduce(List<JoinType> rawData) {

        rawData.get(0).get(TypeWithList.class);

        Map<TypeWithList, List<JoinType>> joinsMap =
                rawData.stream().collect(Collectors.groupingBy(e -> e.get(TypeWithList.class), Collectors.toList()));

        List<TypeWithList> finalResult = joinsMap.entrySet()
                .stream()
                .peek(kv -> kv.getKey()
                        .getInts()
                        .addAll(kv.getValue()
                                .stream()
                                .map(e -> e.get(Integer.class))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        finalResult.forEach(fr -> log.info("{} - intSize: {}", fr.getId(), fr.getInts().size()));
        finalResult.forEach(fr -> fr.getInts().forEach(i -> log.info("{} - {}", fr, i)));

        return finalResult;
    }

    @Data
    @Wither
    @NoArgsConstructor
    @AllArgsConstructor
    @SuppressWarnings("unchecked")
    static class JoinType {
        private Map<Class<?>, Object> joinedTypes;

        public <T> T get(Class<T> clazz) {
            return (T) joinedTypes.get(clazz);
        }
    }

    @Data
    @Wither
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    static class TypeWithList implements Comparator<TypeWithList> {
        @EqualsAndHashCode.Include
        private String id;
        private String data;
        private List<Integer> ints = new ArrayList<>();

        @Override
        public int compare(TypeWithList o1, TypeWithList o2) {
            return o1.getId().compareTo(o2.getId());
        }
    }

}
