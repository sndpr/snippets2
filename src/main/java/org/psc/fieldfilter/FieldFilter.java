package org.psc.fieldfilter;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Slf4j
public class FieldFilter {

    private static Map<Class, Field[]> FIELD_MAP = new ConcurrentHashMap<>();

    public static <T> T filter(T object) {

        Field[] fields = FIELD_MAP.computeIfAbsent(object.getClass(), Class::getDeclaredFields);

        Stream.of(fields).forEach(field -> {
            field.setAccessible(true);

            Object fieldValue = Try.of(() -> field.get(object)).getOrElse(() -> null);

            if (fieldValue != null) {
                if (field.getType().equals(String.class)) {
                    if (((String) fieldValue).equalsIgnoreCase(String.valueOf(Integer.MAX_VALUE))) {
                        Try.run(() -> field.set(object, null)).getOrElseThrow(ex -> new RuntimeException(ex));
                    }
                } else if (field.getType().isArray()) {
                    //...

                } else if (List.class.isAssignableFrom(field.getType())) {
                    List<?> listValues = (List<?>) Try.of(() -> field.get(object)).getOrElse(() -> null);
                    listValues.forEach(FieldFilter::filter);
                }

                Field[] subFields = fieldValue.getClass().getDeclaredFields();
                Stream.of(subFields).forEach(e -> log.info(e.getName()));
            }

            field.setAccessible(false);
        });

        return null;
    }
}
