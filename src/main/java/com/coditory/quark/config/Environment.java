package com.coditory.quark.config;

import java.util.Objects;

import static com.coditory.quark.config.Preconditions.expectNonNull;

public final class Environment {
    private final Config config;
    private final Profiles profiles;

    public Environment(Config config, Profiles profiles) {
        this.config = expectNonNull(config, "config");
        this.profiles = expectNonNull(profiles, "profiles");
    }

    public Config getConfig() {
        return config;
    }

    public Profiles getProfiles() {
        return profiles;
    }

    @Override
    public String toString() {
        return "Environment{" +
                "config=" + config +
                ", profiles=" + profiles +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Environment that = (Environment) o;
        return Objects.equals(config, that.config) && Objects.equals(profiles, that.profiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(config, profiles);
    }
}