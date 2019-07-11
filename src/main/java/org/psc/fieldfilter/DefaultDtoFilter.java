package org.psc.fieldfilter;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultDtoFilter implements JsonFilter<DefaultDto, Integer> {

    private static final int FILTER_VALUE = Integer.MAX_VALUE;

    public void setFilterValue(Integer filterValue) {
    }

    public DefaultDto filter(DefaultDto instance) {
        instance.setAggregates(instance.getAggregates()
                .stream()
                .map(this::filterAggregate)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        if (instance.getAggregates().isEmpty()){
            instance.setAggregates(null);
        }

        instance.setAggregate(filterAggregate(instance.getAggregate());

        if (instance.getAlphanumericId().equals(String.valueOf(FILTER_VALUE))) {
            instance.setAlphanumericId(null);
        }

        return instance;
    }

    private DefaultDto.Aggregate filterAggregate(DefaultDto.Aggregate aggregate) {
        if (aggregate.getStringValue().equals(String.valueOf(FILTER_VALUE))) {
            aggregate.setStringValue(null);
        }

        aggregate.setIntValues(
                aggregate.getIntValues().stream().filter(i -> i == FILTER_VALUE).collect(Collectors.toList()));

        if (aggregate.getIntValues().isEmpty()){
            aggregate.setIntValues(null);
        }

        aggregate.setStringValues(aggregate.getStringValues()
                .stream()
                .filter(s -> s.equals(String.valueOf(Integer.MAX_VALUE)))
                .collect(Collectors.toList()));

        if (aggregate.getStringValues().isEmpty()){
            aggregate.setStringValues(null);
        }

        if (Stream.of(aggregate.getIntValues(), aggregate.getStringValue()).allMatch(Objects::isNull)) {
            aggregate = null;
        }

        return aggregate;
    }
}
