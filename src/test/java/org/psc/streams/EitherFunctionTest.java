package org.psc.streams;

import io.vavr.control.Either;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

class EitherFunctionTest {

    @Test
    void testApply() {

        var users = List.of("a1", "b", "c2", "d", "Eee");

        List<Either<IOException, User>> eithers = users.stream()
                .map((EitherFunction<String, User, IOException>) User::fromUsername)
                .collect(toList());

        List<User> validUsers = eithers.stream()
                .filter(Either::isRight)
                .map(Either::get)
                .collect(toList());

        Assertions.assertThat(validUsers.size()).isEqualTo(3);
    }

    @Data
    @AllArgsConstructor
    private static class User {
        private String username;

        public static User fromUsername(String username) throws IOException {
            if (StringUtils.containsAny(username, "0123456789")) {
                throw new IOException("digits not allowed");
            } else {
                return new User(username);
            }
        }

    }

}