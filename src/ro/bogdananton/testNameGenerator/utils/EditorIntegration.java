package ro.bogdananton.testNameGenerator.utils;

import com.intellij.ide.DataManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EditorIntegration {
    public static Editor getEditor() {
        return (Editor) DataManager.getInstance().getDataContext().getData(DataConstants.EDITOR);
    }

    public static void showError(AnActionEvent e, String contents) {
        String group = "testNameGenerator";
        (new Notification(group, group, contents, NotificationType.ERROR)).notify(e.getProject());
    }

    @NotNull
    public static List<Caret> getCarets(Editor editor) {
        CaretModel caretModel = editor.getCaretModel();
        return caretModel.getAllCarets();
    }
}
