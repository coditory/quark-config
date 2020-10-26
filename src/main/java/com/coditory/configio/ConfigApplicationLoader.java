package com.coditory.configio;

import java.util.Map;

import static com.coditory.configio.Preconditions.expectNonBlank;
import static com.coditory.configio.Preconditions.expectNonNull;

public class ConfigApplicationLoader {
    private String[] args = null;
    private String argsPrefix = "config";
    private Map<String, String> argsAliases = Map.of();
    private String profileArgument = "profile";
    private String defaultProfile = "local";
    private String externalConfigFileArgument = "config.external";
    private String configBaseName = "application";

    ConfigApplicationLoader() {
        // deliberately empty
    }

    public ConfigApplicationLoader withArgs(String[] args) {
        this.args = expectNonNull(args, "args");
        return this;
    }

    public ConfigApplicationLoader withArgsAliases(Map<String, String> argsAliases) {
        this.argsAliases = expectNonNull(argsAliases, "argsAliases");
        return this;
    }

    public ConfigApplicationLoader withArgsConfigPrefix(String argsPrefix) {
        this.argsPrefix = expectNonBlank(argsPrefix, "argsPrefix");
        return this;
    }

    public ConfigApplicationLoader withProfileArgument(String profileArgument) {
        this.profileArgument = expectNonBlank(profileArgument, "profileArgument");
        return this;
    }

    public ConfigApplicationLoader withDefaultProfile(String defaultProfile) {
        this.defaultProfile = expectNonBlank(defaultProfile, "defaultProfile");
        return this;
    }

    public ConfigApplicationLoader withExternalConfigFileArgument(String externalConfigFileArgument) {
        this.externalConfigFileArgument = expectNonBlank(externalConfigFileArgument, "externalConfigFileArgument");
        return this;
    }

    public ConfigApplicationLoader withConfigBaseName(String configBaseName) {
        this.configBaseName = expectNonBlank(configBaseName, "configBaseName");
        return this;
    }

    public Config load() {
        Config allArgsConfig = allArgsConfig();
        Config filteredArgsConfig = filteredArgsConfig(allArgsConfig);
        Config externalConfig = externalConfig(allArgsConfig);
        Config profileConfig = profileConfig(allArgsConfig);
        Config config = ConfigFactory.loadFromClasspath(configBaseName);
        Config resolveConfig = Config.builder()
                .withValue("system", ConfigFactory.buildFromSystemProperties())
                .withValue("env", ConfigFactory.buildFromSystemEnvironment())
                .withValue("args", allArgsConfig)
                .build();
        return config
                .withValues(profileConfig)
                .withValues(externalConfig)
                .withValues(filteredArgsConfig)
                .resolveExpressions(resolveConfig);
    }

    private Config profileConfig(Config argsConfig) {
        String profile = profileArgument != null
                ? argsConfig.getString(profileArgument, defaultProfile)
                : defaultProfile;
        return profile != null
                ? ConfigFactory.loadFromClasspathOrEmpty(configBaseName + "-" + profile)
                : Config.empty();
    }

    private Config allArgsConfig() {
        return args != null
                ? ConfigFactory.buildFromArgs(args, argsAliases)
                : Config.empty();
    }

    private Config filteredArgsConfig(Config argsConfig) {
        Config config = externalConfigFileArgument != null
                ? argsConfig.remove(externalConfigFileArgument)
                : argsConfig;
        config = profileArgument != null
                ? config.remove(profileArgument)
                : config;
        return argsPrefix != null
                ? config.getSubConfigOrEmpty(argsPrefix)
                : config;
    }

    private Config externalConfig(Config argsConfig) {
        return externalConfigFileArgument != null && argsConfig.contains(externalConfigFileArgument)
                ? ConfigFactory.loadFromFileSystem(argsConfig.getString(externalConfigFileArgument))
                : Config.empty();
    }
}
