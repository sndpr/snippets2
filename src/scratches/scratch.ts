interface Success<T> {
  kind: 'success'
  value: T
}

interface Failure<T> {
  kind: 'failure'
  error: T
}

type Result<T, E> = Success<T> | Failure<E>

const unit = <T>(value: T): Success<T> => ({kind: 'success', value: T});
const fail = <T>(error: T): Failure<T> => ({kind: 'failure', error: T});

unit('abc');

interface Function<I, O> {
  (input: I): O
}

const map = <I, O, E>(mappingFunction: Function<I, O>): Function<Result<I, E>, Result<O, E>> =>
    result => result.kind == 'success' ? unit(mappingFunction(result.value)) : result;

const join = <T, E>(result: Result<Result<T, E>, E>): Result<T, E> =>
  result.kind == 'failure' ? result : result.value;

const then = <I, O,E>(mappingFunction: Function<I, Result<O, E>>) => (result: Result<I, E>) =>
  join(map(mappingFunction)(result));

const railRoad = <a, e>(r: Result<a,e>) => ({
  map: <b>(f: (a:a) => b) => railRoad<b, e>(map<a, b, e>(f)(r)),
  then: <b>(f: (a:a) => Result<b,e>) => railRoad(then(f)(r))
});

class SomeType {
  value: string;
}

railRoad<SomeType, 'no-type'>({kind: 'success', value: {value: "abc"}})
  .then(type =>  unit(type))
  .map(() => new SomeType().value = "asdas");