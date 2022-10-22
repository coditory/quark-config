package com.coditory.quark.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.coditory.quark.config.Preconditions.expect;
import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static com.coditory.quark.config.Preconditions.expectNonNull;

public final class ConfigLoader {
    private final ArgumentsParser argumentsParser = new ArgumentsParser();
    private final ProfilesResolver profilesResolver = new ProfilesResolver();
    private Profiles profiles = null;
    private String[] args = null;
    private String externalConfigArgName = "config";
    private String configPropArgPrefix = "config-prop";
    private Path configPath = null;
    private String commonConfigName = "base";
    private String configBaseName = "application";
    private boolean optionalBaseConfig = false;
    private Set<String> optionalProfileConfigs = null;
    private boolean profileConfigsRequired = false;

    ConfigLoader() {
        // deliberately empty
    }

    public ConfigLoader withArgs(String[] args) {
        expectNonNull(args, "args");
        this.args = copy(args);
        return this;
    }

    public ConfigLoader withArgsMapping(Map<String[], String[]> mapping) {
        argumentsParser.withMapping(mapping);
        return this;
    }

    public ConfigLoader addArgsMapping(String[] args, String[] mapping) {
        argumentsParser.addMapping(args, mapping);
        return this;
    }

    public ConfigLoader withArgsAliases(Map<String, String> aliases) {
        argumentsParser.withAliases(aliases);
        return this;
    }

    public ConfigLoader addArgsAlias(String arg, String alias) {
        argumentsParser.addAlias(arg, alias);
        return this;
    }

    public ConfigLoader withoutConfigPropArgPrefix() {
        this.configPropArgPrefix = null;
        return this;
    }

    public ConfigLoader withConfigPropArgPrefix(String configPropArgPrefix) {
        expectNonBlank(configPropArgPrefix, "configPropArgPrefix");
        expect(!Objects.equals(configPropArgPrefix, externalConfigArgName), "Expected configPropArgPrefix != externalConfigArgName");
        expect(!configPropArgPrefix.contains("."), "Expected configPropArgPrefix to contain no dots");
        this.configPropArgPrefix = configPropArgPrefix;
        return this;
    }

    public ConfigLoader withoutExternalConfigArgName() {
        this.externalConfigArgName = null;
        return this;
    }

    public ConfigLoader withExternalConfigArgName(String externalConfigArgName) {
        expectNonBlank(externalConfigArgName, "externalConfigArgName");
        expect(!Objects.equals(configPropArgPrefix, externalConfigArgName), "Expected configPropArgPrefix != externalConfigArgName");
        expect(!externalConfigArgName.contains("."), "Expected externalConfigArgName to contain no dots");
        this.externalConfigArgName = externalConfigArgName;
        return this;
    }

    public ConfigLoader withAllowedProfiles(String... allowedProfiles) {
        return withAllowedProfiles(Set.of(allowedProfiles));
    }

    public ConfigLoader withAllowedProfiles(Set<String> allowedProfiles) {
        this.profilesResolver.withAllowedProfiles(allowedProfiles);
        return this;
    }

    public ConfigLoader withExclusiveProfiles(String... exclusiveProfiles) {
        return withExclusiveProfiles(Set.of(exclusiveProfiles));
    }

    public ConfigLoader withExclusiveProfiles(Set<String> exclusiveProfiles) {
        this.profilesResolver.withExclusiveProfiles(exclusiveProfiles);
        return this;
    }

    public ConfigLoader withExpectedProfileCount(int count) {
        this.profilesResolver.withExpectedProfileCount(count);
        return this;
    }

    public ConfigLoader withMinProfileCount(int count) {
        this.profilesResolver.withMinProfileCount(count);
        return this;
    }

    public ConfigLoader withMaxProfileCount(int count) {
        this.profilesResolver.withMaxProfileCount(count);
        return this;
    }

    public ConfigLoader withOptionalProfileConfigs(String... optionalProfileConfigs) {
        return withOptionalProfileConfigs(Set.of(optionalProfileConfigs));
    }

    public ConfigLoader withOptionalProfileConfigs(Set<String> optionalProfileConfigs) {
        withProfileConfigsRequired();
        this.optionalProfileConfigs = new HashSet<>(optionalProfileConfigs);
        return this;
    }

    public ConfigLoader withOptionalBaseConfig() {
        return withOptionalBaseConfig(true);
    }

    public ConfigLoader withOptionalBaseConfig(boolean optionalBaseConfig) {
        this.optionalBaseConfig = optionalBaseConfig;
        return this;
    }

    public ConfigLoader withProfileConfigsRequired() {
        return withProfileConfigsRequired(true);
    }

    public ConfigLoader withProfileConfigsRequired(boolean profileConfigsRequired) {
        this.profileConfigsRequired = profileConfigsRequired;
        return this;
    }

    public ConfigLoader withProfileArgName(String profileArgName) {
        this.profilesResolver.withProfileArgName(profileArgName);
        return this;
    }

