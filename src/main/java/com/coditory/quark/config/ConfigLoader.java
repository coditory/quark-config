package com.coditory.quark.config;

import org.jetbrains.annotations.NotNull;

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
    private final ConfigProfilesResolver profilesResolver = new ConfigProfilesResolver();
    private ConfigProfiles profiles = null;
    private String[] args = null;
    private String externalConfigArgName = "config";
    private String configPropArgPrefix = "config-prop";
    private Path configPath = null;
    private String commonConfigName = "base";
    private String configBaseName = "application";
    private boolean optionalBaseConfig = false;
    private Set<String> optionalProfileConfigs = null;
    private boolean profileConfigsRequired = false;

    @NotNull
    public ConfigLoader args(@NotNull String[] args) {
        expectNonNull(args, "args");
        this.args = copy(args);
        return this;
    }

    @NotNull
    public ConfigLoader args(@NotNull List<String> args) {
        expectNonNull(args, "args");
        this.args = args.toArray(new String[0]);
        return this;
    }

    @NotNull
    public ConfigLoader argsMapping(@NotNull Map<String[], String[]> mapping) {
        argumentsParser.withMapping(mapping);
        return this;
    }

    @NotNull
    public ConfigLoader addArgsMapping(@NotNull List<String> args, @NotNull List<String> mapping) {
        argumentsParser.addMapping(args.toArray(new String[0]), mapping.toArray(new String[0]));
        return this;
    }

    @NotNull
    public ConfigLoader addArgsMapping(@NotNull String[] args, @NotNull String[] mapping) {
        argumentsParser.addMapping(args, mapping);
        return this;
    }

    @NotNull
    public ConfigLoader argsAliases(@NotNull Map<String, String> aliases) {
        argumentsParser.withAliases(aliases);
        return this;
    }

    @NotNull
    public ConfigLoader addArgsAlias(@NotNull String arg, @NotNull String alias) {
        argumentsParser.addAlias(arg, alias);
        return this;
    }

    @NotNull
    public ConfigLoader noConfigPropArgPrefix() {
        this.configPropArgPrefix = null;
        return this;
    }

    @NotNull
    public ConfigLoader configPropArgPrefix(@NotNull String configPropArgPrefix) {
        expectNonBlank(configPropArgPrefix, "configPropArgPrefix");
        expect(!Objects.equals(configPropArgPrefix, externalConfigArgName), "Expected configPropArgPrefix != externalConfigArgName");
        expect(!configPropArgPrefix.contains("."), "Expected configPropArgPrefix to contain no dots");
        this.configPropArgPrefix = configPropArgPrefix;
        return this;
    }

    @NotNull
    public ConfigLoader noExternalConfigArgName() {
        this.externalConfigArgName = null;
        return this;
    }

    @NotNull
    public ConfigLoader externalConfigArgName(@NotNull String externalConfigArgName) {
        expectNonBlank(externalConfigArgName, "externalConfigArgName");
        expect(!Objects.equals(configPropArgPrefix, externalConfigArgName), "Expected configPropArgPrefix != externalConfigArgName");
        expect(!externalConfigArgName.contains("."), "Expected externalConfigArgName to contain no dots");
        this.externalConfigArgName = externalConfigArgName;
        return this;
    }

    @NotNull
    public ConfigLoader allowedProfiles(@NotNull String... allowedProfiles) {
        return allowedProfiles(Set.of(allowedProfiles));
    }

    @NotNull
    public ConfigLoader allowedProfiles(@NotNull Collection<String> allowedProfiles) {
        this.profilesResolver.allowedProfiles(allowedProfiles);
        return this;
    }

    @NotNull
    public ConfigLoader firstIfNoneMatch(@NotNull List<String> firstIfNoneMatch) {
        this.profilesResolver.firstIfNoneMatch(firstIfNoneMatch);
        return this;
    }

    @NotNull
    public ConfigLoader exclusiveProfiles(@NotNull String... exclusiveProfiles) {
        return exclusiveProfiles(Set.of(exclusiveProfiles));
    }

    @NotNull
    public ConfigLoader exclusiveProfiles(Collection<String> exclusiveProfiles) {
        this.profilesResolver.exclusiveProfiles(exclusiveProfiles);
        return this;
    }

    @NotNull
    public ConfigLoader expectedProfileCount(int count) {
        this.profilesResolver.expectedProfileCount(count);
        return this;
    }

    @NotNull
    public ConfigLoader minProfileCount(int count) {
        this.profilesResolver.minProfileCount(count);
        return this;
    }

    @NotNull
    public ConfigLoader maxProfileCount(int count) {
        this.profilesResolver.maxProfileCount(count);
        return this;
    }

    @NotNull
    public ConfigLoader optionalProfileConfigs(@NotNull String... optionalProfileConfigs) {
        return optionalProfileConfigs(Set.of(optionalProfileConfigs));
    }

    @NotNull
    public ConfigLoader optionalProfileConfigs(@NotNull Set<String> optionalProfileConfigs) {
        profileConfigsRequired();
        this.optionalProfileConfigs = new HashSet<>(optionalProfileConfigs);
        return this;
    }

    @NotNull
    public ConfigLoader optionalBaseConfig() {
        return optionalBaseConfig(true);
    }

    @NotNull
    public ConfigLoader optionalBaseConfig(boolean optionalBaseConfig) {
        this.optionalBaseConfig = optionalBaseConfig;
        return this;
    }

    @NotNull
    public ConfigLoader profileConfigsRequired() {
        return profileConfigsRequired(true);
    }

    @NotNull
    public ConfigLoader profileConfigsRequired(boolean profileConfigsRequired) {
        this.profileConfigsRequired = profileConfigsRequired;
        return this;
    }

    @NotNull
    public ConfigLoader profileArgName(String profileArgName) {
        this.profilesResolver.profileArgName(profileArgName);
        return this;
    }

    @NotNull
    public ConfigLoader commonConfigName(String commonConfigName) {
        expectNonBlank(commonConfigName, "commonConfigName");
        this.commonConfigName = commonConfigName;
        return this;
    }

    @NotNull
    public ConfigLoader defaultProfiles(String... defaultProfiles) {
        this.profilesResolver.defaultProfiles(defaultProfiles);
        return this;
    }

    @NotNull
    public ConfigLoader profilesMapper(Function<List<String>, List<String>> mapper) {
        this.profilesResolver.profilesMapper(mapper);
        return this;
    }

    @NotNull
    public ConfigLoader profilesValidator(Predicate<List<String>> predicate) {
        this.profilesResolver.profilesValidator(predicate);
        return this;
    }

    @NotNull
    public ConfigLoader noConfigBaseName() {
        this.configBaseName = null;
        return this;
    }

    @NotNull
    public ConfigLoader configBaseName(@NotNull String configBaseName) {
        expectNonBlank(configBaseName, "configBaseName");
        expect(
                !configBaseName.contains("/") && !configBaseName.contains("\\"),
                "Expected configBaseName to contain no file separators"
        );
        this.configBaseName = configBaseName;
        return this;
    }

    @NotNull
    public ConfigLoader noConfigPath() {
        this.configPath = null;
        return this;
    }

    @NotNull
    public ConfigLoader configPath(@NotNull Path configPath) {
        expectNonNull(configPath, "configPath");
        expect(!configPath.isAbsolute(), "Expected relative configPath");
        this.configPath = configPath;
        return this;
    }

    @NotNull
    public ConfigLoader configPath(@NotNull String configPath) {
        return configPath(Paths.get(configPath));
    }

    @NotNull
    public ConfigLoader profiles(@NotNull String... profiles) {
        this.profilesResolver.profiles(profiles);
        return this;
    }

    @NotNull
    public ConfigLoader profiles(@NotNull List<String> profiles) {
        this.profilesResolver.profiles(profiles);
        return this;
    }

    @NotNull
    public ConfigLoader profiles(@NotNull ConfigProfiles profiles) {
        this.profilesResolver.profiles(profiles);
        return this;
    }

    @NotNull
    public Config loadConfig() {
        Environment environment = loadEnvironment();
        return environment.config();
    }

    @NotNull
    public Environment loadEnvironment() {
        Config allArgsConfig = allArgsConfig();
        ConfigProfiles profiles = resolveProfiles(allArgsConfig);
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

    private ConfigProfiles resolveProfiles(Config argsConfig) {
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
