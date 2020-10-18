package com.coditory.configio;

import java.util.Objects;
import java.util.Set;

interface ValueStringifier {
    String stringifyValue(Path path, Object value);
}

class AllValueStringifier implements ValueStringifier {

    @Override
    public String stringifyValue(Path path, Object value) {
        return Objects.toString(value);
    }
}

class SecureValueStringifier implements ValueStringifier {
    private static final Set<String> DEFAULT_SECRET_CHUNKS = Set.of(
            "password", "passwords",
            "secret", "secrets",
            "token", "tokens"
    );

    private static final SecureValueStringifier DEFAULT_SECURE_STRINGIFIER = new SecureValueStringifier(DEFAULT_SECRET_CHUNKS);

    static SecureValueStringifier defaultSecureStringifier() {
        return DEFAULT_SECURE_STRINGIFIER;
    }

    private final Set<String> secretChunks;

    SecureValueStringifier(Set<String> secretChunks) {
        this.secretChunks = Set.copyOf(secretChunks);
    }

    @Override
    public String stringifyValue(Path path, Object value) {
        return hasSecretChunk(path)
                ? "***"
                : Objects.toString(value);
    }

    private boolean hasSecretChunk(Path path) {
        return path.getPropertyNames().stream()
                .map(String::toLowerCase)
                .anyMatch(chunk ->
                        secretChunks.stream()
                                .anyMatch(chunk::contains)
                );
    }
}