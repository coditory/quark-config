# Configio

Java configuration management library.

## TODO
- Handle config resolution: "${args.PORT?:env.PORT?:8080}"
- Add full case:
  Base config: ENV(prefix: CNF) + args(prefix: cnf) + application-${env}.yml + application.yml
  use variables from: ENV, ARGS