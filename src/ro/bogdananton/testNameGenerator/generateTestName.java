package ro.bogdananton.testNameGenerator;

import com.intellij.ide.DataManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.SystemProperties;
import org.apache.commons.lang.WordUtils;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.application.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class generateTestName extends AnAction {
    public static final String REGEX_PATTERN_PHP_METHOD_TEST = "public([\\s]*)function([\\s]*)test([\\w]*)\\(";
    public static Document doc;
    public static HashMap<Integer, ChangeEntry> changeList;

    public void actionPerformed(AnActionEvent e) {
        try {
            Editor editor = (Editor) DataManager.getInstance().getDataContext().getData(DataConstants.EDITOR);
            CaretModel caretModel = getCaretModel(editor);
            List<Caret> carets = caretModel.getAllCarets();
            Document doc = getDocument(editor);
            generateTestName.doc = doc;

            changeList = new HashMap<>();

            int cursor = carets.size();
            for (Caret caret: carets) {
                int currentLine = caret.getLogicalPosition().line;
                generateTestName.changeList.put(cursor, getChangeEntry(doc, currentLine));
                cursor--;
            }

            new WriteCommandAction(getProject(editor)) {
                @Override
                protected void run(@Nullable Result result) throws Throwable {
                    for (ChangeEntry changeItem : generateTestName.changeList.values()) {
                        generateTestName.doc.replaceString(changeItem.offsetStart, changeItem.offsetEnd, changeItem.lineContents);
                    }
                }
            }.execute();

        } catch (NullPointerException exception) {
            String contents = "Failed to perform the action. Please report this issue: https://github.com/testNameGenerator/PHPStorm-plugin/issues";
            showError(e, contents);
        }
    }

    private void showError(AnActionEvent e, String contents) {
        String group = "testNameGenerator";
        (new Notification(group, group, contents, NotificationType.ERROR)).notify(e.getProject());
    }

    private Project getProject(Editor editor) {
        return editor.getProject();
    }

    @NotNull
    private Document getDocument(Editor editor) {
        return editor.getDocument();
    }

    @NotNull
    private CaretModel getCaretModel(Editor editor) {
        return editor.getCaretModel();
    }

    @NotNull
    private ChangeEntry getChangeEntry(Document doc, int currentLine) {
        int lineStart;
        int lineEnd;
        lineStart = doc.getLineStartOffset(currentLine);
        lineEnd = doc.getLineEndOffset(currentLine);
        TextRange lineRange = new TextRange(lineStart, lineEnd);
        String lineContents = doc.getText(lineRange).trim();

        ExistingTest updateTest = getInstance(currentLine, doc);
        if (updateTest.doesApply()) {
            String preparedMethodName = getPreparedMethodName(lineContents);
            lineStart = doc.getLineStartOffset(updateTest.getMethodLine());
            lineEnd = doc.getLineEndOffset(updateTest.getMethodLine());
            lineRange = new TextRange(lineStart, lineEnd);
            lineContents = getTabChar() + doc.getText(lineRange).trim().replaceAll(updateTest.getMethodName(), preparedMethodName);
        } else {
            lineContents = getPreparedTestMethod(lineContents);
        }

        return new ChangeEntry(lineStart, lineEnd, lineContents);
    }

    public static Pattern getTestMethodPattern() {
        return Pattern.compile(REGEX_PATTERN_PHP_METHOD_TEST, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    public static ExistingTest getInstance(int lineNumber, Document doc) {
        String cursorText, cursorChar, prevChar = "";
        int i;
        boolean inComment = false;
        boolean wasComment = false; // using this to update only if the current text was in a docblock comment
        Pattern pattern = getTestMethodPattern();

        int lowerChunkBorder = java.lang.Math.max(lineNumber - ExistingTest.lineDifferenceBorder, 0);
        int upperChunkBorder = java.lang.Math.min(lineNumber + 2 + ExistingTest.lineDifferenceBorder, doc.getLineCount() - 1);
        ExistingTest instance = new ExistingTest(); // blank and inactive

        for	(i = lowerChunkBorder; i <= upperChunkBorder; i++) {
            int lineStart = doc.getLineStartOffset(i);
            int lineEnd = doc.getLineEndOffset(i);
            TextRange lineRange = new TextRange(lineStart, lineEnd);
            cursorText = doc.getText(lineRange).trim();

            for(char c : cursorText.toCharArray()) {
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

    public static String getPreparedTestMethod(String originalText)
    {
        String tab = getTabChar();
        String lineDelimiter = getLineDelimiter();
        String commentText = lineDelimiter + tab + "/**" + lineDelimiter +
                tab + " * " + originalText.replace("*/", "* /") + lineDelimiter +
                tab + " */" + lineDelimiter;
        String methodText = "public function test" + getPreparedMethodName(originalText) + "()" + lineDelimiter +
                tab + "{" + lineDelimiter +
                tab + tab + "$this->markTestIncomplete('implement me...');" + lineDelimiter +
                tab + "}" + lineDelimiter;
        return commentText + tab + methodText;
    }

    public static String getPreparedMethodName(String originalText) {
        String methodNameText = originalText.replaceAll("\\P{Alnum}", " ").trim();
        methodNameText = (methodNameText.isEmpty()) ? "Blank" : toCamelCase(methodNameText).replace(" ", "");
        return methodNameText;
    }

    public static String getTabChar()
    {
        return "    ";
    }

    public static String getLineDelimiter()
    {
        return SystemProperties.getLineSeparator();
    }

    public static String toCamelCase(final String init)
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
