package com.coditory.quark.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static com.coditory.quark.config.Preconditions.expectNonNull;
import static com.coditory.quark.config.Preconditions.expectUnique;

public final class Profiles {
    private static final Profiles EMPTY = new Profiles(List.of());

    public static ProfilesResolver resolver() {
        return new ProfilesResolver();
    }

    private final List<String> profiles;
    private final Set<String> set;

    public static Profiles empty() {
        return EMPTY;
    }

    public static Profiles of(String... profiles) {
        return new Profiles(List.of(profiles));
    }

    public static Profiles of(List<String> profiles) {
        return new Profiles(profiles);
    }

    Profiles(List<String> profiles) {
        expectNonNull(profiles, "profiles");
        expectUnique(profiles);
        for (String profile : profiles) {
            expectNonBlank(profile, "profile");
        }
        this.profiles = List.copyOf(profiles);
        this.set = Set.copyOf(profiles);
    }

    public List<String> getValues() {
        return profiles;
    }

    public boolean isActive(String profile) {
        return set.contains(profile);
    }

    public boolean isAnyActive(Collection<String> profiles) {
        return profiles.stream()
                .anyMatch(set::contains);
    }

    public boolean isAnyActive(String... profiles) {
        return Arrays.stream(profiles)
                .anyMatch(set::contains);
    }

    public boolean areAllActive(Collection<String> profiles) {
        return set.containsAll(profiles);
    }

    public boolean areAllActive(String... profiles) {
        return Arrays.stream(profiles)
                .allMatch(set::contains);
    }

    public <R> R on(String profile, Supplier<R> supplier) {
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
        Profiles profiles1 = (Profiles) o;
        return Objects.equals(profiles, profiles1.profiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profiles);
    }
}
