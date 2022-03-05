package org.psc.reflection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CustomerAsRecord(
        int id,
        String name,
        String surname,
        BigDecimal points,
        LocalDate joined,
        LocalDateTime lastAccess
) {
}
