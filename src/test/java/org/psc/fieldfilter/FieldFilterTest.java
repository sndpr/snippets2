package org.psc.fieldfilter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FieldFilterTest {

    @Test
    void testIt() {
        var defaultDto = new DefaultDto();
        defaultDto.setIntegers(new Integer[]{0, -4, Integer.MAX_VALUE, Integer.MIN_VALUE});
        defaultDto.setStrings(List.of("adsnadasd", String.valueOf(Integer.MAX_VALUE), "454as"));
        defaultDto.setAlphanumericId(String.valueOf(Integer.MAX_VALUE));
        FieldFilter.filter(defaultDto);

        assertThat(true, is(true));
    }
}
