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

    private static String getGreeting() {
        return "Hello!";
    }

}
