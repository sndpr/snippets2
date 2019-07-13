package org.psc.streams;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
public class GroupingReducer {

    public List<TypeWithList> reduce(@NotNull List<JoinType> rawData) {
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

    public List<TypeWithList> singleStreamReduce(@NotNull List<JoinType> rawData) {

        Collection<TypeWithList> combinedData = rawData.stream()
                .collect(Collectors.groupingBy(joinType -> joinType.get(TypeWithList.class),
                        Collector.of(TypeWithList::new, (typeWithList, joinType) -> {
                            typeWithList.setId(joinType.get(TypeWithList.class).getId());
                            typeWithList.setData(joinType.get(TypeWithList.class).getData());
                            typeWithList.getInts().add(joinType.get(Integer.class));
                        }, (a, b) -> a, Function.identity())))
                .values();

        return new ArrayList<>(combinedData);
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
        public int compare(@NotNull TypeWithList o1, @NotNull TypeWithList o2) {
            return o1.getId().compareTo(o2.getId());
        }
    }

}
