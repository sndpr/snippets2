package org.psc.consec;

import java.util.*;

public class ConsecutiveSequencesCalculator {

    public static List<List<Integer>> calcConsecutiveSequences(int sum) {
        int upperBoundary = (int) Math.ceil(sum / 2.0);
        int currentSum = 0;
        int currentElem = 1;

        List<List<Integer>> results = new ArrayList<>();
        Deque<Integer> currentSequence = new LinkedList<>();

        while (currentElem <= upperBoundary) {
            if (currentSum < sum) {
                currentSum += currentElem;
                currentSequence.addLast(currentElem);
                currentElem++;
            }
            if (currentSum > sum) {
                currentSum -= currentSequence.removeFirst();
            }
            if (currentSum == sum) {
                results.add(new LinkedList<>(currentSequence));
                currentSum -= currentSequence.removeFirst();
            }
        } return results;
    }


}
