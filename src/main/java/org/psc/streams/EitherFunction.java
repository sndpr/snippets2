package org.psc.streams;

import io.vavr.control.Either;

import java.util.function.Function;

@FunctionalInterface
public interface EitherFunction<T, R, EX extends Throwable> extends Function<T, Either<EX, R>> {

    @Override
    default Either<EX, R> apply(T t) {
        try {
            return Either.right(throwingApply(t));
        } catch (Throwable throwable) {
            //noinspection unchecked
            return Either.left((EX) throwable);
        }
    }

    R throwingApply(T t) throws EX;

    static <T, R, EX extends Throwable> EitherFunction<T, R, EX> of(Function<T, Either<EX, R>> function) {
        return (EitherFunction<T, R, EX>) function;
    }

}
