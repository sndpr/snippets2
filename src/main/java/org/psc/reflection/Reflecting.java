package org.psc.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class Reflecting {

    public static void main(String[] args) throws Throwable {
        var someSemiDataHolder = new SomeSemiDataHolder();
        someSemiDataHolder.setSomeId(1561);
        someSemiDataHolder.setSomeValue("some value");

        MethodHandle getSomeValue = MethodHandles.lookup()
                .findVirtual(SomeSemiDataHolder.class, "getSomeValue", MethodType.methodType(String.class));
        var someValue = getSomeValue.invoke(someSemiDataHolder);
        System.out.println(someValue);

        MethodHandle getSomeValueByField = MethodHandles.lookup()
                .findGetter(SomeSemiDataHolder.class, "someValue", String.class);
        var anotherValue = getSomeValueByField.invoke(someSemiDataHolder);
        System.out.println(anotherValue);


        var someSemiDataHolder2 = new SomeSemiDataHolder();
        someSemiDataHolder2.setSomeId(9954);
        someSemiDataHolder2.setSomeValue("some other");

        var anotherSemiDataHolder = new AnotherSemiDataHolder("hi");

        var a = getSomeValue.invoke(someSemiDataHolder2);
        System.out.println(a);

        var getSomeValueFromAnother = MethodHandles.lookup()
                .findVirtual(AnotherSemiDataHolder.class, "getSomeValue", MethodType.methodType(String.class));
        var b = getSomeValueFromAnother.invoke(anotherSemiDataHolder);
        System.out.println(b);

    }

    public static class AnotherSemiDataHolder {
        private final String someValue;

        public AnotherSemiDataHolder(String someValue) {
            this.someValue = someValue;
        }

        public String getSomeValue() {
            return someValue;
        }
    }

    public static class SomeSemiDataHolder {

        private String someValue;

        private int someId;

        public String getSomeValue() {
            return someValue;
        }

        public int getSomeId() {
            return someId;
        }

        public SomeSemiDataHolder setSomeValue(String someValue) {
            this.someValue = someValue;
            return this;
        }

        public SomeSemiDataHolder setSomeId(int someId) {
            this.someId = someId;
            return this;
        }
    }
}
