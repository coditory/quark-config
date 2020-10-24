# Configio

Java configuration library.

## TODO
- Add: ConfigLoader, ConfigParser
- Unify exceptions
- Unify config api
- Add full case:
  Base config: ENV(prefix: CNF) + args(prefix: cnf) + application-${env}.yml + application.yml
  use variables from: ENV, ARGS
  See: https://github.com/pmendelski/kotlin-ktor-sandbox/blob/master/src/main/kotlin/com/coditory/sandbox/infra/config/ConfigLoader.kt