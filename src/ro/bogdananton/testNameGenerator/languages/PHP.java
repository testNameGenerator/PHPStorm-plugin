package ro.bogdananton.testNameGenerator.languages;

import org.jetbrains.annotations.NotNull;
import ro.bogdananton.testNameGenerator.settings.EditorSettings;

import java.util.regex.Pattern;

public class PHP extends Generic {
    public static final String REGEX_PATTERN_METHOD_TEST = "public([\\s]*)function([\\s]*)test([\\w]*)\\(";

    @NotNull
    public static String getNewTestMethodContentString(String originalText) {
        String tab = EditorSettings.getTabChar();
        String lineDelimiter = EditorSettings.getLineDelimiter();
        String preparedMethodName = getPreparedMethodName(originalText);

        String commentText = lineDelimiter + tab + "/**" + lineDelimiter +
                tab + " * " + originalText.replace("*/", "* /") + lineDelimiter +
                tab + " */" + lineDelimiter;

        String methodText = "public function test" + preparedMethodName + "()" + lineDelimiter +
                tab + "{" + lineDelimiter +
                tab + tab + "$this->markTestIncomplete('implement me...');" + lineDelimiter +
                tab + "}" + lineDelimiter;

        return commentText + tab + methodText;
    }

    public static Pattern getTestMethodPattern() {
        return Pattern.compile(REGEX_PATTERN_METHOD_TEST, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }
}
