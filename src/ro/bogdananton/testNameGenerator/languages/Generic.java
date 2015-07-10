package ro.bogdananton.testNameGenerator.languages;

import ro.bogdananton.testNameGenerator.utils.StringUtils;

abstract public class Generic {

    public static String getPreparedMethodName(String originalText) {
        String methodNameText = originalText.replaceAll("\\P{Alnum}", " ").trim();

        if (methodNameText.isEmpty()) {
            return "blank";
        }

        // @todo Change this to a switch (camel case / snake case)
        return StringUtils.convertToJoinedCamelCase(methodNameText).replace(" ", "");
    }
}
