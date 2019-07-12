package org.psc.streams;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

@Slf4j
public class GroupingReducerTest {


    @Test
    void testReduce() {
        GroupingReducer.TypeWithList first = new GroupingReducer.TypeWithList().withId("first").withData("some");
        GroupingReducer.TypeWithList second = new GroupingReducer.TypeWithList().withId("second").withData("0");
        GroupingReducer.TypeWithList third = new GroupingReducer.TypeWithList().withId("third").withData("3");
        GroupingReducer.TypeWithList fourth = new GroupingReducer.TypeWithList().withId("fourth").withData("45242");

        GroupingReducer.JoinType joinType1 = new GroupingReducer.JoinType().withJoinedTypes(
                Map.of(GroupingReducer.TypeWithList.class, first, Integer.class, 1));

        GroupingReducer.JoinType joinType2 = new GroupingReducer.JoinType().withJoinedTypes(
                Map.of(GroupingReducer.TypeWithList.class, first, Integer.class, 2));

        GroupingReducer.JoinType joinType3 = new GroupingReducer.JoinType().withJoinedTypes(
                Map.of(GroupingReducer.TypeWithList.class, first, Integer.class, 3));

        GroupingReducer.JoinType joinType4 = new GroupingReducer.JoinType().withJoinedTypes(
                Map.of(GroupingReducer.TypeWithList.class, second));

        GroupingReducer.JoinType joinType5 = new GroupingReducer.JoinType().withJoinedTypes(
                Map.of(GroupingReducer.TypeWithList.class, third, Integer.class, 54545));

        GroupingReducer.JoinType joinType6 = new GroupingReducer.JoinType().withJoinedTypes(
                Map.of(GroupingReducer.TypeWithList.class, third, Integer.class, 78972));

        GroupingReducer.JoinType joinType7 = new GroupingReducer.JoinType().withJoinedTypes(
                Map.of(GroupingReducer.TypeWithList.class, third, Integer.class, 978542));

        GroupingReducer.JoinType joinType8 = new GroupingReducer.JoinType().withJoinedTypes(
                Map.of(GroupingReducer.TypeWithList.class, fourth, Integer.class, 450458));

        GroupingReducer.JoinType joinType9 = new GroupingReducer.JoinType().withJoinedTypes(
                Map.of(GroupingReducer.TypeWithList.class, fourth, Integer.class, 556872));


        List<GroupingReducer.JoinType> joinTypes =
                List.of(joinType1, joinType2, joinType3, joinType4, joinType5, joinType6, joinType7, joinType8,
                        joinType9);

        GroupingReducer groupingReducer = new GroupingReducer();
        groupingReducer.reduce(joinTypes);
    }
}
