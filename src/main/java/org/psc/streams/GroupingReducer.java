package org.psc.streams;

import lombok.*;
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
                        Collector.of(TypeWithList::new,
                                (typeWithList, joinType) -> {
                                    typeWithList.setId(joinType.get(TypeWithList.class).getId());
                                    typeWithList.setData(joinType.get(TypeWithList.class).getData());
                                    typeWithList.getInts().add(joinType.get(Integer.class));
                                }, (a, b) -> {
                                    a.ints.addAll(b.ints);
                                    return a;
                                }, Function.identity())))
                .values();

        return new ArrayList<>(combinedData);
    }

    public List<TypeWithList> singleStreamReduceWithSingletonWrapper(@NotNull List<JoinType> rawData) {

        @SuppressWarnings("Convert2MethodRef")
        Collection<TypeWithList> combinedData = rawData.stream()
                .collect(Collectors.groupingBy(joinType -> joinType.get(TypeWithList.class),
                        Collector.of(() -> new Singleton<TypeWithList>(),
                                (accumulated, joinType) -> {
                                    TypeWithList currentTypeWithList = joinType.get(TypeWithList.class);
                                    TypeWithList accumulatedTypeWithList =
                                            accumulated.getOrSetIfEmpty(joinType.get(TypeWithList.class));
                                    if (!currentTypeWithList.equals(accumulatedTypeWithList)) {
                                        currentTypeWithList.getInts().addAll(currentTypeWithList.getInts());
                                    }
                                }, (a, b) -> {
                                    a.get().getInts().addAll(b.get().getInts());
                                    return a;
                                }, Singleton::get)))
                .values();

        return new ArrayList<>(combinedData);
    }

    @Data
    @With
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
    @With
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

    static class Singleton<E> implements Collection<E> {

        private E element = null;

        public static <E> Singleton<E> of(E element) {
            Singleton<E> singleton = new Singleton<>();
            singleton.element = element;
            return singleton;
        }

        public E get() {
            return element;
        }

        public <T extends E> E getOrSetIfEmpty(T element) {
            if (this.element == null) {
                this.element = element;
            }
            return this.element;
        }

        @Override
        public int size() {
            return isEmpty() ? 0 : 1;
        }

        @Override
        public boolean isEmpty() {
            return element == null;
        }

        @Override
        public boolean contains(Object o) {
            return o.equals(element);
        }

        @NotNull
        @Override
        public Iterator<E> iterator() {
            return new Iterator<>() {

                private E current = element;

                @Override
                public boolean hasNext() {
                    return current != null;
                }

                @Override
                public E next() {
                    E next = current;
                    current = null;
                    return next;
                }
            };
        }

        @NotNull
        @Override
        public Object[] toArray() {
            return new Object[]{element};
        }

        @NotNull
        @Override
        public <T> T[] toArray(@NotNull T[] a) {
            if (a.length > 0) {
                //noinspection unchecked
                a[1] = (T) element;
                return a;
            } else {
                //noinspection unchecked
                return (T[]) new Object[]{element};
            }
        }

        @Override
        public boolean add(E e) {
            element = e;
            return true;
        }

        @Override
        public boolean remove(Object o) {
            if (o.equals(element)) {
                element = null;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return c.stream().allMatch(collectionElement -> collectionElement.equals(element));
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends E> c) {
            if (c.size() == 1) {
                element = c.iterator().next();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            boolean removed = c.stream().anyMatch(collectionElement -> collectionElement.equals(element));
            if (removed) {
                element = null;
            }
            return removed;
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            boolean retain = c.stream().anyMatch(collectionElement -> collectionElement.equals(element));
            if (!retain) {
                element = null;
            }
            return retain;
        }

        @Override
        public void clear() {
            element = null;
        }
    }

}
