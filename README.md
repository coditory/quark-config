# Quark Config
[![Build](https://github.com/coditory/quark-config/actions/workflows/build.yml/badge.svg)](https://github.com/coditory/quark-config/actions/workflows/build.yml)
[![Coverage](https://codecov.io/gh/coditory/quark-config/branch/master/graph/badge.svg?token=G4B3UCSDN7)](https://codecov.io/gh/coditory/quark-config)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.coditory.quark/quark-config/badge.svg)](https://mvnrepository.com/artifact/com.coditory.quark/quark-config)

> Lightweight and single purpose java library for loading and manipulating configurations

A configuration library,
similar to the one created in [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)
of [typesafe](https://github.com/lightbend/config), that is:
- lightweight, without a burden of a framework (has exactly two dependencies gson and snakeyaml)
- loads configuration from multiple sources: arguments, classpath, file system
- supports multiple formats YAML, JSON, properties
- provides a collection of parsers for values such as `java.util.Duration`
- hides secrets when formatted to string
- provides basic expressions for references and default values
- public API annotated with `@NotNull` and `@Nullable` for better [kotlin integration](https://kotlinlang.org/docs/java-to-kotlin-nullability-guide.html#platform-types)

## Installation

Add to your `build.gradle`:

```gradle
dependencies {
    implementation "com.coditory.quark:quark-config:0.1.25"
}
```

## Usage

### Load application configuration

```java
Config config = new ConfigLoader()
        .defaultProfiles("local")
        .configPath("configs")
        .args(args)
        .loadConfig();
```

Loads configuration files:
- from classpath directory: `config`
- with config base name: `application` (it's a default value)
- with default profile: `local` (can be overridden with args `--profile=$PROFILE`)
- overrides configuration values using args `--config-value.$NAME=$VALUE`

```
// Sample config files layout:
src/main/resources/config
  application.yml
  application-local.yml
  application-prod.yml
```

Explore the API, there is much more to configure.

#### Config merging order

Application configuration sources are merged into a single config object 
according to the following order:

- base config
  - base config from classpath, by default: `application.{yml,json,properties}`
- profile config
  - profile based config from classpath, by default: `application-${profile}.{yml,json,properties}`
  - profiles are enabled in arguments: `--profile=$PROFILE` or `--profile=$PROFILE1,$PROFILE2`
  - by default profile configs are optional
- external config
  - config file from system
  - passed as an argument `--config=$PATH_TO_CONFIG`
- config values from arguments
  - all arguments passed as arguments `--config-value.<config_key>=$VALUE`

Example:
```yml
# file: application.yml
application:
  name: "best-app"
  port: ${_env.PORT ? _args.port ? 7070}

# file: application-local.yml
application:
  port: 8080

# file: application-prod.yml
application:
  port: 80
```

```java
Config config = new ConfigLoader().loadConfig();
// loads base config only
// Output: {application={port=7070, name=best-app}}

Config config = new ConfigLoader()
    .args("--profile", "prod")
    .loadConfig();
// loads config with prod profile
// Output: {application={port=80, name=best-app}}
        
Config config = new ConfigLoader()
    .aArgs("--port", "7071")
    .loadConfig();
// resolves config with expression
// Output: {application={port=7071, name=best-app}}

Config config = new ConfigLoader()
    .args("--config-value.application.port", "8081")
    .loadConfig();
// loads config with config value passed as an argument
// Output: {application={port=8081, name=best-app}}
```

For more examples see the [test cases](src/integrationTest/groovy/com/coditory/quark/config/LoadApplicationConfigSpec.groovy).

#### Config resolution variables

You can use the following expression variables to resolve an application config:
- `${_profiles.*}` - profiles arsed from args
- `${_env.*}` - all system variables from `System.getenv()`
- `${_system.*}` - all system variables from `System.getProperties()`
- `${_args.*}` - all arguments

### Creating a config

You can create a config object in multiple ways:

```java
// Create from var args
Config.of(
    "application.name", "best-app",
    "application.port", 8080
);

// Create from a map
Config.of(Map.of("application.port", 8080));

// Create empty config and add value
Config.builder()
    .put("application.port", 8080)
    .build();

// Build own config with customizations
Config.builder()
    .put("application.name", "best-app")
    .put("application.port", 8080)
    .setSecretHidingValueMapper(customSecretHider)
    .setValueParser(customParser)
    .build();

// Create config from strings, files or system properties
ConfigFactory.buildFromSystemProperties();
ConfigFactory.buildFromSystemEnvironment();
ConfigFactory.buildFromArgs(args);
ConfigFactory.loadFromClasspath(path);
ConfigFactory.loadFromFileSystem(path);
ConfigFactory.parseJson(json);
ConfigFactory.parseYaml(yaml);
ConfigFactory.parseProperties(properties);

// Load config from classpath files with some validation and profiles passed in args
// (typical setup for web application, that is deployable to dev, test and prod environemnts)
new ConfigLoader()
    .addArgsMapping(["--prod"], ["--profile", "prod"])
    .addArgsMapping(["--test"], ["--profile", "test"])
    .addArgsMapping(["--dev"], ["--profile", "dev"])
    .addArgsAlias("p", "profile")
    .minProfileCount(1)
    .exclusiveProfiles(mainProfiles)
    .firstIfNoneMatch(mainProfiles)
    .configPath("config")
    .args(args)
    .loadConfig();
```

### Resolving a config value
```java
// throws if config value is not defined
config.getInteger("application.port");

// returns a default value if config value is not defined
config.getInteger("application.port", 8080);

// returns null if config value is not defined
config.getIntegerOrNull("application.port");

// returns an Optional from config value
config.getIntegerAsOptional("application.port");
```

Quark Config provides a lot of parsers
so String values may be retrieved as a non String objects:
```java
Config.of(
    "port", "8080",
    "timeout", "1s",
    "locale", "pl-PL",
    "currency", "PLN",
    "timestamp", "2007-12-03T10:15:30.00Z"
)
assert config.getInteger("port") == 8080
assert config.getDuration("timeout") == Duration.parse("PT1S")
assert config.getLocale("locale") == new Locale("pl", "PL")
assert config.getCurrency("currency") == Currency.getInstance("PLN")
assert config.getInstant("timestamp") == Instant.parse("2007-12-03T10:15:30.00Z")
```

Resolving a sub-config:
```java
Config applicationConfig = config.getSubConfig("application");
applicationConfig.getInteger("port");
```

### Merging configs
Two config objects can be merged:

```java
Config config = Config.of(
    "a", "A",
    "b", "B"
);
Config other = Config.of(
   "b", "X",
   "c", "Y"
);

config.withDefaults(other);
// {a=A, b=B, c=Y}

config.withValues(other);
// {a=A, b=X, c=Y}
```

### Config expressions

Config values can use some basic expressions:
```java
// Config with references to other value
Config.builder()
    .put("a", "${b}")
    .put("b", "B")
    .resolveExpressions()
    .build();
// {a=B, b=B}

// Config with a fallback value
Config.builder()
    .withValue("a", "${b ? X}")
    .resolveExpressions()
    .build();
// {a=X}

// Config with a double fallback value
Config.builder()
    .put("a", "${b ? c ? X}")
    .resolveExpressions()
    .build();
// {a=X}

// Config with expression values
Config.builder()
    .put("a", "${b ? c ? X}")
    .resolveExpressions(Config.of(c, "C"))
    .build();
// {a=C}
```

### Load config from file

The config can be loaded from:
- classpath file
- file from the file system

You don't need to specify an extension of the configuration file.
Quark Config will automatically look for: `*.yml`, `*.yaml`, `*.json`, `*.properties`

```java
ConfigFactory.loadFromClasspath("custom-config");
ConfigFactory.loadFromFileSystem("custom-config");
```

### Parsing and formatting a config

Quark Config supports the following formats:
- yaml
- json
- properties

```java
// parsing a config
ConfigFactory.parseYaml(yaml);
ConfigFactory.parseJson(json);
ConfigFactory.parseProperties(properties);

// formatting a config
ConfigFormatter.toYaml(config);
ConfigFormatter.toJson(config);
ConfigFormatter.toProperties(config);

// formatting a config with exposed secrets
ConfigFormatter.toYamlWithExposedSecrets(config);
ConfigFormatter.toJsonWithExposedSecrets(config);
ConfigFormatter.toPropertiesWithExposedSecrets(config);

// config.toString() method uses output of a Map with hidden secrets
config.toString();
```
