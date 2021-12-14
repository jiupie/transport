package com.wl.utils;

import java.util.Objects;

public class Asserts {

    public static void assertTrue(Boolean b, String message) {
        if (!b) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(Object o, String message) {
        if (o == null) {
            Objects.requireNonNull(o, message);
        }
    }

    public static void notBlank(String str, String message) {
        if (str == null || StringUtil.hasText(str)) {
            throw new IllegalArgumentException(message);
        }
    }

}
