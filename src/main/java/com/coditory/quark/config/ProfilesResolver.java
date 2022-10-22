package com.coditory.quark.config;

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

public final class ProfilesResolver {
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

    ProfilesResolver() {
        // deliberately empty
    }

    public ProfilesResolver minProfileCount(int min) {
        expect(min >= 0, "Expected min >= 0");
        if (maxProfiles != null) {
            expect(min <= maxProfiles, "Expected min <= maxProfiles");
        }
        this.minProfiles = min;
        return this;
    }

    public ProfilesResolver maxProfileCount(int max) {
        expect(max >= 0, "Expected max >= 0");
        if (minProfiles != null) {
            expect(max <= minProfiles, "Expected max >= minProfiles");
        }
        this.maxProfiles = max;
        return this;
    }

    public ProfilesResolver expectedProfileCount(int count) {
        expect(count >= 0, "Expected count >= 0");
        minProfileCount(count);
        maxProfileCount(count);
        return this;
    }

    public ProfilesResolver firstIfNoneMatch(List<String> firstIfNoneMatch) {
        expectNonEmpty(firstIfNoneMatch, "firstIfNoneMatch");
        this.firstIfNoneMatchProfiles = List.copyOf(firstIfNoneMatch);
        return this;
    }

    public ProfilesResolver allowedProfiles(String... allowedProfiles) {
        return allowedProfiles(Set.of(allowedProfiles));
    }

    public ProfilesResolver allowedProfiles(Collection<String> allowedProfiles) {
        this.allowedProfiles = new HashSet<>(allowedProfiles);
        return this;
    }

    public ProfilesResolver exclusiveProfiles(String... exclusiveProfiles) {
        return exclusiveProfiles(Set.of(exclusiveProfiles));
    }

    public ProfilesResolver exclusiveProfiles(Collection<String> exclusiveProfiles) {
        this.exclusiveProfiles = new HashSet<>(exclusiveProfiles);
        return this;
    }

    public ProfilesResolver profileArgName(String profileArgName) {
        this.profileArgName = expectNonBlank(profileArgName, "profileArgName");
        return this;
    }

    public ProfilesResolver defaultProfiles(String... defaultProfiles) {
        return defaultProfiles(Arrays.asList(defaultProfiles));
    }

    public ProfilesResolver defaultProfiles(List<String> defaultProfiles) {
        expectNonNull(defaultProfiles, "defaultProfiles");
        this.defaultProfiles = List.copyOf(defaultProfiles);
        return this;
    }

    public ProfilesResolver profiles(String... profiles) {
        this.enforcedProfiles = Arrays.asList(profiles);
        return this;
    }

    public ProfilesResolver profiles(List<String> profiles) {
        this.enforcedProfiles = List.copyOf(profiles);
        return this;
    }

    public ProfilesResolver profiles(Profiles profiles) {
        this.enforcedProfiles = profiles.getValues();
        return this;
    }

    public ProfilesResolver profilesMapper(Function<List<String>, List<String>> mapper) {
        this.profilesMapper = mapper;
        return this;
    }

    public ProfilesResolver profilesValidator(Predicate<List<String>> validator) {
        this.profilesValidator = validator;
        return this;
    }

    public ProfilesResolver argsMapping(Map<String[], String[]> mapping) {
        argumentsParser.withMapping(mapping);
        return this;
    }

    public ProfilesResolver addArgsMapping(List<String> args, List<String> mapping) {
        argumentsParser.addMapping(args.toArray(new String[0]), mapping.toArray(new String[0]));
        return this;
    }

    public ProfilesResolver addArgsMapping(String[] args, String[] mapping) {
        argumentsParser.addMapping(args, mapping);
        return this;
    }

    public ProfilesResolver argsAliases(Map<String, String> aliases) {
        argumentsParser.withAliases(aliases);
        return this;
    }

    public ProfilesResolver addArgsAlias(String arg, String alias) {
        argumentsParser.addAlias(arg, alias);
        return this;
    }

    public Profiles resolve() {
        return resolve(Config.empty());
    }

    public Profiles resolve(String... args) {
        Map<String, Object> values = argumentsParser.parse(args);
        Config argsConfig = Config.of(values);
        return resolve(argsConfig);
    }

    public Profiles resolve(Config argsConfig) {
        List<String> profiles = profiles(argsConfig);
        return new Profiles(profiles);
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
