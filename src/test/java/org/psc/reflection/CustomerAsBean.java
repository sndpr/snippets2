package org.psc.reflection;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CustomerAsBean {
    private int id;
    private String name;
    private String surname;
    private BigDecimal points;
    private LocalDate joined;
    private LocalDateTime lastAccess;
    private boolean flagged;
}
