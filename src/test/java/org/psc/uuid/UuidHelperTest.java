package org.psc.uuid;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Slf4j
class UuidHelperTest {

    @Test
    void testGenerateFromInt() {
        UuidHelper uuidHelper = new UuidHelper();

        String uuid1 = uuidHelper.generateFromInt(1234).toString();
        String uuid2 = uuidHelper.generateFromInt(1234).toString();

        log.info("uuid1 = {}", uuid1);
        log.info("uuid2 = {}", uuid2);

        assertThat(uuid1, is(uuid2));

        String uuid3 = uuidHelper.generateFromInt(999456).toString();
        String uuid4 = uuidHelper.generateFromInt(999456).toString();

        log.info("uuid3 = {}", uuid3);
        log.info("uuid4 = {}", uuid4);

        assertThat(uuid3, is(uuid4));
    }
}