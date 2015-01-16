package ro.bogdananton.testNameGenerator;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.SystemProperties;
import org.apache.commons.lang.WordUtils;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.application.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class generateTestName extends AnAction {
    public static Document doc;
    public static HashMap<Integer, ChangeEntry> changeList;

    public void actionPerformed(AnActionEvent e) {
        Editor editor = (Editor) DataManager.getInstance().getDataContext().getData(DataConstants.EDITOR);
        CaretModel caretModel = editor.getCaretModel();
        List<Caret> carets = caretModel.getAllCarets();
        Document doc = editor.getDocument();
        generateTestName.doc = doc;

        int lineStart = 0;
        int lineEnd = 0;

        changeList = new HashMap<Integer, ChangeEntry>();

        int cursor = carets.size();
        for (Caret caret: carets) {
            LogicalPosition lp = caret.getLogicalPosition();

            lineStart = doc.getLineStartOffset(lp.line);
            lineEnd = doc.getLineEndOffset(lp.line);
            TextRange lineRange = new TextRange(lineStart, lineEnd);
            String lineContents = doc.getText(lineRange).trim();

            ExistingTest updateTest = getInstance(lp.line, doc);
            if (updateTest.doesApply()) {
                String preparedMethodName = getPreparedMethodName(lineContents);
                lineStart = doc.getLineStartOffset(updateTest.getMethodLine());
                lineEnd = doc.getLineEndOffset(updateTest.getMethodLine());
                lineRange = new TextRange(lineStart, lineEnd);
                lineContents = doc.getText(lineRange).trim();
                lineContents = getTabChar() + lineContents.replaceAll(updateTest.getMethodName(), preparedMethodName);
            } else {
                lineContents = getPreparedTestMethod(lineContents);
            }

            ChangeEntry changeItem = new ChangeEntry();
            changeItem.offsetStart = lineStart;
            changeItem.offsetEnd = lineEnd;
            changeItem.lineContents = lineContents;

            generateTestName.changeList.put(cursor, changeItem);
            cursor--;
        }
        new WriteCommandAction(editor.getProject()) {
            @Override
            protected void run(Result result) throws Throwable {
                Set keySet = generateTestName.changeList.keySet();
                Object[] tempArray = keySet.toArray();

                for (int i = 0; i < generateTestName.changeList.size(); i++) {
                    ChangeEntry changeItem = generateTestName.changeList.get(tempArray[i]);
                    generateTestName.doc.replaceString(changeItem.offsetStart, changeItem.offsetEnd, changeItem.lineContents);
                }
            }
        }.execute();
    }


    public static Pattern getTestMethodPattern() {
        Pattern pattern = Pattern.compile("public([\\s]*)function([\\s]*)test([\\w]*)\\(", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return pattern;
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

//            System.out.println(i + ": " + cursorText);

            for(char c : cursorText.toCharArray()) {
                cursorChar = String.valueOf(c);
                // if no additional protection is used, will be buggy when /* and */ are in a string or in commented // line
                if (prevChar.equals("/") & cursorChar.equals("*") & (lineNumber >= i)) {
                    inComment = true;
//                    System.out.println(lineNumber + " - " + i + " -(" + (lineNumber >= i) + ")- comment found in " + cursorText);
                }
                if (prevChar.equals("*") & cursorChar.equals("/") & (lineNumber <= i)) {
                    // take into account starting from the current line, to avoid false positives
                    if (inComment & (lineNumber <= i)) {
                        wasComment = true;
                    }
//                    System.out.println(wasComment + " -----> " + inComment);
                    inComment = false;
                }
                prevChar = cursorChar;
            }

//            System.out.println("[" + lineNumber + "] [" + (lineNumber < i) + "]  [" + (!inComment) + "] [" + (wasComment) + "]");
            if ((lineNumber < i) & !inComment & wasComment) {
                // get the first function found (current pattern states that the method must start with the "test" string)
                Matcher matcher = pattern.matcher(cursorText);
                if (matcher.find()) {
                    // got it
                    instance.setMatchedTestName(matcher.group(3).toString());
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
        return "\t";
    }

    public static String getLineDelimiter()
    {
        return SystemProperties.getLineSeparator();
    }

    public static String toCamelCase(final String init)
    {
        final StringBuilder ret = new StringBuilder(init.length());
        for (final String word : init.split(" ")) {
            ret.append(WordUtils.capitalize(word));
        }
        return ret.toString();
    }
}
