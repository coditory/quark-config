#!/usr/bin/env bash
set -e
set -u
set -o pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
PAD="    "

function generateGetters() {
  local -r types=(
    "String" "Object" "Boolean" "Short" "Byte" "Integer" "Long" "Float" "Double"
    "BigDecimal" "Instant" "ZonedDateTime" "Duration" "Locale" "Currency"
  )
  local -r javaFile="$DIR/../java/com/coditory/configio/ConfigValueExtractor.java"
  local javaCode="$(sed '/\/\/ GETTERS/Q' "$javaFile")"
  javaCode="${javaCode}\n\n$PAD// GETTERS"
  for type in "${types[@]}"; do
    javaCode="${javaCode}\n"
    javaCode="${javaCode}\n${PAD}// ${type} API"
    javaCode="${javaCode}\n"
    javaCode="${javaCode}\n${PAD}default Optional<${type}> getOptional${type}(String path) {"
    javaCode="${javaCode}\n${PAD}${PAD}return getOptional(${type}.class, path);"
    javaCode="${javaCode}\n${PAD}}"
    javaCode="${javaCode}\n"
    javaCode="${javaCode}\n${PAD}default ${type} get${type}(String path) {"
    javaCode="${javaCode}\n${PAD}${PAD}return getOptional${type}(path)"
    javaCode="${javaCode}\n${PAD}${PAD}${PAD}${PAD}.orElseThrow(() -> missingConfigValueForPath(path));"
    javaCode="${javaCode}\n${PAD}}"
    javaCode="${javaCode}\n"
    javaCode="${javaCode}\n${PAD}default ${type} get${type}OrNull(String path) {"
    javaCode="${javaCode}\n${PAD}${PAD}return get${type}OrDefault(path, null);"
    javaCode="${javaCode}\n${PAD}}"
    javaCode="${javaCode}\n"
    javaCode="${javaCode}\n${PAD}default ${type} get${type}(String path, ${type} defaultValue) {"
    javaCode="${javaCode}\n${PAD}${PAD}return getOptional${type}(path).orElse(defaultValue);"
    javaCode="${javaCode}\n${PAD}}"
    javaCode="${javaCode}\n"
  done
  javaCode="${javaCode}\n}\n"
  echo -e "$javaCode" >"$javaFile"
}

generateGetters
