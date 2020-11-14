package com.coditory.quark.config;

import java.util.ArrayList;
import java.util.List;

class QuotedSpliterator {
    static List<String> splitBy(String expression, String separator) {
        List<String> result = new ArrayList<>();
        boolean escaped = false;
        Character quote = null;
        boolean preserveQuote = false;
        StringBuilder chunk = new StringBuilder();
        char[] chars = expression.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            if (escaped) {
                if ('\'' == c || '"' == c) {
                    chunk.append(c);
                } else {
                    throw new RuntimeException("Could not escape: \"" + c + "\" in expression: \"" + expression + "\"");
                }
                escaped = false;
            } else if ('\\' == c) {
                escaped = true;
            } else if ('\'' == c || '"' == c) {
                if (quote == null) {
                    quote = c;
                    preserveQuote = chunk.length() != 0;
                    if (preserveQuote) {
                        chunk.append(c);
                    }
                } else if (quote == c) {
                    quote = null;
                    if (preserveQuote) {
                        chunk.append(c);
                    } else {
                        result.add(chunk.toString());
                        chunk = new StringBuilder();
                    }
                }
            } else if (quote != null) {
                chunk.append(c);
            } else if (isSeparator(separator, chars, i)) {
                if (chunk.length() > 0) {
                    result.add(chunk.toString().trim());
                    chunk = new StringBuilder();
                    i += separator.length() - 1;
                }
            } else if (c != ' ' || chunk.length() > 0) {
                chunk.append(c);
            }
        }
        if (chunk.length() > 0) {
            result.add(chunk.toString().trim());
        }
        return result;
    }

    private static boolean isSeparator(String separator, char[] c, int index) {
        if (c.length < separator.length() + index) {
            return false;
        }
        boolean match = true;
        int i = 0;
        while (i < separator.length() && match) {
            match = separator.charAt(i) == c[index + i];
            ++i;
        }
        return match && i == separator.length();
    }
}
