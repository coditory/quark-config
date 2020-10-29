package com.coditory.configio;

import java.util.Arrays;
import java.util.Set;

import static com.coditory.configio.Preconditions.expectNonBlank;
import static com.coditory.configio.Preconditions.expectNonEmpty;

public class SecretHidingValueMapper implements ConfigValueMapper {
    private static final String DEFAULT_SECRET_REPLACEMENT = "***";
    private static final Set<String> DEFAULT_SECRET_NAMES = Set.of(
            "password", "passwords",
            "secret", "secrets",
            "token", "tokens"
    );

    private static final SecretHidingValueMapper DEFAULT_SECRET_HIDING_VALUE_MAPPER =
            new SecretHidingValueMapper(DEFAULT_SECRET_NAMES, DEFAULT_SECRET_REPLACEMENT);

    static SecretHidingValueMapper defaultSecretHidingValueMapper() {
        return DEFAULT_SECRET_HIDING_VALUE_MAPPER;
    }

    private final Set<String> secretNames;
    private final String secretReplacement;

    public SecretHidingValueMapper(Set<String> secretNames, String secretReplacement) {
        this.secretNames = Set.copyOf(expectNonEmpty(secretNames, "secretNames"));
        this.secretReplacement = expectNonBlank(secretReplacement, "secretReplacement");
    }

    @Override
    public Object mapValue(String path, Object value) {
        return hasSecretChunk(path)
                ? secretReplacement
                : value;
    }

    private boolean hasSecretChunk(String path) {
        return Arrays.stream(path.split("\\."))
                .map(String::toLowerCase)
                .anyMatch(chunk ->
                        secretNames.stream()
                                .anyMatch(chunk::contains)
                );
    }
}
