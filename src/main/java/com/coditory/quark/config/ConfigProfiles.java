package com.coditory.quark.config;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static com.coditory.quark.config.Preconditions.expectNonNull;
import static com.coditory.quark.config.Preconditions.expectUnique;

public final class ConfigProfiles {
    private static final ConfigProfiles EMPTY = new ConfigProfiles(List.of());

    @NotNull
    public static ConfigProfilesResolver resolver() {
        return new ConfigProfilesResolver();
    }

    private final List<String> profiles;
    private final Set<String> set;

    @NotNull
    public static ConfigProfiles empty() {
        return EMPTY;
    }

    @NotNull
    public static ConfigProfiles of(@NotNull String... profiles) {
        return new ConfigProfiles(List.of(profiles));
    }

    @NotNull
    public static ConfigProfiles of(@NotNull List<String> profiles) {
        return new ConfigProfiles(profiles);
    }

    ConfigProfiles(List<String> profiles) {
        expectNonNull(profiles, "profiles");
        expectUnique(profiles);
        for (String profile : profiles) {
            expectNonBlank(profile, "profile");
        }
        this.profiles = List.copyOf(profiles);
        this.set = Set.copyOf(profiles);
    }

    @NotNull
    public List<String> getValues() {
        return profiles;
    }

    public boolean isActive(@NotNull String profile) {
        return set.contains(profile);
    }

    public boolean isAnyActive(@NotNull Collection<String> profiles) {
        return profiles.stream()
                .anyMatch(set::contains);
    }

    public boolean isAnyActive(@NotNull String... profiles) {
        return Arrays.stream(profiles)
                .anyMatch(set::contains);
    }

    public boolean areAllActive(@NotNull Collection<String> profiles) {
        return set.containsAll(profiles);
    }

    public boolean areAllActive(@NotNull String... profiles) {
        return Arrays.stream(profiles)
                .allMatch(set::contains);
    }

    public <R> R on(@NotNull String profile, @NotNull Supplier<R> supplier) {
        if (isActive(profile)) {
            return supplier.get();
        }
        return null;
    }

    public boolean isEmpty() {
        return profiles.isEmpty();
    }

    @Override
    public String toString() {
        return "Profiles{profiles=" + profiles + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigProfiles profiles1 = (ConfigProfiles) o;
        return Objects.equals(profiles, profiles1.profiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profiles);
    }
}
