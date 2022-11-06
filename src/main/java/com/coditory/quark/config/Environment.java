package com.coditory.quark.config;

import org.jetbrains.annotations.NotNull;

import static com.coditory.quark.config.Preconditions.expectNonNull;

public record Environment(Config config, Profiles profiles) {
    public Environment(@NotNull Config config, @NotNull Profiles profiles) {
        this.config = expectNonNull(config, "config");
        this.profiles = expectNonNull(profiles, "profiles");
    }
}