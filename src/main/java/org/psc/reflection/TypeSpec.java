package org.psc.reflection;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.capitalize;

public class TypeSpec<T> {
    private static final String DEFAULT_DELIMITER = ";";
    private static final String DEFAULT_NULL_REPLACEMENT = "";
    private static final String GET_PREFIX = "get";
    private static final String IS_PREFIX = "is";
    private static final String SET_PREFIX = "set";

    private static final Map<Class<?>, Function<?, String>> DEFAULT_TYPE_MAPPINGS = Map.of(
            String.class, Function.identity(),
            BigDecimal.class, (BigDecimal bd) -> bd.toPlainString(),
            Number.class, (Number n) -> n.toString(),
            LocalDate.class, (LocalDate ld) -> ld.format(DateTimeFormatter.ISO_LOCAL_DATE),
            LocalDateTime.class, (LocalDateTime ldt) -> ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    );
    private final String delimiter;
    private final Class<T> type;
    private final String nullReplacement;
    private final Map<String, Function<T, String>> fieldMappings;
    private final Map<Class<?>, Function<?, String>> typeMappings;
    private final List<FieldResolutionSpec<T>> getters;
    private final List<FieldNameWithMethodHandle> setters;

    public TypeSpec(Class<T> type, String delimiter) {
        this(type, delimiter, DEFAULT_NULL_REPLACEMENT, Collections.emptyMap(), Collections.emptyMap());
    }

    public TypeSpec(Class<T> type) {
        this(type, DEFAULT_DELIMITER);
    }

    private TypeSpec(Class<T> type, String delimiter, String nullReplacement,
            Map<String, Function<T, String>> fieldMappings,
            Map<Class<?>, Function<?, String>> typeMappings) {
        this.type = type;
        this.delimiter = delimiter;
        this.nullReplacement = nullReplacement;
        this.fieldMappings = new ConcurrentHashMap<>(fieldMappings);
        this.typeMappings = new ConcurrentHashMap<>(typeMappings);

        MethodHandles.Lookup lookup = MethodHandles.lookup();

        Map<String, Method> gettersAndSettersByName = Arrays.stream(type.getDeclaredMethods())
                .filter(it -> it.getName().startsWith(GET_PREFIX) || it.getName().startsWith(IS_PREFIX) ||
                        it.getName().startsWith(SET_PREFIX))
                .collect(Collectors.toMap(Method::getName, Function.identity()));

        Map<String, GetterAndSetter> methodsByFieldName = Arrays.stream(type.getDeclaredFields())
                .mapMulti((Field field, Consumer<Field> consumer) -> {
                    if (gettersAndSettersByName.containsKey(GET_PREFIX + capitalize(field.getName())) ||
                            gettersAndSettersByName.containsKey(IS_PREFIX + capitalize(field.getName())) ||
                            gettersAndSettersByName.containsKey(SET_PREFIX + capitalize(field.getName()))) {
                        consumer.accept(field);
                    }
                })
                .collect(Collectors.toMap(Field::getName,
                        field -> new GetterAndSetter(field, getGetter(gettersAndSettersByName, field),
                                gettersAndSettersByName.get(SET_PREFIX + capitalize(field.getName())))));

        getters = Arrays.stream(type.getDeclaredFields())
                .map(it -> methodsByFieldName.get(it.getName()))
                .filter(Objects::nonNull)
                .filter(it -> it.getter != null)
                .map(it -> new FieldResolutionSpec<>(it.field().getName(), unreflectMethod(lookup, it.getter()),
                        resolveToStringMapper(it)))
                .toList();

        setters = Arrays.stream(type.getDeclaredFields())
                .map(it -> methodsByFieldName.get(it.getName()))
                .filter(Objects::nonNull)
                .map(GetterAndSetter::setter)
                .filter(Objects::nonNull)
                .map(it -> new FieldNameWithMethodHandle(it.getName(), unreflectMethod(lookup, it)))
                .toList();

    }

