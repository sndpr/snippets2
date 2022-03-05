package org.psc.reflection;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SuppressWarnings("ClassCanBeRecord")
@Value
public class CustomerAsBean {
    int id;
    String name;
    String surname;
    BigDecimal points;
    LocalDate joined;
    LocalDateTime lastAccess;
    boolean flagged;
}
