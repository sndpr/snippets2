package org.psc.sql;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class SelectionCriteriaTest {

    @Test
    void testBuildVariant2Nested() {
        String selectionCriteria = new SelectionCriteria.ProtoBuilder()
                .is("ABC", "asdasd", s -> StringUtils.wrap(s, "'"))
                .or(it -> it
                        .is("XYZ", "asdasd", s -> StringUtils.wrap(s, "'"))
                        .is("AAA", "asdasd", s -> StringUtils.wrap(s, "'")))
                .build();

        log.info(selectionCriteria);
        assertThat(selectionCriteria).isEqualTo("AND ABC = 'asdasd' OR (XYZ = 'asdasd' AND AAA = 'asdasd')");
    }

    @Test
    void testBuildVariant2NestedWithRedundantAnd() {
        String selectionCriteria = new SelectionCriteria.ProtoBuilder()
                .is("ABC", "asdasd", s -> StringUtils.wrap(s, "'"))
                .or(it -> it
                        .is("XYZ", "asdasd", s -> StringUtils.wrap(s, "'"))
                        .and()
                        .is("AAA", "asdasd", s -> StringUtils.wrap(s, "'")))
                .build();

        log.info(selectionCriteria);
        assertThat(selectionCriteria).isEqualTo("AND ABC = 'asdasd' OR (XYZ = 'asdasd' AND AAA = 'asdasd')");
    }

    @Test
    void testBuildVariant2WithInlineOr() {
        String selectionCriteria = new SelectionCriteria.ProtoBuilder()
                .is("ABC", "asdasd", s -> StringUtils.wrap(s, "'"))
                .or()
                .is("XYZ", "asdasd", s -> StringUtils.wrap(s, "'"))
                .build();

        log.info(selectionCriteria);
        assertThat(selectionCriteria).isEqualTo("AND ABC = 'asdasd' OR XYZ = 'asdasd'");
    }
}