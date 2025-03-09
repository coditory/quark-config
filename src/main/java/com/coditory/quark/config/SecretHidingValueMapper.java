package com.coditory.quark.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static com.coditory.quark.config.Preconditions.expectNonEmpty;

public class SecretHidingValueMapper implements ConfigEntryMapper {
    private static final Pattern WORD_FINDER = Pattern.compile("(([A-Z]?[a-z]+)|([A-Z]))");
    private static final String DEFAULT_SECRET_REPLACEMENT = "***";
    private static final Set<String> DEFAULT_SECRET_NAMES = Set.of(
            "password", "passwords",
            "secret", "secrets",
            "token", "tokens",
            "key", "keys",
            "apiKey", "apiKeys"
    );

    private static final SecretHidingValueMapper DEFAULT_SECRET_HIDING_VALUE_MAPPER =
            new SecretHidingValueMapper(DEFAULT_SECRET_NAMES, DEFAULT_SECRET_REPLACEMENT);

    public static SecretHidingValueMapper defaultSecretHidingValueMapper() {
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
        int lastDot = path.lastIndexOf('.');
        String last = lastDot < 0 ? path : path.substring(lastDot + 1);
        return findWordsInMixedCase(last).stream()
                .flatMap(chunk -> Arrays.stream(chunk.split("-")))
                .map(String::toLowerCase)
                .anyMatch(secretNames::contains);
    }

    private List<String> findWordsInMixedCase(String text) {
        Matcher matcher = WORD_FINDER.matcher(text);
        List<String> words = new ArrayList<>();
        while (matcher.find()) {
            words.add(matcher.group(0));
        }
        return words;
    }
}
