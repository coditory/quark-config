package com.coditory.quark.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.coditory.quark.config.Preconditions.expect;
import static com.coditory.quark.config.Preconditions.expectNonBlank;
import static com.coditory.quark.config.Preconditions.expectNonNull;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class ConfigLoader {
    private String[] args = null;
    private String externalConfigArgName = "config";
    private String configPropArgPrefix = "config-prop";
    private Map<String, String> argsAliases = new LinkedHashMap<>(Map.of("p", "profile"));
    private Map<String[], String[]> argsMapping = new LinkedHashMap<>();
    private String profileArgName = "profile";
    private List<String> defaultProfiles = new ArrayList<>();
    private Path configPath = null;
    private String commonConfigName = "base";
    private String configBaseName = "application";
    private boolean optionalBaseConfig = false;
    private Set<String> allowedProfiles = null;
    private Set<String> optionalProfileConfigs = null;
    private boolean optionalAllProfileConfigs = false;

    ConfigLoader() {
        // deliberately empty
    }

    public ConfigLoader withArgs(String[] args) {
        expectNonNull(args, "args");
        this.args = copy(args);
        return this;
    }

    public ConfigLoader withArgsMapping(Map<String[], String[]> argsMapping) {
        expectNonNull(argsMapping, "argsMapping");
        this.argsMapping = copy(argsMapping);
        return this;
    }

    public ConfigLoader addArgsMapping(String[] args, String[] mapping) {
        expectNonNull(args, "args");
        expectNonNull(mapping, "mapping");
        this.argsMapping.put(copy(args), copy(mapping));
        return this;
    }

    public ConfigLoader withArgsAliases(Map<String, String> argsAliases) {
        expectNonNull(argsAliases, "argsAliases");
        this.argsAliases = new LinkedHashMap<>(argsAliases);
        return this;
    }

    public ConfigLoader addArgsAlias(String arg, String alias) {
        expectNonBlank(arg, "arg");
        expectNonBlank(alias, "alias");
        this.argsAliases.put(arg, alias);
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
        this.allowedProfiles = new HashSet<>(allowedProfiles);
        return this;
    }

    public ConfigLoader withOptionalProfileConfigs(String... optionalProfileConfigs) {
        return withOptionalProfileConfigs(Set.of(optionalProfileConfigs));
    }

    public ConfigLoader withOptionalProfileConfigs(Set<String> optionalProfileConfigs) {
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

    public ConfigLoader withOptionalAllProfileConfigs() {
        return withOptionalAllProfileConfigs(true);
    }

    public ConfigLoader withOptionalAllProfileConfigs(boolean optionalAllProfileConfigs) {
        this.optionalAllProfileConfigs = optionalAllProfileConfigs;
        return this;
    }

    public ConfigLoader withProfileArgName(String profileArgName) {
        this.profileArgName = expectNonBlank(profileArgName, "profileArgName");
        return this;
    }

    public ConfigLoader withCommonConfigName(String commonConfigName) {
        expectNonBlank(commonConfigName, "commonConfigName");
        this.commonConfigName = commonConfigName;
        return this;
    }

    public ConfigLoader withDefaultProfiles(String... defaultProfiles) {
        expectNonNull(defaultProfiles, "defaultProfiles");
        List<String> newProfiles = stream(defaultProfiles)
                .flatMap(s -> stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        this.defaultProfiles = new ArrayList<>(newProfiles);
        return this;
    }

    public ConfigLoader addDefaultProfile(String defaultProfile) {
        expectNonBlank(defaultProfile, "defaultProfile");
        List<String> newProfiles = stream(defaultProfile.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        this.defaultProfiles.addAll(newProfiles);
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

    public Config load() {
        Config allArgsConfig = allArgsConfig();
        List<String> profiles = profiles(allArgsConfig);
        Config resolveConfig = Config.builder()
                .withValue("profiles", profiles)
                .withValue("system", ConfigFactory.buildFromSystemProperties())
                .withValue("env", ConfigFactory.buildFromSystemEnvironment())
                .withValue("args", allArgsConfig)
                .build();
        return baseConfig()
                .withValues(profileConfig(profiles))
                .withValues(externalConfig(allArgsConfig))
                .withValues(filteredArgsConfig(allArgsConfig))
                .resolveExpressions(resolveConfig);
    }

    private Config baseConfig() {
        return loadFromClasspath(null);
    }

    private List<String> profiles(Config argsConfig) {
        Stream<String> profilesFromArgument = profileArgName != null && argsConfig.contains(profileArgName)
                ? Stream.of(argsConfig.getString(profileArgName))
                : Stream.of();
        List<String> profiles = profilesFromArgument
                .flatMap(s -> stream(s.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(toList());
        profiles = profiles.isEmpty() && !defaultProfiles.isEmpty()
                ? defaultProfiles
                : profiles;
        if (allowedProfiles != null) {
            Set<String> invalidProfiles = profiles.stream()
                    .filter(p -> !allowedProfiles.contains(p))
                    .collect(toSet());
            expect(invalidProfiles.isEmpty(), "Invalid profiles: " + invalidProfiles);
        }
        return profiles;
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
        return optionalAllProfileConfigs ||
                (optionalProfileConfigs != null && optionalProfileConfigs.contains(profile));
    }

    private Config allArgsConfig() {
        return args != null
                ? ConfigFactory.buildFromArgs(args, argsAliases, argsMapping)
                : Config.empty();
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

    private LinkedHashMap<String[], String[]> copy(Map<String[], String[]> input) {
        LinkedHashMap<String[], String[]> result = new LinkedHashMap<>();
        for (Map.Entry<String[], String[]> entry : input.entrySet()) {
            result.put(copy(entry.getKey()), copy(entry.getValue()));
        }
        return result;
    }
}
