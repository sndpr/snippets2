package org.psc.jupiter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.annotation.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExtensionTest {

    @Test
    @DisabledOnWeekday({DayOfWeek.FRIDAY, DayOfWeek.MONDAY})
    void testConditionally() {
        assertThat(1).isEqualTo(1);
    }

    @Test
    void testAlways() {
        assertThat(1).isEqualTo(1);
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @ExtendWith(DisabledOnWeekdayCondition.class)
    @interface DisabledOnWeekday {

        DayOfWeek[] value();

    }

    static class DisabledOnWeekdayCondition implements ExecutionCondition {

        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            DayOfWeek today = LocalDate.now().getDayOfWeek();
            return AnnotationSupport.findAnnotation(context.getElement(), DisabledOnWeekday.class)
                    .map(DisabledOnWeekday::value)
                    .map(List::of)
                    .filter(daysOfWeek -> daysOfWeek.contains(today))
                    .map(daysOfWeek -> ConditionEvaluationResult.disabled("Today is " + today + "."))
                    .orElse(ConditionEvaluationResult.enabled(
                            "Today is not one of the days on which the test should be disabled"));
        }
    }

}
