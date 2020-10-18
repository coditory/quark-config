package com.coditory.configio;

import com.coditory.configio.api.ConfigValueConversionException;

import java.util.Arrays;
import java.util.Locale;

class LocaleParser {
    static Locale parseLocale(String value) {
        Locale locale = Locale.forLanguageTag(value.replace("_", "-"));
        boolean isAvailable = Arrays.asList(Locale.getAvailableLocales())
                .contains(locale);
        if (!isAvailable) {
            throw new ConfigValueConversionException("Got not available locale: " + locale);
        }
        return locale;
    }
}
