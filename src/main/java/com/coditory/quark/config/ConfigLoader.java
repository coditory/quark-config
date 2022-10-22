package com.coditory.quark.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
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

    public ConfigLoader args(String[] args) {
        expectNonNull(args, "args");
        this.args = copy(args);
        return this;
    }

    public ConfigLoader args(List<String> args) {
        expectNonNull(args, "args");
        this.args = args.toArray(new String[0]);
        return this;
    }

    public ConfigLoader argsMapping(Map<String[], String[]> mapping) {
        argumentsParser.withMapping(mapping);
        return this;
    }

    public ConfigLoader addArgsMapping(List<String> args, List<String> mapping) {
        argumentsParser.addMapping(args.toArray(new String[0]), mapping.toArray(new String[0]));
        return this;
    }

    public ConfigLoader addArgsMapping(String[] args, String[] mapping) {
        argumentsParser.addMapping(args, mapping);
        return this;
    }

    public ConfigLoader argsAliases(Map<String, String> aliases) {
        argumentsParser.withAliases(aliases);
        return this;
    }

    public ConfigLoader addArgsAlias(String arg, String alias) {
        argumentsParser.addAlias(arg, alias);
        return this;
    }

    public ConfigLoader noConfigPropArgPrefix() {
        this.configPropArgPrefix = null;
        return this;
    }

    public ConfigLoader configPropArgPrefix(String configPropArgPrefix) {
        expectNonBlank(configPropArgPrefix, "configPropArgPrefix");
        expect(!Objects.equals(configPropArgPrefix, externalConfigArgName), "Expected configPropArgPrefix != externalConfigArgName");
        expect(!configPropArgPrefix.contains("."), "Expected configPropArgPrefix to contain no dots");
        this.configPropArgPrefix = configPropArgPrefix;
        return this;
    }

    public ConfigLoader noExternalConfigArgName() {
        this.externalConfigArgName = null;
        return this;
    }

    public ConfigLoader externalConfigArgName(String externalConfigArgName) {
        expectNonBlank(externalConfigArgName, "externalConfigArgName");
        expect(!Objects.equals(configPropArgPrefix, externalConfigArgName), "Expected configPropArgPrefix != externalConfigArgName");
        expect(!externalConfigArgName.contains("."), "Expected externalConfigArgName to contain no dots");
        this.externalConfigArgName = externalConfigArgName;
        return this;
    }

    public ConfigLoader allowedProfiles(String... allowedProfiles) {
        return allowedProfiles(Set.of(allowedProfiles));
    }

    public ConfigLoader allowedProfiles(Collection<String> allowedProfiles) {
        this.profilesResolver.allowedProfiles(allowedProfiles);
        return this;
    }

    public ConfigLoader firstIfNoneMatch(List<String> firstIfNoneMatch) {
        this.profilesResolver.firstIfNoneMatch(firstIfNoneMatch);
        return this;
    }

    public ConfigLoader exclusiveProfiles(String... exclusiveProfiles) {
        return exclusiveProfiles(Set.of(exclusiveProfiles));
    }

    public ConfigLoader exclusiveProfiles(Collection<String> exclusiveProfiles) {
        this.profilesResolver.exclusiveProfiles(exclusiveProfiles);
        return this;
    }

    public ConfigLoader expectedProfileCount(int count) {
        this.profilesResolver.expectedProfileCount(count);
        return this;
    }

    public ConfigLoader minProfileCount(int count) {
        this.profilesResolver.minProfileCount(count);
        return this;
    }

    public ConfigLoader maxProfileCount(int count) {
        this.profilesResolver.maxProfileCount(count);
        return this;
    }

    public ConfigLoader optionalProfileConfigs(String... optionalProfileConfigs) {
        return optionalProfileConfigs(Set.of(optionalProfileConfigs));
    }

    public ConfigLoader optionalProfileConfigs(Set<String> optionalProfileConfigs) {
        profileConfigsRequired();
        this.optionalProfileConfigs = new HashSet<>(optionalProfileConfigs);
        return this;
    }

    public ConfigLoader optionalBaseConfig() {
        return optionalBaseConfig(true);
    }

    public ConfigLoader optionalBaseConfig(boolean optionalBaseConfig) {
        this.optionalBaseConfig = optionalBaseConfig;
        return this;
    }

    public ConfigLoader profileConfigsRequired() {
        return profileConfigsRequired(true);
    }

    public ConfigLoader profileConfigsRequired(boolean profileConfigsRequired) {
        this.profileConfigsRequired = profileConfigsRequired;
        return this;
    }

    public ConfigLoader profileArgName(String profileArgName) {
        this.profilesResolver.profileArgName(profileArgName);
        return this;
    }

    public ConfigLoader commonConfigName(String commonConfigName) {
        expectNonBlank(commonConfigName, "commonConfigName");
        this.commonConfigName = commonConfigName;
        return this;
    }

    public ConfigLoader defaultProfiles(String... defaultProfiles) {
        this.profilesResolver.defaultProfiles(defaultProfiles);
        return this;
    }

    public ConfigLoader profilesMapper(Function<List<String>, List<String>> mapper) {
        this.profilesResolver.profilesMapper(mapper);
        return this;
    }

    public ConfigLoader profilesValidator(Predicate<List<String>> predicate) {
        this.profilesResolver.profilesValidator(predicate);
        return this;
    }

    public ConfigLoader noConfigBaseName() {
        this.configBaseName = null;
        return this;
    }

    public ConfigLoader configBaseName(String configBaseName) {
        expectNonBlank(configBaseName, "configBaseName");
        expect(
                !configBaseName.contains("/") && !configBaseName.contains("\\"),
                "Expected configBaseName to contain no file separators"
        );
        this.configBaseName = configBaseName;
        return this;
    }

    public ConfigLoader noConfigPath() {
        this.configPath = null;
        return this;
    }

    public ConfigLoader configPath(Path configPath) {
        expectNonNull(configPath, "configPath");
        expect(!configPath.isAbsolute(), "Expected relative configPath");
        this.configPath = configPath;
        return this;
    }

    public ConfigLoader configPath(String configPath) {
        return configPath(Paths.get(configPath));
    }

    public ConfigLoader profiles(String... profiles) {
        this.profilesResolver.profiles(profiles);
        return this;
    }

    public ConfigLoader profiles(List<String> profiles) {
        this.profilesResolver.profiles(profiles);
        return this;
    }

    public ConfigLoader profiles(Profiles profiles) {
        this.profilesResolver.profiles(profiles);
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
                .put("_profiles", profiles.getValues())
                .put("_system", ConfigFactory.buildFromSystemProperties())
                .put("_env", ConfigFactory.buildFromSystemEnvironment())
                .put("_args", allArgsConfig)
                .build();
        Config config = Config.builder()
                .putAll(baseConfig())
                .putAll(profileConfig(profiles.getValues()))
                .putAll(externalConfig(allArgsConfig))
                .putAll(filteredArgsConfig(allArgsConfig))
                .resolveExpressions(resolveConfig)
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
        ConfigBuilder configBuilder = Config.builder();
        for (String profile : profiles) {
            if (profile != null && !profile.isBlank()) {
                Config profileConfig = loadFromClasspath(profile);
                configBuilder = configBuilder.putAll(profileConfig);
            }
        }
        return configBuilder.build();
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
