# Configio

Java configuration management library.

## TODO
- Handle config resolution: "${args.PORT?:env.PORT?:8080}"
- Add full case:
  Base config: ENV(prefix: CNF) + args(prefix: cnf) + application-${env}.yml + application.yml
  use variables from: ENV, ARGS
- See: https://github.com/pmendelski/kotlin-ktor-sandbox/blob/master/src/main/kotlin/com/coditory/sandbox/infra/config/ConfigLoader.kt
- Add config loading method load.
  Config.loadAndParse("application-${profile}")
  Config.loadAndParse("/tmp/application")