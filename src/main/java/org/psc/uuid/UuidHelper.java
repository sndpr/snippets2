package org.psc.uuid;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UuidHelper {

    public UUID generateFromInt(int seed) {
        return UUID.nameUUIDFromBytes(String.valueOf(seed).getBytes(StandardCharsets.UTF_8));
    }

}
