package org.psc.fieldfilter;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DefaultDto {

    private Long id;
    private Integer[] integers;
    private String alphanumericId;
    private NestedAggregate nestedAggregate;
    private Aggregate aggregate;
    private List<Aggregate> aggregates;
    private List<String> strings;
    private Map<Integer, PrimitiveIntWrapper> primitiveIntWrapperMap;


    @Data
    public static class NestedAggregate {
        private StringWrapper nestedNested;
        private List<Aggregate> aggregateList;
        private Map<String, AggregateWithMap> aggregateWithMapMap;
        private Integer intVal;
        private int primitiveIntValue;
    }

    @Data
    public static class Aggregate {
        private String stringValue;
        private List<Integer> intValues;
        private List<String> stringValues;
    }

    @Data
    public static class AggregateWithMap {
        private String stringValue;
        private Map<String, Integer> valueMap;
    }

    @Data
    public static class StringWrapper {
        private String stringValue;
    }

    @Data
    public static class PrimitiveIntWrapper {
        private int primitiveIntValue;
    }
}
