# Configio

Java configuration management library.

## TODO
- Add tests for config.putIfMissing(path, value)
- Add tests for config.put(path, value)
- Handle config resolution: "${args.PORT?:env.PORT?:8080}"
- Add full case:
  Base config: ENV(prefix: CNF) + args(prefix: cnf) + application-${env}.yml + application.yml
  use variables from: ENV, ARGS