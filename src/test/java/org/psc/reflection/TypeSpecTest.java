package org.psc.reflection;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TypeSpecTest {

    @Test
    void shouldSerializeWithDefaults() {
        TypeSpec<CustomerAsValueType> customerAsBeanTypeSpec = TypeSpec.forType(CustomerAsValueType.class)
                .build();

        var customer = new CustomerAsValueType(1, "Abc", "Def", new BigDecimal("15.93"), LocalDate.of(2010, 4, 17),
                LocalDateTime.of(2022, 3, 4, 14, 56), false);

        String serialized = customerAsBeanTypeSpec.serialize(customer);

        assertThat(serialized).isEqualTo("1;Abc;Def;15.93;2010-04-17;2022-03-04T14:56:00;false");
    }

    @Test
    void shouldDeserializeWithDefaults() {
        TypeSpec<CustomerAsValueType> customerAsBeanTypeSpec = TypeSpec.forType(CustomerAsValueType.class)
                .build();

        var customer = new CustomerAsValueType(1, "Abc", "Def", new BigDecimal("15.93"), LocalDate.of(2010, 4, 17),
                LocalDateTime.of(2022, 3, 4, 14, 56), false);

        String serialized = customerAsBeanTypeSpec.serialize(customer);

        var deserialized = customerAsBeanTypeSpec.deserialize(serialized);
        assertThat(deserialized).isEqualTo(customer);
    }

    @Test
    void shouldSerializeNullValueWithDefaults() {
        TypeSpec<CustomerAsValueType> customerAsBeanTypeSpec = TypeSpec.forType(CustomerAsValueType.class)
                .build();

        var customer = new CustomerAsValueType(1, null, "Def", new BigDecimal("15.93"), LocalDate.of(2010, 4, 17),
                LocalDateTime.of(2022, 3, 4, 14, 56), false);

        String serialized = customerAsBeanTypeSpec.serialize(customer);

        assertThat(serialized).isEqualTo("1;;Def;15.93;2010-04-17;2022-03-04T14:56:00;false");
    }

    @Test
    void shouldSerializeNullWithCustomFallback() {
        TypeSpec<CustomerAsValueType> customerAsBeanTypeSpec = TypeSpec.forType(CustomerAsValueType.class)
                .nullReplacement("NULL")
                .build();

        var customer = new CustomerAsValueType(1, null, "Def", new BigDecimal("15.93"), LocalDate.of(2010, 4, 17),
                LocalDateTime.of(2022, 3, 4, 14, 56), false);

        String serialized = customerAsBeanTypeSpec.serialize(customer);

        assertThat(serialized).isEqualTo("1;NULL;Def;15.93;2010-04-17;2022-03-04T14:56:00;false");
    }

    @Test
    void shouldSerializeWithCustomFieldMapping() {
        TypeSpec<CustomerAsValueType> customerAsBeanTypeSpec = TypeSpec.forType(CustomerAsValueType.class)
                .serialization(builder -> builder
                        .fieldMapping("id", customer -> StringUtils.leftPad(String.valueOf(customer.getId()), 5, '0')))
                .build();

        var customer = new CustomerAsValueType(1, "Abc", "Def", new BigDecimal("15.93"), LocalDate.of(2010, 4, 17),
                LocalDateTime.of(2022, 3, 4, 14, 56), false);

        String serialized = customerAsBeanTypeSpec.serialize(customer);

        assertThat(serialized).isEqualTo("00001;Abc;Def;15.93;2010-04-17;2022-03-04T14:56:00;false");
    }

    @Test
    void shouldSerializeWithCustomTypeMapping() {
        TypeSpec<CustomerAsValueType> customerAsBeanTypeSpec = TypeSpec.forType(CustomerAsValueType.class)
                .serialization(builder -> builder.typeMapping(int.class, i -> String.valueOf(100 * i)))
                .build();

        var customer = new CustomerAsValueType(1, "Abc", "Def", new BigDecimal("15.93"), LocalDate.of(2010, 4, 17),
                LocalDateTime.of(2022, 3, 4, 14, 56), false);

        String serialized = customerAsBeanTypeSpec.serialize(customer);

        assertThat(serialized).isEqualTo("100;Abc;Def;15.93;2010-04-17;2022-03-04T14:56:00;false");
    }

    @Test
    void shouldSerializeWithCustomFieldMappingDueToHigherPriority() {
        TypeSpec<CustomerAsValueType> customerAsBeanTypeSpec = TypeSpec.forType(CustomerAsValueType.class)
                .serialization(builder -> builder
                        .typeMapping(int.class, i -> String.valueOf(100 * i))
                        .fieldMapping("id", customer -> StringUtils.leftPad(String.valueOf(customer.getId()), 5, '0')))
                .build();

        var customer = new CustomerAsValueType(1, "Abc", "Def", new BigDecimal("15.93"), LocalDate.of(2010, 4, 17),
                LocalDateTime.of(2022, 3, 4, 14, 56), false);

        String serialized = customerAsBeanTypeSpec.serialize(customer);

        assertThat(serialized).isEqualTo("00001;Abc;Def;15.93;2010-04-17;2022-03-04T14:56:00;false");
    }

}
