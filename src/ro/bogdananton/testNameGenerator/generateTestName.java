package ro.bogdananton.testNameGenerator;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.util.SystemProperties;
import org.apache.commons.lang.WordUtils;

import java.util.List;

public class generateTestName extends AnAction {
    public static Document doc;
    public static String newDocumentContents = "";

    public void actionPerformed(AnActionEvent e) {
        Editor editor = (Editor) DataManager.getInstance().getDataContext().getData(DataConstants.EDITOR);
        CaretModel caretModel = editor.getCaretModel();
        List<Caret> carets = caretModel.getAllCarets();
        Document doc = editor.getDocument();
        generateTestName.doc = doc;

        String newDocumentContents = "";
        int lineStart = 0;
        int lineEnd = 0;
        int oldLineEnd = 0;

        for (Caret caret: carets) {
            LogicalPosition lp = caret.getLogicalPosition();

            lineStart = doc.getLineStartOffset(lp.line);
            lineEnd = doc.getLineEndOffset(lp.line);
            TextRange lineRange = new TextRange(lineStart, lineEnd);
            String lineContents = doc.getText(lineRange).trim();

            newDocumentContents += doc.getText(new TextRange(oldLineEnd, lineStart));
            newDocumentContents += getPreparedTestMethod(lineContents);

            oldLineEnd = lineEnd;
        }
        newDocumentContents += doc.getText(new TextRange(lineEnd, doc.getLineEndOffset(doc.getLineCount()-1)));
        generateTestName.newDocumentContents = newDocumentContents;

        final Application application = ApplicationManager.getApplication();
        final Runnable runnable = new Runnable(){
            @Override public void run(){
                application.runWriteAction(new Runnable(){
                       @Override public void run(){
                           generateTestName.doc.setText(generateTestName.newDocumentContents);
                       }
                   }
                );
            }
        };
        application.invokeLater(runnable, ModalityState.NON_MODAL);
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
