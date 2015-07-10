package ro.bogdananton.testNameGenerator.utils;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import ro.bogdananton.testNameGenerator.datastructure.ExistingTestEntry;
import ro.bogdananton.testNameGenerator.languages.PHP;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestManager {
    public static ExistingTestEntry getInstance(int lineNumber, Document doc) {
        String cursorText, cursorChar, prevChar = "";
        int i;
        boolean inComment = false;
        boolean wasComment = false; // using this to update only if the current text was in a docblock comment
        Pattern pattern = PHP.getTestMethodPattern();

        int lowerChunkBorder = Math.max(lineNumber - ExistingTestEntry.lineDifferenceBorder, 0);
        int upperChunkBorder = Math.min(lineNumber + 2 + ExistingTestEntry.lineDifferenceBorder, doc.getLineCount() - 1);
        ExistingTestEntry instance = new ExistingTestEntry(); // blank and inactive

        for (i = lowerChunkBorder; i <= upperChunkBorder; i++) {
            int lineStart = doc.getLineStartOffset(i);
            int lineEnd = doc.getLineEndOffset(i);
            TextRange lineRange = new TextRange(lineStart, lineEnd);
            cursorText = doc.getText(lineRange).trim();

            for (char c : cursorText.toCharArray()) {
                cursorChar = String.valueOf(c);
                // if no additional protection is used, will be buggy when /* and */ are in a string or in commented // line
                if (prevChar.equals("/") & cursorChar.equals("*") & (lineNumber >= i)) {
                    inComment = true;
                }
                if (prevChar.equals("*") & cursorChar.equals("/") & (lineNumber <= i)) {
                    // take into account starting from the current line, to avoid false positives
                    if (inComment & (lineNumber <= i)) {
                        wasComment = true;
                    }
                    inComment = false;
                }
                prevChar = cursorChar;
            }

            if ((lineNumber < i) & !inComment & wasComment) {
                // get the first function found (current pattern states that the method must start with the "test" string)
                Matcher matcher = pattern.matcher(cursorText);
                if (matcher.find()) {
                    // got it
                    instance.setMatchedTestName(matcher.group(3));
                    instance.setMethodLine(i);
                    instance.setMethodText(cursorText);
                    instance.setDoesApply(true);
                    return instance;
                }
            }
        }

        return instance;
    }
}