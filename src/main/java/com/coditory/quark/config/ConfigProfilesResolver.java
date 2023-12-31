package com.coditory.quark.config;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.coditory.quark.config.Preconditions.expect;
import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static com.coditory.quark.config.Preconditions.expectNonEmpty;
import static com.coditory.quark.config.Preconditions.expectNonNull;
import static com.coditory.quark.config.Preconditions.expectUnique;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public final class ConfigProfilesResolver {
    private final ArgumentsParser argumentsParser = new ArgumentsParser();
    private String profileArgName = "profile";
    private List<String> enforcedProfiles;
    private List<String> defaultProfiles;
    private List<String> firstIfNoneMatchProfiles;
    private Set<String> allowedProfiles;
    private Set<String> exclusiveProfiles;
    private Predicate<List<String>> profilesValidator;
    private Function<List<String>, List<String>> profilesMapper;
    private Integer minProfiles;
    private Integer maxProfiles;

    ConfigProfilesResolver() {
        // deliberately empty
    }

    @NotNull
    public ConfigProfilesResolver minProfileCount(int min) {
        expect(min >= 0, "Expected min >= 0");
        if (maxProfiles != null) {
            expect(min <= maxProfiles, "Expected min <= maxProfiles");
        }
        this.minProfiles = min;
        return this;
    }

    @NotNull
    public ConfigProfilesResolver maxProfileCount(int max) {
        expect(max >= 0, "Expected max >= 0");
        if (minProfiles != null) {
            expect(max <= minProfiles, "Expected max >= minProfiles");
        }
        this.maxProfiles = max;
        return this;
    }

    @NotNull
    public ConfigProfilesResolver expectedProfileCount(int count) {
        expect(count >= 0, "Expected count >= 0");
        minProfileCount(count);
        maxProfileCount(count);
        return this;
    }

    @NotNull
    public ConfigProfilesResolver firstIfNoneMatch(@NotNull List<String> firstIfNoneMatch) {
        expectNonEmpty(firstIfNoneMatch, "firstIfNoneMatch");
        this.firstIfNoneMatchProfiles = List.copyOf(firstIfNoneMatch);
        return this;
    }

    @NotNull
    public ConfigProfilesResolver allowedProfiles(@NotNull String... allowedProfiles) {
        expectNonNull(allowedProfiles, "allowedProfiles");
        return allowedProfiles(Set.of(allowedProfiles));
    }

    @NotNull
    public ConfigProfilesResolver allowedProfiles(@NotNull Collection<String> allowedProfiles) {
        expectNonNull(allowedProfiles, "allowedProfiles");
        this.allowedProfiles = new HashSet<>(allowedProfiles);
        return this;
    }

    @NotNull
    public ConfigProfilesResolver exclusiveProfiles(@NotNull String... exclusiveProfiles) {
        expectNonNull(exclusiveProfiles, "exclusiveProfiles");
        return exclusiveProfiles(Set.of(exclusiveProfiles));
    }

    @NotNull
    public ConfigProfilesResolver exclusiveProfiles(@NotNull Collection<String> exclusiveProfiles) {
        expectNonNull(exclusiveProfiles, "exclusiveProfiles");
        this.exclusiveProfiles = new HashSet<>(exclusiveProfiles);
        return this;
    }

    @NotNull
    public ConfigProfilesResolver profileArgName(@NotNull String profileArgName) {
        this.profileArgName = expectNonBlank(profileArgName, "profileArgName");
        return this;
    }

    @NotNull
    public ConfigProfilesResolver defaultProfiles(@NotNull String... defaultProfiles) {
        expectNonNull(defaultProfiles, "defaultProfiles");
        return defaultProfiles(Arrays.asList(defaultProfiles));
    }

    @NotNull
    public ConfigProfilesResolver defaultProfiles(@NotNull List<String> defaultProfiles) {
        expectNonNull(defaultProfiles, "defaultProfiles");
        this.defaultProfiles = List.copyOf(defaultProfiles);
        return this;
    }

    @NotNull
    public ConfigProfilesResolver profiles(@NotNull String... profiles) {
        expectNonNull(profiles, "profiles");
        this.enforcedProfiles = Arrays.asList(profiles);
        return this;
    }

    @NotNull
    public ConfigProfilesResolver profiles(@NotNull List<String> profiles) {
        expectNonNull(profiles, "profiles");
        this.enforcedProfiles = List.copyOf(profiles);
        return this;
    }

    @NotNull
    public ConfigProfilesResolver profiles(@NotNull ConfigProfiles profiles) {
        expectNonNull(profiles, "profiles");
        this.enforcedProfiles = profiles.getValues();
        return this;
    }

    @NotNull
    public ConfigProfilesResolver profilesMapper(@NotNull Function<List<String>, List<String>> mapper) {
        this.profilesMapper = expectNonNull(mapper, "mapper");
        return this;
    }

    @NotNull
    public ConfigProfilesResolver profilesValidator(@NotNull Predicate<List<String>> validator) {
        this.profilesValidator = expectNonNull(validator, "validator");
        return this;
    }

    @NotNull
    public ConfigProfilesResolver argsMapping(@NotNull Map<String[], String[]> mapping) {
        expectNonNull(mapping, "mapping");
        argumentsParser.withMapping(mapping);
        return this;
    }

    @NotNull
    public ConfigProfilesResolver addArgsMapping(@NotNull List<String> args, @NotNull List<String> mapping) {
        expectNonNull(args, "args");
        expectNonNull(mapping, "mapping");
        argumentsParser.addMapping(args.toArray(new String[0]), mapping.toArray(new String[0]));
        return this;
    }

    @NotNull
    public ConfigProfilesResolver addArgsMapping(@NotNull String[] args, @NotNull String[] mapping) {
        expectNonNull(args, "args");
        expectNonNull(mapping, "mapping");
        argumentsParser.addMapping(args, mapping);
        return this;
    }

    @NotNull
    public ConfigProfilesResolver argsAliases(@NotNull Map<String, String> aliases) {
        expectNonNull(aliases, "aliases");
        argumentsParser.withAliases(aliases);
        return this;
    }

    @NotNull
    public ConfigProfilesResolver addArgsAlias(@NotNull String arg, @NotNull String alias) {
        expectNonNull(arg, "arg");
        expectNonNull(alias, "alias");
        argumentsParser.addAlias(arg, alias);
        return this;
    }

    @NotNull
    public ConfigProfiles resolve() {
        return resolve(Config.empty());
    }

    @NotNull
    public ConfigProfiles resolve(@NotNull String... args) {
        expectNonNull(args, "args");
        Map<String, Object> values = argumentsParser.parse(args);
        Config argsConfig = Config.of(values);
        return resolve(argsConfig);
    }

    @NotNull
    public ConfigProfiles resolve(@NotNull Config argsConfig) {
        expectNonNull(argsConfig, "argsConfig");
        List<String> profiles = profiles(argsConfig);
        return new ConfigProfiles(profiles);
    }

    private List<String> profiles(Config argsConfig) {
        Stream<String> profilesRaw = Stream.of();
        if (enforcedProfiles != null) {
            profilesRaw = enforcedProfiles.stream();
        } else if (profileArgName != null && argsConfig.contains(profileArgName)) {
            profilesRaw = Stream.of(argsConfig.getString(profileArgName));
        }
        List<String> profiles = profilesRaw
                .flatMap(s -> stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(toList());
        profiles = profiles.isEmpty() && defaultProfiles != null && !defaultProfiles.isEmpty()
                ? defaultProfiles
                : profiles;
        if (firstIfNoneMatchProfiles != null) {
            Set<String> common = profiles.stream()
                    .filter(p -> firstIfNoneMatchProfiles.contains(p))
                    .collect(toSet());
            if (common.isEmpty()) {
                List<String> copy = new ArrayList<>();
                copy.add(firstIfNoneMatchProfiles.get(0));
                copy.addAll(profiles);
                profiles = copy;
            }
        }
        if (profilesMapper != null) {
            profiles = profilesMapper.apply(profiles);
        }
        expectUnique(profiles, "profiles");
        if (allowedProfiles != null) {
            Set<String> invalidProfiles = profiles.stream()
                    .filter(p -> !allowedProfiles.contains(p))
                    .collect(toSet());
            expect(invalidProfiles.isEmpty(), "Invalid profiles: " + invalidProfiles);
        }
        if (exclusiveProfiles != null) {
            Set<String> invalidProfiles = profiles.stream()
                    .filter(p -> exclusiveProfiles.contains(p))
                    .collect(toSet());
            expect(invalidProfiles.size() == 1, "Detected exclusive profiles: " + invalidProfiles);
        }
        if (minProfiles != null) {
            expect(profiles.size() >= minProfiles, "Too little active profiles");
        }
        if (maxProfiles != null) {
            expect(profiles.size() <= maxProfiles, "Too many active profiles");
        }
        if (profilesValidator != null && !profilesValidator.test(profiles)) {
            throw new IllegalArgumentException("Invalid profiles: " + profiles);
        }
        return profiles;
    }
}
