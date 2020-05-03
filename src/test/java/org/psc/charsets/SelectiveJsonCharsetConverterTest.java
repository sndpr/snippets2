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
        var input = """
                {
                  "key[xxxyyy]}": "p\\"ssssŠ\\"xy\\"\\"}}}}]{{{{fff}",
                  "array": [
                    {
                      "arrKey1": "value[[[[[\\"[{{{]]]{{\\""
                    },
                    {
                      "arrKey2": "value[s[\\"[{{{{]]\\\\{\\""
                    }
                  ]
                }
                """;
        var output = SelectiveJsonCharsetConverter.convert(input);
        log.info(output);
    }
}