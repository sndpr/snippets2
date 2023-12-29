package org.psc.charsets;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.binary.Hex;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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

    @Test
    void testConversionOfUnsupportedChars() {
        var input = "aŁaŠä";
        for (int i = 0; i < input.length(); i++) {
            System.out.print((int) input.charAt(i) + "|");
        }
        System.out.println();
        System.out.println(Hex.encodeHexString(input.getBytes(StandardCharsets.UTF_8)));
        var output = new String(input.getBytes(StandardCharsets.UTF_8), Charset.forName("IBM1141"));
        System.out.println(Hex.encodeHexString(output.getBytes(Charset.forName("IBM1141"))));
        System.out.println(output);
    }
}