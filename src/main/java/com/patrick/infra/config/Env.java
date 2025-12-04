package com.patrick.infra.config;

import io.github.cdimascio.dotenv.Dotenv;

public final class Env {
    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();

    private Env() {}

    public static String get(String key, boolean required) {
        String variable = System.getenv(key);

        if (variable == null || variable.isBlank()) {
            variable = DOTENV.get(key);
        }
        if ((variable == null || variable.isBlank()) && required) {
            throw new IllegalStateException("Missing required env variable: " + key);
        }
        return variable;
    }
    public static String getOrDefault(String key, String defaultValue) {
        String variable = System.getenv(key);
        if (variable == null || variable.isBlank()) {
            variable = DOTENV.get(key);
        }
        return (variable == null || variable.isBlank()) ? defaultValue : variable;
    }

}
