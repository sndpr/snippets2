package org.psc.charsets;

import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class SelectiveJsonCharsetConverterTest {

    @Test
    void convert() {
        @Language("JSON")
        var input = "{\n" +
                "  \"key[xxxyyy]}\": \"p\\\"ssssŠ\\\"xy\\\"\\\"}}}}]{{{{fff}\",\n" +
                "  " +
                "\"array\": [\n" +
                "    {\n" +
                "      \"arrKey1\": \"value[[[[[\\\"[{{{{{\\\"\"\n" +
                "    },\n" +
                "    " +
                "{\n" +
                "      \"arrKey2\": \"value[s[\\\"[{{{{{\\\"\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        var output = SelectiveJsonCharsetConverter.convert(input);
        log.info(output);
    }
}