# Configio

Java configuration management library.

## TODO
- Fix failing tests
- Add config.merge()
- Add config.addDefaults(config2)
- Add config.addIfMissing(path, value)
- Add config.addOrThrow(path, value)
- Add config.addOrReplace(path, value)
- Handle config resolution: "${args.PORT?:env.PORT?:8080}"
- Add full case:
  Base config: ENV(prefix: CNF) + args(prefix: cnf) + application-${env}.yml + application.yml
  use variables from: ENV, ARGS