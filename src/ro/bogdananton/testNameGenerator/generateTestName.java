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

            ChangeEntry changeItem = new ChangeEntry();
            changeItem.lineNumber = lp.line;
            changeItem.lineContents = getPreparedTestMethod(lineContents);

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

                    int lineStart = generateTestName.doc.getLineStartOffset(changeItem.lineNumber);
                    int lineEnd = generateTestName.doc.getLineEndOffset(changeItem.lineNumber);

                    generateTestName.doc.replaceString(lineStart, lineEnd, changeItem.lineContents);
                }

            }
        }.execute();
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
