package org.psc.varargs.overloads;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VarargsOverloadsTest {

    @Test
    void testVarargsOverloads() {
        assertThat(VarargsOverloads.callMe("hello")).isEqualTo("modified_hello");
        assertThat(VarargsOverloads.callMe("hello", s -> s)).isEqualTo("hello");
        assertThat(VarargsOverloads.callMe("hello", new VarargsOverloads.StringModifier())).isEqualTo("modified_hello");
    }

}