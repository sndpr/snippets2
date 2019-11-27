package org.psc.charsets;

import org.springframework.security.crypto.codec.Hex;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.stream.Collectors;

public class SelectiveJsonCharsetConverter {

    private static final byte OPENING_CURLY_CP870 = Hex.decode("c0")[0];
    private static final byte CLOSING_CURLY_CP870 = Hex.decode("d0")[0];
    private static final byte OPENING_SQUARE_CP870 = Hex.decode("4a")[0];
    private static final byte CLOSING_SQUARE_CP870 = Hex.decode("5a")[0];

    private static final byte OPENING_CURLY_CP1141 = Hex.decode("43")[0];
    private static final byte CLOSING_CURLY_CP1141 = Hex.decode("dc")[0];
    private static final byte OPENING_SQUARE_CP1141 = Hex.decode("63")[0];
    private static final byte CLOSING_SQUARE_CP1141 = Hex.decode("fc")[0];

    private static final byte QUOTATION_MARK = Hex.decode("7f")[0];
    private static final byte BACKSLASH_CP870 = Hex.decode("e0")[0];

    private static final Map<Byte, Byte> CP870_TO_CP1141_OMISSIONS;
    private static final Map<Byte, Byte> CP1141_TO_CP870_OMISSIONS;

    static {
        CP870_TO_CP1141_OMISSIONS =
                Map.of(OPENING_CURLY_CP870, OPENING_CURLY_CP1141,
                        CLOSING_CURLY_CP870, CLOSING_CURLY_CP1141,
                        OPENING_SQUARE_CP870, OPENING_SQUARE_CP1141,
                        CLOSING_SQUARE_CP870, CLOSING_SQUARE_CP1141);

        CP1141_TO_CP870_OMISSIONS = CP870_TO_CP1141_OMISSIONS.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public static String convert(String input) {
        byte[] intermediate = input.getBytes(Charset.forName("IBM870"));

        boolean isWithinConvertibleRegion = false;
        for (int i = 0; i < intermediate.length; i++) {

            if (i > 0 && intermediate[i] == QUOTATION_MARK && intermediate[i - 1] != BACKSLASH_CP870 &&
                    !isWithinConvertibleRegion) {
                isWithinConvertibleRegion = true;
            } else if (i > 0 && intermediate[i] == QUOTATION_MARK && intermediate[i - 1] != BACKSLASH_CP870 &&
                    isWithinConvertibleRegion){
                isWithinConvertibleRegion = false;
            }

            if (!isWithinConvertibleRegion) {
                intermediate[i] = CP870_TO_CP1141_OMISSIONS.getOrDefault(intermediate[i], intermediate[i]);
            }
        }

        return new String(intermediate, Charset.forName("IBM01141"));
    }
}
