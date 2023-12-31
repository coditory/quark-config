package com.coditory.quark.config;

import org.jetbrains.annotations.NotNull;

import static com.coditory.quark.config.Preconditions.expectNonNull;

public record Environment(Config config, ConfigProfiles profiles) {
    public Environment(@NotNull Config config, @NotNull ConfigProfiles profiles) {
        this.config = expectNonNull(config, "config");
        this.profiles = expectNonNull(profiles, "profiles");
    }
}