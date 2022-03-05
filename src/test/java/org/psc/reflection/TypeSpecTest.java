package org.psc.reflection;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TypeSpecTest {

    @Test
    void shouldCreateATypeSpec() {
        TypeSpec<CustomerAsBean> customerAsBeanTypeSpec = TypeSpec.forType(CustomerAsBean.class)
                .build();

        var customer = new CustomerAsBean(1, "Abc", "Def", new BigDecimal("15.93"), LocalDate.of(2010, 4, 17),
                LocalDateTime.of(2022,3,4,14,56));

        String serialized = customerAsBeanTypeSpec.serialize(customer);

        assertThat(serialized).isEqualTo("1;Abc;Def;15.93;2010-04-17;2022-03-04T14:56:00");
    }

}
