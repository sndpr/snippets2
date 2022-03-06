package org.psc.reflection;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.StringUtils.capitalize;

public class TypeSpec<T> {
    private static final String DEFAULT_DELIMITER = ";";
    private static final String DEFAULT_NULL_REPLACEMENT = "";
    private static final String GET_PREFIX = "get";
    private static final String IS_PREFIX = "is";
    private static final String SET_PREFIX = "set";

    private static final Map<Class<?>, Function<?, String>> DEFAULT_SERIALIZATION_TYPE_MAPPINGS = Map.of(
            String.class, Function.identity(),
            BigDecimal.class, (BigDecimal bd) -> bd.toPlainString(),
            Number.class, (Number n) -> n.toString(),
            LocalDate.class, (LocalDate ld) -> ld.format(DateTimeFormatter.ISO_LOCAL_DATE),
            LocalDateTime.class, (LocalDateTime ldt) -> ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    );

    private static final Map<Class<?>, Function<String, ?>> DEFAULT_DESERIALIZATION_TYPE_MAPPINGS =
            createDefaultDeserializationTypeMappings();

    private final String delimiter;
    private final String nullReplacement;
    private final Map<String, Function<T, String>> serializationFieldMappings;
    private final Map<Class<?>, Function<?, String>> serializationTypeMappings;
    private final Map<String, Function<String, ?>> deserializationFieldMappings;
    private final Map<Class<?>, Function<String, ?>> deserializationTypeMappings;
    private final List<FieldResolutionSpec<T>> fieldResolutionSpecs;
    private final Constructor<T> resolvedAllArgsConstructor;
    private final Constructor<T> noArgsConstructor;
    private final Map<Integer, Integer> deserializationPositionLookup;

    public TypeSpec(Class<T> type, String delimiter) {
        this(
                type,
                delimiter,
                DEFAULT_NULL_REPLACEMENT,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap());
    }

    public TypeSpec(Class<T> type) {
        this(type, DEFAULT_DELIMITER);
    }