    private Method getGetter(Map<String, Method> gettersAndSettersByName, Field field) {
        var getter = gettersAndSettersByName.get(GET_PREFIX + capitalize(field.getName()));
        if (getter == null) {
            return gettersAndSettersByName.get(IS_PREFIX + capitalize(field.getName()));
        } else {
            return getter;
        }
    }

    public static <T> TypeSpec.Builder<T> forType(Class<T> type) {
        return new TypeSpec.Builder<>(type);
    }

    public String serialize(T object) {
        return getters.stream()
                .map(resolveValueAsString(object))
                .collect(Collectors.joining(delimiter, "", ""));
    }

    @NotNull
    private Function<FieldResolutionSpec<T>, String> resolveValueAsString(T object) {
        return fieldResolutionSpec -> {
            try {
                Object fieldValue = fieldResolutionSpec.methodHandle().invoke(object);
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
    private BiFunction<T, Object, String> resolveToStringMapper(GetterAndSetter getterAndSetter) {
        Class<?> fieldType = getterAndSetter.field().getType();
        String fieldName = getterAndSetter.field().getName();

        if (fieldMappings.containsKey(fieldName)) {
            return (typeObject, fieldValue) -> fieldMappings.get(fieldName).apply(typeObject);
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
            return (typeObject, fieldValue) -> ((Function<Object, String>) typeMappings
                    .entrySet()
                    .stream()
                    .filter(entry -> fieldType.equals(entry.getKey()))
                    .findFirst()
                    .orElseGet(() -> typeMappings
                            .entrySet()
                            .stream()
                            .filter(entry -> entry.getKey().isInstance(fieldType))
                            .findFirst()
                            .orElseGet(() -> Map.entry(Object.class, Objects::toString)))
                    .getValue())
                    .apply(fieldValue);
        }
    }

    private MethodHandle unreflectMethod(MethodHandles.Lookup lookup, Method method) {
        try {
            return lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class Builder<T> {
        private final Class<T> type;
        private final SerializationBuilder<T> serializationBuilder;
        private final DeserializationBuilder<T> deserializationBuilder;

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

        public Builder<T> serialization(Consumer<SerializationBuilder<T>> consumer) {
            consumer.accept(serializationBuilder);
            return this;
        }

        public Builder<T> deserialization(Consumer<DeserializationBuilder<T>> consumer) {
            consumer.accept(deserializationBuilder);
            return this;
        }

        public TypeSpec<T> build() {
            return new TypeSpec<>(type, delimiter, serializationBuilder.nullReplacement,
                    serializationBuilder.fieldMappings, serializationBuilder.typeMappings);
        }

    }

    public static class SerializationBuilder<T> {
        private final Map<String, Function<T, String>> fieldMappings = new ConcurrentHashMap<>();
        private final Map<Class<?>, Function<?, String>> typeMappings = new ConcurrentHashMap<>(DEFAULT_TYPE_MAPPINGS);
        private String nullReplacement = DEFAULT_NULL_REPLACEMENT;

        private SerializationBuilder() {
        }

        public SerializationBuilder<T> nullReplacement(String nullReplacement) {
            this.nullReplacement = nullReplacement;
            return this;
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
        private DeserializationBuilder() {
        }

        public DeserializationBuilder<T> fieldMapping(String fieldName, Function<T, String> mapper) {
            return this;
        }

        public <U> DeserializationBuilder<T> typeMapping(Class<U> from, Function<U, String> mapper) {
            return this;
        }
    }

    private record FieldResolutionSpec<T>(
            String fieldName,
            MethodHandle methodHandle,
            BiFunction<T, Object, String> toStringMapper
    ) {}

    private record FieldNameWithMethodHandle(String fieldName, MethodHandle methodHandle) {}

    private record GetterAndSetter(Field field, Method getter, Method setter) {}

}
