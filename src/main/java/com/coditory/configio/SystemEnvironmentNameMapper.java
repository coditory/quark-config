package com.coditory.configio;

import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;

class SystemEnvironmentNameMapper {
    static String mapSystemEnvironmentName(String sysName) {
        StringBuilder builder = new StringBuilder();
        boolean underscore = false;
        for (int i = 0; i < sysName.length(); ++i) {
            char c = sysName.charAt(i);
            if (c == '_') {
                underscore = true;
            } else if (underscore) {
                builder.append(toUpperCase(c));
            } else {
                builder.append(toLowerCase(c));
            }
        }
        return builder.toString();
    }
}
