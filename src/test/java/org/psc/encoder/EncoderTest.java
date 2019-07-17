package org.psc.encoder;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
class EncoderTest {

    @Test
    void testEncode() {
        String encodedPassword = Encoder.encode("password");
        log.info(encodedPassword);
        assertThat(encodedPassword, startsWith("$2a$16$"));

    }
}
