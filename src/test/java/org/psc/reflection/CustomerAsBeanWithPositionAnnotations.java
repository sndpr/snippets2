package org.psc.reflection;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CustomerAsBeanWithPositionAnnotations {

    @FieldPosition(Integer.MAX_VALUE)
    private int id;

    private String name;

    private String surname;

    private BigDecimal points;

    @FieldPosition(0)
    private LocalDate joined;

    private LocalDateTime lastAccess;

    @FieldPosition(1)
    private boolean flagged;
}
