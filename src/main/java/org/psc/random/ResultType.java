package org.psc.random;

import java.util.function.UnaryOperator;

public enum ResultType implements UnaryOperator<String> {

    SUCCESS {
        @Override
        public String apply(String s) {
            return name() + ": " + s;
        }
    },
    FAILURE;

    @Override
    public String apply(String s) {
        return "result for " + s + " is " + name();
    }

}

