package ro.bogdananton.testNameGenerator.utils;

import org.apache.commons.lang.WordUtils;

public class StringUtils
{
    /**
     * @todo Treat non-alpha char filtering.
     * @param init string to be converted
     * @return
     */
    public static String convertToJoinedCamelCase(final String init)
    {
        final StringBuilder ret = new StringBuilder(init.length());
        for (final String word : init.split(" ")) {
            if (word.length() > 1) {
                ret.append(WordUtils.capitalize(word));
            } else {
                ret.append(word.toUpperCase());
            }
        }
        return ret.toString();
    }
}
