package com.coditory.configio;

public class ApplicationConfigLoader {
    public static ApplicationConfigLoader applicationConfigLoader() {
        return new ApplicationConfigLoader();
    }

    private String[] args = null;
    private String argsPrefix = "config";
    private String profileArgument = "profile";
    private String defaultProfile = "local";
    private String externalConfigFileArgument = "config.external";
    private String configBaseName = "application";

    public ApplicationConfigLoader withArgs(String[] args) {
        this.args = args;
        return this;
    }

    public ApplicationConfigLoader withProfileArgument(String profileArgument) {
        this.profileArgument = profileArgument;
        return this;
    }

    public ApplicationConfigLoader withDefaultProfile(String defaultProfile) {
        this.defaultProfile = defaultProfile;
        return this;
    }

    public ApplicationConfigLoader withExternalConfigFileArgument(String externalConfigFileArgument) {
        this.externalConfigFileArgument = externalConfigFileArgument;
        return this;
    }

    public ApplicationConfigLoader withConfigBaseName(String configBaseName) {
        this.configBaseName = configBaseName;
        return this;
    }

    public Config load() {
        Config allArgsConfig = allArgsConfig();
        Config filteredArgsConfig = filteredArgsConfig(allArgsConfig);
        Config externalConfig = externalConfig(allArgsConfig);
        Config profileConfig = profileConfig(allArgsConfig);
        Config config = ConfigLoader.loadFromClasspathInAnyFormat(configBaseName);
        Config resolveConfig = Config.builder()
                .withValue("system", ConfigLoader.loadSystemProperties())
                .withValue("env", ConfigLoader.loadSystemEnvironment())
                .withValue("args", allArgsConfig)
                .build();
        return config
                .addOverrides(profileConfig)
                .addOverrides(externalConfig)
                .addOverrides(filteredArgsConfig)
                .resolveWith(resolveConfig);
    }

    private Config profileConfig(Config argsConfig) {
        String profile = profileArgument != null
                ? argsConfig.getString(profileArgument, defaultProfile)
                : defaultProfile;
        return profile != null
                ? ConfigLoader.loadFromClasspathInAnyFormat(configBaseName + "-" + profile)
                : Config.empty();
    }

    private Config allArgsConfig() {
        return args != null
                ? Config.fromArgs(args)
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
                ? config.subConfig(argsPrefix)
                : config;
    }

    private Config externalConfig(Config argsConfig) {
        return externalConfigFileArgument != null && argsConfig.contains(externalConfigFileArgument)
                ? ConfigLoader.loadFromFileSystem(argsConfig.getString(externalConfigFileArgument))
                : Config.empty();
    }
}
