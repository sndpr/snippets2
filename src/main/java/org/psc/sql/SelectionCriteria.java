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

    interface StatementBuilder {

        // variant 1
        <T> StatementBuilder and(IntermediateCriterion criterion);

        <T> StatementBuilder or(IntermediateCriterion criterion);

        // variant 2
        StatementBuilder and(Consumer<StatementBuilder> statementBuilderConsumer);

        StatementBuilder or(Consumer<StatementBuilder> statementBuilderConsumer);

        StatementBuilder and();

        StatementBuilder or();

        <T> StatementBuilder is(String parameterName, T value, Function<T, String> valueMapper);

        <T> StatementBuilder in(String parameterName, List<T> value, Function<T, String> valueMapper);

        StatementBuilder like(String parameterName, String value);

        String build();
    }

    static class ProtoBuilder implements StatementBuilder {

        private final Deque<String> filters = new ArrayDeque<>();

        private final Deque<CriterionSpec> var2Filters = new ArrayDeque<>();

        private final Stack<String> clauseOperatorStack = new Stack<>();

        // variant 1
        @Override
        public <T> StatementBuilder and(IntermediateCriterion criterion) {
            // first one without "clause operator" to allow for WHEN???
            if (!filters.isEmpty()) {
                criterion.withClauseOperator("AND");
            }
            filters.add(criterion.createFilterClause());
            return this;
        }

        @Override
        public <T> StatementBuilder or(IntermediateCriterion criterion) {
            // first one without "clause operator" to allow for WHEN???
            if (!filters.isEmpty()) {
                criterion.withClauseOperator("OR");
            }
            filters.add(criterion.createFilterClause());
            return this;
        }

        // variant 2
        @Override
        public StatementBuilder and(Consumer<StatementBuilder> statementBuilderConsumer) {
            clauseOperatorStack.push("AND (");
            statementBuilderConsumer.accept(this);
            var2Filters.getLast().setSuffix(")");
            return this;
        }

        @Override
        public StatementBuilder or(Consumer<StatementBuilder> statementBuilderConsumer) {
            clauseOperatorStack.push("OR (");
            statementBuilderConsumer.accept(this);
            var2Filters.getLast().setSuffix(")");
            return this;
        }

        @Override
        public StatementBuilder and() {
            clauseOperatorStack.push("AND ");
            return this;
        }

        @Override
        public StatementBuilder or() {
            clauseOperatorStack.push("OR ");
            return this;
        }

        @Override
        public <T> StatementBuilder is(String parameterName, T value, Function<T, String> valueMapper) {
            var clauseOperator = clauseOperatorStack.empty() ? "AND " : clauseOperatorStack.pop();
            var2Filters.add(new CriterionSpec().withPrefix(clauseOperator)
                    .withCriterion(parameterName + " = " + valueMapper.apply(value)));
            return this;
        }

        @Override
        public <T> StatementBuilder in(String parameterName, List<T> value, Function<T, String> valueMapper) {
            return null;
        }

        @Override
        public StatementBuilder like(String parameterName, String value) {
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
