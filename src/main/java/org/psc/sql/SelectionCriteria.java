package org.psc.sql;

import lombok.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

// PROTOTYPING
public class SelectionCriteria {

    interface Builder {

        // variant 1
        <T> Builder and(IntermediateCriterion criterion);

        <T> Builder or(IntermediateCriterion criterion);

        // variant 2
        Builder and(Consumer<Builder> statementBuilderConsumer);

        Builder or(Consumer<Builder> statementBuilderConsumer);

        Builder and();

        Builder or();

        <T> Builder is(String parameterName, T value, Function<T, String> valueMapper);

        <T> Builder in(String parameterName, List<T> value, Function<T, String> valueMapper);

        Builder like(String parameterName, String value);

        String build();
    }

    static class ProtoBuilder implements Builder {

        private final Deque<String> filters = new ArrayDeque<>();

        private final Deque<CriterionSpec> var2Filters = new ArrayDeque<>();

        private final Stack<String> clauseOperatorStack = new Stack<>();

        // variant 1
        @Override
        public <T> Builder and(IntermediateCriterion criterion) {
            // first one without "clause operator" to allow for WHEN???
            if (!filters.isEmpty()) {
                criterion.withClauseOperator("AND");
            }
            filters.add(criterion.createFilterClause());
            return this;
        }

        @Override
        public <T> Builder or(IntermediateCriterion criterion) {
            // first one without "clause operator" to allow for WHEN???
            if (!filters.isEmpty()) {
                criterion.withClauseOperator("OR");
            }
            filters.add(criterion.createFilterClause());
            return this;
        }

        // variant 2
        @Override
        public Builder and(Consumer<Builder> statementBuilderConsumer) {
            clauseOperatorStack.push("AND (");
            statementBuilderConsumer.accept(this);
            var2Filters.getLast().setSuffix(")");
            return this;
        }

        @Override
        public Builder or(Consumer<Builder> statementBuilderConsumer) {
            clauseOperatorStack.push("OR (");
            statementBuilderConsumer.accept(this);
            var2Filters.getLast().setSuffix(")");
            return this;
        }

        @Override
        public Builder and() {
            clauseOperatorStack.push("AND ");
            return this;
        }

        @Override
        public Builder or() {
            clauseOperatorStack.push("OR ");
            return this;
        }

        @Override
        public <T> Builder is(String parameterName, T value, Function<T, String> valueMapper) {
            var clauseOperator = clauseOperatorStack.empty() ? "AND " : clauseOperatorStack.pop();
            var2Filters.add(new CriterionSpec().withPrefix(clauseOperator)
                    .withCriterion(parameterName + " = " + valueMapper.apply(value)));
            return this;
        }

        @Override
        public <T> Builder in(String parameterName, List<T> value, Function<T, String> valueMapper) {
            return null;
        }

        @Override
        public Builder like(String parameterName, String value) {
            return null;
        }

        @Override
        public String build() {
            return var2Filters.stream()
                    .map(it -> it.prefix + it.criterion + it.suffix)
                    .collect(Collectors.joining(" "));
            //            return String.join(" ", filters);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @With
    static class CriterionSpec {
        private String prefix;
        private String criterion;
        private String suffix = "";
    }

    static class Operators {
        <T> DefaultIntermediateCriterion<T> is(String parameterName, T value, Function<T, String> valueMapper) {
            return new DefaultIntermediateCriterion<T>()
                    .withParameterName(parameterName)
                    .withValue(value)
                    .withValueMapper(valueMapper)
                    .withValueOperator("=");
        }

        <T> DefaultIntermediateCriterion<T> not(String parameterName, T value, Function<T, String> valueMapper) {
            return new DefaultIntermediateCriterion<T>()
                    .withParameterName(parameterName)
                    .withValue(value)
                    .withValueMapper(valueMapper)
                    .withValueOperator("!=");
        }

        <T> IntermediateListCriterion<T> in(String parameterName, List<T> values, Function<T, String> valueMapper) {
            return new IntermediateListCriterion<T>()
                    .withParameterName(parameterName)
                    .withValues(values)
                    .withValueMapper(valueMapper)
                    .withValueOperator("IN");
        }

        DefaultIntermediateCriterion<String> like(String parameterName, String value) {
            return new DefaultIntermediateCriterion<String>().withParameterName(parameterName)
                    .withValue(value)
                    .withValueOperator("LIKE");
        }
    }

    @RequiredArgsConstructor
    enum Operator {
        IS("="),
        NOT("!="),
        EQ("="),
        NOT_EQ("!="),
        IN("IN"),
        NOT_IN("NOT IN"),
        LESS("<"),
        LESS_OR_EQ("<="),
        GREATER(">"),
        GREATER_OR_EQ(">="),
        LIKE("LIKE"),
        ;

        private final String operator;

        public <T> DefaultIntermediateCriterion<T> apply(String parameterName, T value,
                Function<T, String> valueMapper) {
            return new DefaultIntermediateCriterion<T>().withParameterName(parameterName)
                    .withValue(value)
                    .withValueMapper(valueMapper)
                    .withValueOperator(operator);
        }
    }

    interface IntermediateCriterion {
        default IntermediateCriterion withClauseOperator(String clauseOperator) {
            setClauseOperator(clauseOperator);
            return this;
        }

        void setClauseOperator(String clauseOperator);

        String createFilterClause();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @With
    static class DefaultIntermediateCriterion<T> implements IntermediateCriterion {
        private String parameterName;
        private T value;
        private Function<T, String> valueMapper;
        private String valueOperator;
        private String clauseOperator;

        @Override
        public String createFilterClause() {
            return clauseOperator + " " + parameterName + " " + valueOperator + " " + valueMapper.apply(value);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @With
    static class IntermediateListCriterion<T> implements IntermediateCriterion {
        private String parameterName;
        private List<T> values;
        private Function<T, String> valueMapper;
        private String valueOperator;
        private String clauseOperator;

        @Override
        public String createFilterClause() {
            return clauseOperator + " " + parameterName + " " + valueOperator + " " +
                    values.stream()
                            .map(valueMapper)
                            .collect(Collectors.joining(", ", "(", ")"));
        }
    }

}
