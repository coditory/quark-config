# Quark Config
[![Build](https://github.com/coditory/quark-config/actions/workflows/build.yml/badge.svg)](https://github.com/coditory/quark-config/actions/workflows/build.yml)
[![Coverage Status](https://coveralls.io/repos/github/coditory/quark-config/badge.svg)](https://coveralls.io/github/coditory/quark-config)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.coditory.quark/quark-config/badge.svg)](https://mvnrepository.com/artifact/com.coditory.quark/quark-config)

> Lightweight and single purpose java library for loading and manipulating configurations

A configuration library,
similar to the one created in [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)
of [typesafe](https://github.com/lightbend/config), that is:
- lightweight, without a burden of a framework (has exactly two dependencies gson and yamlsnake)
- loads configuration from multiple sources: arguments, classpath, file system
- supports multiple formats YAML, JSON, properties
- provides a collection of parsers for values such as `java.util.Duration`
- hides secrets when formatted to string
- provides basic expressions for references and default values

## Installation

Add to your `build.gradle`:

```gradle
dependencies {
    implementation "com.coditory.quark:quark-config:0.1.8"
}
```

## Usage

### Load application configuration

```java
Config config = ConfigFactory.configLoader()
        .withDefaultProfiles("local")
        .withConfigPath("configs")
        .withArgs(args)
        .load()
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
  port: ${env.PORT ? args.port ? 7070}

# file: application-local.yml
application:
  port: 8080

# file: application-prod.yml
application:
  port: 80
```

```java
// load base config only
ConfigFactory.loadConfig();
// Output: {application={port=7070, name=best-app}}

// load config with prod profile
ConfigFactory.loadApplicationConfig("--profile", "prod");
// Output: {application={port=80, name=best-app}}

// resolve config with expression
ConfigFactory.loadApplicationConfig("--port", "7071");
// Output: {application={port=7071, name=best-app}}

// load config with config value passed as argument
ConfigFactory.loadApplicationConfig("--config-value.application.port", "8081");
// Output: {application={port=8081, name=best-app}}
```
For more examples see the [test cases](src/integration/groovy/com/coditory/quark/config/LoadApplicationConfigSpec.groovy).

#### Config resolution variables

You can use the following expression variables to resolve an application config:
- `${profiles.*}` - profiles arsed from args
- `${env.*}` - all system variables from `System.getenv()`
- `${system.*}` - all system variables from `System.getProperties()`
- `${args.*}` - all arguments

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
Config.empty()
    .withValue("application.port", 8080);

// Build own config with customizations
Config.builder()
    .withValue("application.name", "best-app")
    .withValue("application.port", 8080)
    .withSecretHidingValueMapper(customSecretHider)
    .withValueParser(customParser)
    .build();
```

The config object is immutable. Every time you add a value to a config, a new config instance is returned:
```java
Config c1 = Config.of("application.port", 8080);
Config c2 = c1.withValue("application.name", "best-app");

assert c1 != c2
assert !c1.equals(c2)
```

### Resolving a config value
```java
// throws if config value is not defined
config.getInteger("application.port")

// returns a default value if config value is not defined
config.getInteger("application.port", 8080)

// returns null if config value is not defined
config.getIntegerOrNull("application.port")

// returns an Optional from config value
config.getIntegerAsOptional("application.port")
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
Config applicationConfig = config.getSubConfig("application")
applicationConfig.getInteger("port")
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
    .withValue("a", "${b}")
    .withValue("b", "B")
    .build()
    .resolveExpressions();
// {a=B, b=B}

// Config with a fallback value
Config.builder()
    .withValue("a", "${b ? X}")
    .build()
    .resolveExpressions();
// {a=X}

// Config with a double fallback value
Config
    .of("a", "${b ? c ? X}")
    .resolveExpressions();
// {a=X}

// Config with expression values
Config
    .of("a", "${b ? c ? X}")
    .resolveExpressions(Config.of(c, "C"));
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
config.toString()
```
