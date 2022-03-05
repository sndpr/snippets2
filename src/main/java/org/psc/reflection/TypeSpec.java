package org.psc.reflection;

import org.apache.commons.lang3.StringUtils;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;

public class TypeSpec<T> {

    private static final String DEFAULT_DELIMITER = ";";
    private static final Map<Class<?>, Function<?, String>> DEFAULT_MEMBER_TYPE_MAPPINGS = Map.of(
            String.class, Function.identity(),
            BigDecimal.class, (BigDecimal bd) -> bd.toPlainString(),
            Number.class, (Number n) -> n.toString(),
            LocalDate.class, (LocalDate ld) -> ld.format(DateTimeFormatter.ISO_LOCAL_DATE),
            LocalDateTime.class, (LocalDateTime ldt) -> ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    );

    private final String delimiter;
    private final Class<T> type;
    private final Map<Class<?>, Function<?, String>> memberTypeMappings;
    private final List<MethodHandle> getters;
    private final List<MethodHandle> setters;

    public TypeSpec(Class<T> type, String delimiter) {
        this(type, delimiter, Collections.emptyMap());
    }

    public TypeSpec(Class<T> type) {
        this(type, DEFAULT_DELIMITER);
    }

    private TypeSpec(Class<T> type, String delimiter, Map<Class<?>, Function<?, String>> memberTypeMappings) {
        this.type = type;
        this.delimiter = delimiter;
        this.memberTypeMappings = new ConcurrentHashMap<>(memberTypeMappings);

        MethodHandles.Lookup lookup = MethodHandles.lookup();

        Map<String, Method> gettersAndSettersByName = Arrays.stream(type.getDeclaredMethods())
                .filter(it -> it.getName().startsWith("get") || it.getName().startsWith("set"))
                .collect(Collectors.toMap(Method::getName, Function.identity()));

        Map<String, GetterAndSetter> methodsByFieldName = Arrays.stream(type.getDeclaredFields())
                .mapMulti((Field field, Consumer<Field> consumer) -> {
                    if (gettersAndSettersByName.containsKey("get" + capitalize(field.getName()))||
                            gettersAndSettersByName.containsKey("set" + capitalize(field.getName()))) {
                        consumer.accept(field);
                    }
                })
                .collect(Collectors.toMap(Field::getName, field -> new GetterAndSetter(
                        gettersAndSettersByName.get("get" + capitalize(field.getName())),
                        gettersAndSettersByName.get("set" + capitalize(field.getName())))));

        getters = Arrays.stream(type.getDeclaredFields())
                .map(it -> methodsByFieldName.get(it.getName()))
                .filter(Objects::nonNull)
                .map(GetterAndSetter::getter)
                .filter(Objects::nonNull)
                .map(it -> unreflectMethod(lookup, it))
                .toList();

        setters = Arrays.stream(type.getDeclaredFields())
                .map(it -> methodsByFieldName.get(it.getName()))
                .filter(Objects::nonNull)
                .map(GetterAndSetter::setter)
                .filter(Objects::nonNull)
                .map(it -> unreflectMethod(lookup, it))
                .toList();

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
    private Function<MethodHandle, String> resolveValueAsString(T object) {
        return handle -> {
            try {
                Object temp = handle.invoke(object);
                if (temp == null) {
                    return "";
                }
                var mapping = memberTypeMappings
                        .entrySet()
                        .stream()
                        .filter(entry -> temp.getClass().equals(entry.getKey()))
                        .findFirst()
                        .orElseGet(() -> memberTypeMappings
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().isInstance(temp))
                                .findFirst()
                                .orElseGet(() -> Map.entry(Object.class, Objects::toString)));

                @SuppressWarnings("unchecked")
                var mapped = ((Function<Object, String>) mapping.getValue()).apply(temp);
                //                String resolved = switch (temp) {
                //                    case String s -> s;
                //                    case LocalDate localDate -> localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                //                    case BigDecimal bigDecimal -> bigDecimal.toPlainString();
                //                    case Number n -> n.toString();
                //                    default -> throw new IllegalStateException("Unexpected value: " + temp);
                //                };
                return mapped;
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        };
    }

    private MethodHandle unreflectMethod(MethodHandles.Lookup lookup, Method method) {
        try {
            return lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private MethodHandle findSetter(MethodHandles.Lookup lookup, Field field) {
        try {
            return lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class Builder<T> {

        private final Class<T> type;
        private String delimiter = DEFAULT_DELIMITER;
        private final Map<Class<?>, Function<?, String>> memberTypeMappings =
                new ConcurrentHashMap<>(DEFAULT_MEMBER_TYPE_MAPPINGS);

        Builder(Class<T> type) {
            this.type = type;
        }

        public Builder<T> delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public <U> Builder<T> mapping(Class<U> from, Function<U, String> mapper) {
            memberTypeMappings.put(from, mapper);
            return this;
        }

        public TypeSpec<T> build() {
            return new TypeSpec<>(type, delimiter, memberTypeMappings);
        }

    }

    private record GetterAndSetter(
            Method getter,
            Method setter
    ) {
    }


}
