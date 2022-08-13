package org.psc.varargs.overloads;

import java.util.function.UnaryOperator;

public class VarargsOverloads {

    public static class StringModifier implements UnaryOperator<String> {
        @Override
        public String apply(String s) {
            return s;
        }
    }

    public static String callMe(String input, UnaryOperator<String>... modifiers) {
        var result = input;
        for (UnaryOperator<String> modifier : modifiers) {
            result = modifier.apply(result);
        }
        return result;
    }

    public static String callMe(String input, StringModifier... modifiers) {
        var result = input;
        for (UnaryOperator<String> modifier : modifiers) {
            result = modifier.apply(result);
        }
        return "modified_" + result;
    }

}
