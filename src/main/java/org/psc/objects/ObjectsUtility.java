package org.psc.objects;

import java.util.Objects;
import java.util.Optional;

public class ObjectsUtility {

    public static void main(String[] args) {
        var s = Objects.requireNonNullElseGet(null, ObjectsUtility::getGreeting);
        System.out.println(s);

        // instead of:
        Optional.ofNullable(null)
                .orElseGet(ObjectsUtility::getGreeting);
    }

    /**
     * {@return Returns just {@code Hello!} }
     * <p>
     * That's it.
     * </p>
     * <p>
     * However, IntelliJ doesn't render inline {@code {@code}} values within an {@code {@return ...}} inline block
     * correctly in reader mode.
     * </p>
     */
    private static String getGreeting() {
        return "Hello!";
    }

}
