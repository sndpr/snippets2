package org.psc.encoder;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
class EncoderTest {

    @Test
    void testEncode() {
        String encodedPassword = Encoder.encode("password");
        log.info(encodedPassword);
        assertThat(encodedPassword, is("$2a$16$Gbr92RV8JMCjWvmyS6wDWu6V4NuO2TkDHJ3CW8GXVHkL5Lp4XZ9OK"));

    }
}