    public ConfigLoader withCommonConfigName(String commonConfigName) {
        expectNonBlank(commonConfigName, "commonConfigName");
        this.commonConfigName = commonConfigName;
        return this;
    }

    public ConfigLoader withDefaultProfiles(String... defaultProfiles) {
        this.profilesResolver.withDefaultProfiles(defaultProfiles);
        return this;
    }

    public ConfigLoader withProfiles(String... profiles) {
        this.profilesResolver.withProfiles(profiles);
        return this;
    }

    public ConfigLoader withProfilesMapper(Function<List<String>, List<String>> mapper) {
        this.profilesResolver.withProfilesMapper(mapper);
        return this;
    }

    public ConfigLoader withProfilesValidator(Predicate<List<String>> predicate) {
        this.profilesResolver.withProfilesValidator(predicate);
        return this;
    }

    public ConfigLoader withoutConfigBaseName() {
        this.configBaseName = null;
        return this;
    }

    public ConfigLoader withConfigBaseName(String configBaseName) {
        expectNonBlank(configBaseName, "configBaseName");
        expect(
                !configBaseName.contains("/") && !configBaseName.contains("\\"),
                "Expected configBaseName to contain no file separators"
        );
        this.configBaseName = configBaseName;
        return this;
    }

    public ConfigLoader withoutConfigPath() {
        this.configPath = null;
        return this;
    }

    public ConfigLoader withConfigPath(Path configPath) {
        expectNonNull(configPath, "configPath");
        expect(!configPath.isAbsolute(), "Expected relative configPath");
        this.configPath = configPath;
        return this;
    }

    public ConfigLoader withConfigPath(String configPath) {
        return withConfigPath(Paths.get(configPath));
    }

    public ConfigLoader withProfiles(List<String> profiles) {
        this.profiles = Profiles.of(profiles);
        return this;
    }

    public ConfigLoader withProfiles(Profiles profiles) {
        this.profiles = profiles;
        return this;
    }

    public Config loadConfig() {
        Environment environment = loadEnvironment();
        return environment.getConfig();
    }

    public Environment loadEnvironment() {
        Config allArgsConfig = allArgsConfig();
        Profiles profiles = resolveProfiles(allArgsConfig);
        Config resolveConfig = Config.builder()
                .withValue("_profiles", profiles.getValues())
                .withValue("_system", ConfigFactory.buildFromSystemProperties())
                .withValue("_env", ConfigFactory.buildFromSystemEnvironment())
                .withValue("_args", allArgsConfig)
                .build();
        Config config = Config.builder()
                .withValues(baseConfig())
                .withValues(profileConfig(profiles.getValues()))
                .withValues(externalConfig(allArgsConfig))
                .withValues(filteredArgsConfig(allArgsConfig))
                .withResolvedExpressions(resolveConfig)
                .build();
        return new Environment(config, profiles);
    }

    private Profiles resolveProfiles(Config argsConfig) {
        return profiles != null
                ? profiles
                : profilesResolver.resolve(argsConfig);
    }

    private Config baseConfig() {
        return loadFromClasspath(null);
    }

    private Config profileConfig(List<String> profiles) {
        Config config = Config.empty();
        for (String profile : profiles) {
            if (profile != null && !profile.isBlank()) {
                Config profileConfig = loadFromClasspath(profile);
                config = config.withValues(profileConfig);
            }
        }
        return config;
    }

    private Config loadFromClasspath(String profile) {
        String configName = configBaseName != null ? configBaseName : commonConfigName;
        if (configBaseName != null && profile != null) {
            configName = configBaseName + "-" + profile;
        } else if (profile != null) {
            configName = profile;
        }
        String path = configPath != null
                ? configPath.resolve(configName).toString()
                : configName;
        return isConfigOptional(profile)
                ? ConfigFactory.loadFromClasspathOrEmpty(path)
                : ConfigFactory.loadFromClasspath(path);
    }

    private boolean isConfigOptional(String profile) {
        if (profile == null) {
            return optionalBaseConfig;
        }
        return !profileConfigsRequired ||
                (optionalProfileConfigs != null && optionalProfileConfigs.contains(profile));
    }

    private Config allArgsConfig() {
        if (args == null) {
            return Config.empty();
        }
        Map<String, Object> values = argumentsParser.parse(args);
        return Config.of(values);
    }

    private Config filteredArgsConfig(Config argsConfig) {
        return configPropArgPrefix != null
                ? argsConfig.getSubConfigOrEmpty(configPropArgPrefix)
                : Config.empty();
    }

    private Config externalConfig(Config argsConfig) {
        return externalConfigArgName != null && argsConfig.contains(externalConfigArgName)
                ? ConfigFactory.loadFromFileSystem(argsConfig.getString(externalConfigArgName))
                : Config.empty();
    }

    private String[] copy(String[] input) {
        return Arrays.copyOf(input, input.length);
    }
}