    private TypeSpec(
            Class<T> type,
            String delimiter,
            String nullReplacement,
            Map<String, Integer> fieldPositions,
            Map<String, Function<T, String>> serializationFieldMappings,
            Map<Class<?>, Function<?, String>> serializationTypeMappings,
            Map<String, Function<String, ?>> deserializationFieldMappings,
            Map<Class<?>, Function<String, ?>> deserializationTypeMappings) {
        this.delimiter = delimiter;
        this.nullReplacement = nullReplacement;

        this.serializationFieldMappings = new ConcurrentHashMap<>(serializationFieldMappings);
        this.serializationTypeMappings = new ConcurrentHashMap<>(serializationTypeMappings);

        this.deserializationFieldMappings = new ConcurrentHashMap<>(deserializationFieldMappings);
        this.deserializationTypeMappings = new ConcurrentHashMap<>(deserializationTypeMappings);

        MethodHandles.Lookup lookup = MethodHandles.lookup();

        fieldResolutionSpecs = resolveFieldResolutionSpecs(type, fieldPositions, lookup);

        deserializationPositionLookup = resolveDeserializationPositionLookup();

        // no setters, so we have to try deserialization by using an AllArgsConstructor
        if (fieldResolutionSpecs.stream()
                .map(FieldResolutionSpec::setterMethodHandle)
                .filter(Objects::nonNull)
                .toList()
                .isEmpty()) {
            // again, only declaredFields for now
            try {
                noArgsConstructor = null;
                resolvedAllArgsConstructor = type.getDeclaredConstructor(Arrays.stream(type.getDeclaredFields())
                        .map(Field::getType)
                        .toArray(Class[]::new));
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        } else {
            try {
                noArgsConstructor = type.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
            resolvedAllArgsConstructor = null;
        }

    }

    public static <T> TypeSpec.Builder<T> forType(Class<T> type) {
        return new TypeSpec.Builder<>(type);
    }

    private static Map<Class<?>, Function<String, ?>> createDefaultDeserializationTypeMappings() {
        var map = new ConcurrentHashMap<Class<?>, Function<String, ?>>();

        map.put(String.class, Function.identity());
        map.put(Boolean.class, Boolean::valueOf);
        map.put(boolean.class, Boolean::valueOf);
        map.put(BigDecimal.class, BigDecimal::new);
        map.put(Integer.class, Integer::valueOf);
        map.put(int.class, Integer::valueOf);
        map.put(Long.class, Long::valueOf);
        map.put(long.class, Long::valueOf);
        map.put(Double.class, Double::valueOf);
        map.put(double.class, Double::valueOf);
        map.put(LocalDate.class, s -> LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE));
        map.put(LocalDateTime.class, s -> LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        return map;
    }

    public String serialize(T object) {
        return fieldResolutionSpecs.stream()
                .sorted(Comparator.comparing(FieldResolutionSpec::position))
                .map(resolveValueAsString(object))
                .collect(Collectors.joining(delimiter, "", ""));
    }

    public T deserialize(String value) {
        String[] split = value.split(delimiter);
        String[] sortedSplit = new String[split.length];

        IntStream.range(0, split.length)
                .forEach(i -> {
                    int positionForIndex = deserializationPositionLookup.get(i);
                    sortedSplit[i] = split[positionForIndex];
                });

        // TODO: how should deserialization of custom null values work?
        if (resolvedAllArgsConstructor != null) {
            return deserializeWithAllArgsConstructor(sortedSplit);
        } else {
            return deserializeWithSetters(sortedSplit);
        }
    }

    @NotNull
    private T deserializeWithSetters(String[] sortedSplit) {
        T instance;
        try {
            instance = noArgsConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }

        IntStream.range(0, sortedSplit.length).forEach(i -> {
            FieldResolutionSpec<T> currentFieldResolutionSpec = fieldResolutionSpecs.get(i);
            String valueToDeserialize = sortedSplit[i];
            Object deserializedValue = currentFieldResolutionSpec.fromStringMapper().apply(valueToDeserialize);
            try {
                currentFieldResolutionSpec.setterMethodHandle().invoke(instance, deserializedValue);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        });

        return instance;
    }

    private T deserializeWithAllArgsConstructor(String[] sortedSplit) {
        Object[] mappedConstructorArgs = IntStream.range(0, sortedSplit.length).mapToObj(i -> {
            FieldResolutionSpec<T> currentFieldResolutionSpec = fieldResolutionSpecs.get(i);
            String valueToDeserialize = sortedSplit[i];
            return currentFieldResolutionSpec.fromStringMapper().apply(valueToDeserialize);
        }).toArray();

        try {
            return resolvedAllArgsConstructor.newInstance(mappedConstructorArgs);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<FieldResolutionSpec<T>> resolveFieldResolutionSpecs(
            Class<T> type,
            Map<String, Integer> fieldPositions,
            MethodHandles.Lookup lookup) {
        // for now, we're using the declaredFields + declaredMethods only, which means, that inherited components
        // won't be included
        Map<String, Method> gettersAndSettersByName = Arrays.stream(type.getDeclaredMethods())
                .filter(it -> it.getName().startsWith(GET_PREFIX) || it.getName().startsWith(IS_PREFIX) ||
                        it.getName().startsWith(SET_PREFIX))
                .collect(Collectors.toMap(Method::getName, Function.identity()));

        Map<String, GetterAndSetter> methodsByFieldName =
                Arrays.stream(type.getDeclaredFields())
                        .mapMulti((Field field, Consumer<Field> consumer) -> {
                            if (gettersAndSettersByName.containsKey(GET_PREFIX + capitalize(field.getName())) ||
                                    gettersAndSettersByName.containsKey(IS_PREFIX + capitalize(field.getName())) ||
                                    gettersAndSettersByName.containsKey(SET_PREFIX + capitalize(field.getName()))) {
                                consumer.accept(field);
                            }
                        })
                        .collect(Collectors.toMap(
                                Field::getName,
                                field -> new GetterAndSetter(
                                        field,
                                        getGetter(gettersAndSettersByName, field),
                                        gettersAndSettersByName.get(SET_PREFIX + capitalize(field.getName())))));

        var nativePosition = new AtomicInteger(0);
        var positionResolver = new PositionResolver<>(type, fieldPositions);

        return Arrays.stream(type.getDeclaredFields())
                .map(it -> methodsByFieldName.get(it.getName()))
                .filter(Objects::nonNull)
                .filter(it -> it.getter != null)
                .map(it -> new FieldResolutionSpec<>(
                        nativePosition.getAndIncrement(),
                        positionResolver.get(it.field()),
                        it.field().getName(),
                        it.field().getType(),
                        unreflectMethod(lookup, it.getter()),
                        unreflectMethod(lookup, it.setter()),
                        resolveToStringMapper(it),
                        resolveFromStringMapper(it.field())))
                .toList();
    }

    private Map<Integer, Integer> resolveDeserializationPositionLookup() {
        List<Integer> weightedPositions = fieldResolutionSpecs
                .stream()
                .mapToInt(FieldResolutionSpec::position)
                .sorted()
                .boxed()
                .toList();

        return IntStream.range(0, fieldResolutionSpecs.size())
                .boxed()
                .collect(Collectors.toMap(
                        Function.identity(),
                        i -> {
                            int positionForIndex = fieldResolutionSpecs.stream().filter(it -> it.nativePosition() == i)
                                    .findFirst()
                                    .map(FieldResolutionSpec::position)
                                    .orElseThrow();
                            return weightedPositions.indexOf(positionForIndex);
                        })
                );
    }

    private Method getGetter(Map<String, Method> gettersAndSettersByName, Field field) {
        var getter = gettersAndSettersByName.get(GET_PREFIX + capitalize(field.getName()));
        if (getter == null) {
            return gettersAndSettersByName.get(IS_PREFIX + capitalize(field.getName()));
        } else {
            return getter;
        }
    }

    @NotNull
    private Function<FieldResolutionSpec<T>, String> resolveValueAsString(T object) {
        return fieldResolutionSpec -> {
            try {
                Object fieldValue = fieldResolutionSpec.getterMethodHandle().invoke(object);
                if (fieldValue == null) {
                    return nullReplacement;
                }

                var mapped = fieldResolutionSpec.toStringMapper().apply(object, fieldValue);
                //                String resolved = switch (fieldValue) {
                //                    case String s -> s;
                //                    case LocalDate localDate -> localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                //                    case BigDecimal bigDecimal -> bigDecimal.toPlainString();
                //                    case Number n -> n.toString();
                //                    default -> throw new IllegalStateException("Unexpected value: " + fieldValue);
                //                };
                return mapped;
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        };
    }

    @NotNull
    private Function<String, Object> resolveFromStringMapper(Field field) {
        Optional<? extends Function<String, ?>> fieldDeserializer = deserializationFieldMappings.entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(field.getName()))
                .map(Map.Entry::getValue)
                .findFirst();

        if (fieldDeserializer.isEmpty()) {
            Class<?> fieldType = field.getType();
            //noinspection unchecked
            return (Function<String, Object>) deserializationTypeMappings.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().equals(fieldType))
                    .findFirst()
                    .orElseGet(() -> deserializationTypeMappings.entrySet()
                            .stream()
                            .filter(entry -> entry.getKey().isAssignableFrom(fieldType))
                            .findFirst()
                            .orElseThrow())
                    .getValue();
        } else {
            //noinspection unchecked
            return (Function<String, Object>) fieldDeserializer.get();
        }
    }

    @NotNull
    private BiFunction<T, Object, String> resolveToStringMapper(GetterAndSetter getterAndSetter) {
        Class<?> fieldType = getterAndSetter.field().getType();
        String fieldName = getterAndSetter.field().getName();

        if (serializationFieldMappings.containsKey(fieldName)) {
            return (typeObject, fieldValue) -> serializationFieldMappings.get(fieldName).apply(typeObject);
        } else {
            // if the Map.Entry.getValue is unwrapped within the stream, the second typeMappings resolution can no
            // longer be mapped to a Function<?, String> even though the return type is the same as the required type,
            // I'm too dumb to understand that:
            // Bad return type in lambda expression: Function<capture of ?, String> cannot be converted to
            // Function<capture of ?, String>
            //            return typeMappings
            //                    .entrySet()
            //                    .stream()
            //                    .filter(entry -> fieldType.equals(entry.getKey()))
            //                    .findFirst()
            //                    .map(Map.Entry::getValue)
            //                    .orElseGet(() -> typeMappings
            //                            .entrySet()
            //                            .stream()
            //                            .filter(entry -> entry.getKey().isInstance(fieldType))
            //                            .findFirst()
            //                            .map(Map.Entry::getValue)
            //                            .orElseGet(() -> Objects::toString));
            //noinspection unchecked
            return (typeObject, fieldValue) -> ((Function<Object, String>) serializationTypeMappings.entrySet()
                    .stream()
                    .filter(entry -> fieldType.equals(entry.getKey()))
                    .findFirst()
                    .orElseGet(() -> serializationTypeMappings.entrySet()
                            .stream()
                            .filter(entry -> entry.getKey().isInstance(fieldType))
                            .findFirst()
                            .orElseGet(() -> Map.entry(Object.class, Objects::toString)))
                    .getValue()).apply(fieldValue);
        }
    }

    private MethodHandle unreflectMethod(MethodHandles.Lookup lookup, Method method) {
        if (method == null) {
            return null;
        }
        try {
            return lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class Builder<T> {
        private final Class<T> type;
        private final Map<String, Integer> fieldPositions = new ConcurrentHashMap<>();
        private final SerializationBuilder<T> serializationBuilder;
        private final DeserializationBuilder<T> deserializationBuilder;
        private String nullReplacement = DEFAULT_NULL_REPLACEMENT;

        private String delimiter = DEFAULT_DELIMITER;

        Builder(Class<T> type) {
            this.type = type;
            serializationBuilder = new SerializationBuilder<>();
            deserializationBuilder = new DeserializationBuilder<>();
        }

        public Builder<T> delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder<T> nullReplacement(String nullReplacement) {
            this.nullReplacement = nullReplacement;
            return this;
        }

        public Builder<T> position(String fieldName, int position) {
            fieldPositions.put(fieldName, position);
            return this;
        }

        public Builder<T> serialization(Consumer<SerializationBuilder<T>> consumer) {
            consumer.accept(serializationBuilder);
            return this;
        }

        public Builder<T> deserialization(Consumer<DeserializationBuilder<T>> consumer) {
            consumer.accept(deserializationBuilder);
            return this;
        }

        public TypeSpec<T> build() {
            return new TypeSpec<>(
                    type,
                    delimiter,
                    nullReplacement,
                    fieldPositions,
                    serializationBuilder.fieldMappings,
                    serializationBuilder.typeMappings,
                    deserializationBuilder.fieldMappings,
                    deserializationBuilder.typeMappings);
        }

    }

    public static class SerializationBuilder<T> {
        private final Map<String, Function<T, String>> fieldMappings = new ConcurrentHashMap<>();
        private final Map<Class<?>, Function<?, String>> typeMappings =
                new ConcurrentHashMap<>(DEFAULT_SERIALIZATION_TYPE_MAPPINGS);

        private SerializationBuilder() {
        }

        public SerializationBuilder<T> fieldMapping(String fieldName, Function<T, String> mapper) {
            fieldMappings.put(fieldName, mapper);
            return this;
        }

        public <U> SerializationBuilder<T> typeMapping(Class<U> from, Function<U, String> mapper) {
            typeMappings.put(from, mapper);
            return this;
        }
    }

    public static class DeserializationBuilder<T> {
        private final Map<String, Function<String, ?>> fieldMappings = new ConcurrentHashMap<>();
        private final Map<Class<?>, Function<String, ?>> typeMappings =
                new ConcurrentHashMap<>(DEFAULT_DESERIALIZATION_TYPE_MAPPINGS);

        private DeserializationBuilder() {
        }

        public <U> DeserializationBuilder<T> fieldMapping(String fieldName, Function<String, U> mapper) {
            fieldMappings.put(fieldName, mapper);
            return this;
        }

        public <U> DeserializationBuilder<T> typeMapping(Class<U> from, Function<String, U> mapper) {
            typeMappings.put(from, mapper);
            return this;
        }
    }

    private record FieldResolutionSpec<T>(
            int nativePosition,
            int position,
            String fieldName,
            Class<?> fieldType,
            MethodHandle getterMethodHandle,
            MethodHandle setterMethodHandle,
            BiFunction<T, Object, String> toStringMapper,
            Function<String, Object> fromStringMapper
    ) {}

    private record GetterAndSetter(Field field, Method getter, Method setter) {}

    private static class PositionResolver<T> {
        private final Map<String, Integer> resolvedFieldPositions = new ConcurrentHashMap<>();

        private PositionResolver(Class<T> type, Map<String, Integer> customFieldPositions) {
            AtomicInteger fallBackPosition = new AtomicInteger(Integer.MAX_VALUE / 2);
            Map<String, Integer> positionsFromAnnotations = Arrays.stream(type.getDeclaredFields())
                    .collect(Collectors.toMap(Field::getName, it -> {
                        FieldPosition fieldPosition = it.getAnnotation(FieldPosition.class);
                        return fieldPosition == null ? fallBackPosition.getAndIncrement() : fieldPosition.value();
                    }));

            // programmatically set positions take precedence over annotations (if a field has positions set through
            // both variants)
            resolvedFieldPositions.putAll(positionsFromAnnotations);
            resolvedFieldPositions.putAll(customFieldPositions);
        }

        public int get(Field field) {
            return resolvedFieldPositions.get(field.getName());
        }

    }
}
