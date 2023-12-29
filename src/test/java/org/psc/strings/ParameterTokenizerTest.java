package org.psc.strings;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterTokenizerTest {

    @Test
    void shouldTokenizeParameters() {
        var params = " abc(hello    (22.22.22))   ok() no(some, thing)  ";
        Map<String, String> tokenized = new ParameterTokenizer().tokenize(params);
        assertThat(tokenized).containsEntry("abc", "hello    (22.22.22)")
                .containsEntry("ok", "")
                .containsEntry("no", "some, thing");
    }

}