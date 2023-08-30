package org.psc.strings;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

public class ParameterTokenizer {

    public Map<String, String> tokenize(String parameters) {
        var parsedParameters = new HashMap<String, String>();
        String keyReference = null;
        var bracketsStack = new ArrayDeque<Pair<Integer, Character>>();
        var chars = parameters.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '(') {
                bracketsStack.addLast(Pair.of(i, '('));
                if (keyReference == null) {
                    var j = i - 1;
                    while (j > 0 && Character.isAlphabetic(chars[j])) {
                        if (Character.isAlphabetic(chars[j - 1])) {
                            j--;
                        } else {
                            break;
                        }
                    }
                    keyReference = parameters.substring(j, i);
                }
            } else if (chars[i] == ')') {
                if (bracketsStack.size() == 1) {
                    var from = bracketsStack.removeLast();
                    parsedParameters.put(keyReference, parameters.substring(from.getKey() + 1, i));
                    keyReference = null;
                } else {
                    bracketsStack.removeLast();
                }
            }
        }

        return parsedParameters;
    }

}
