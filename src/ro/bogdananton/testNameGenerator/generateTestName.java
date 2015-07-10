package ro.bogdananton.testNameGenerator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.application.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.bogdananton.testNameGenerator.datastructure.ChangeEntry;
import ro.bogdananton.testNameGenerator.datastructure.ExistingTestEntry;
import ro.bogdananton.testNameGenerator.languages.Generic;
import ro.bogdananton.testNameGenerator.languages.PHP;
import ro.bogdananton.testNameGenerator.utils.EditorIntegration;
import ro.bogdananton.testNameGenerator.utils.TestManager;

import java.util.HashMap;
import java.util.List;

public class generateTestName extends AnAction {
    public static final int DEFAULT_INDENT_SPACES = 4;
    public static Document doc;
    public static HashMap<Integer, ChangeEntry> changeList;

    public void actionPerformed(AnActionEvent e) {
        try {
            Editor editor = EditorIntegration.getEditor();
            List<Caret> carets = EditorIntegration.getCarets(editor);

            Document doc = editor.getDocument();
            generateTestName.doc = doc;

            changeList = new HashMap<Integer, ChangeEntry>();

            int cursor = carets.size();
            for (Caret caret: carets) {
                int currentLine = caret.getLogicalPosition().line;
                generateTestName.changeList.put(cursor, getChangeEntry(doc, currentLine));
                cursor--;
            }

            new WriteCommandAction(editor.getProject()) {
                @Override
                protected void run(@Nullable Result result) throws Throwable {
                    for (ChangeEntry changeItem : generateTestName.changeList.values()) {
                        generateTestName.doc.replaceString(changeItem.offsetStart, changeItem.offsetEnd, changeItem.lineContents);
                    }
                }
            }.execute();

        } catch (NullPointerException exception) {
            String contents = "Failed to perform the action. Please report this issue: https://github.com/testNameGenerator/PHPStorm-plugin/issues";
            EditorIntegration.showError(e, contents);
        }
    }

    @NotNull
    private ChangeEntry getChangeEntry(Document doc, int currentLine) {
        int lineStart;
        int lineEnd;
        lineStart = doc.getLineStartOffset(currentLine);
        lineEnd = doc.getLineEndOffset(currentLine);
        TextRange lineRange = new TextRange(lineStart, lineEnd);
        String lineContents = doc.getText(lineRange).trim();

        ExistingTestEntry updateTest = TestManager.getInstance(currentLine, doc);
        if (updateTest.doesApply()) {
            String preparedMethodName = Generic.getPreparedMethodName(lineContents);
            lineStart = doc.getLineStartOffset(updateTest.getMethodLine());
            lineEnd = doc.getLineEndOffset(updateTest.getMethodLine());
            lineRange = new TextRange(lineStart, lineEnd);
            lineContents = ro.bogdananton.testNameGenerator.settings.EditorSettings.getTabChar() + doc.getText(lineRange).trim().replaceAll(updateTest.getMethodName(), preparedMethodName);
        } else {
            lineContents = PHP.getNewTestMethodContentString(lineContents);
        }

        return new ChangeEntry(lineStart, lineEnd, lineContents);
    }

}
