package ro.bogdananton.testNameGenerator.settings;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.project.Project;
import com.intellij.util.SystemProperties;
import ro.bogdananton.testNameGenerator.generateTestName;
import ro.bogdananton.testNameGenerator.utils.EditorIntegration;

import java.util.Arrays;

public class EditorDetails
{
    public static String getTabChar()
    {
        int indentSpaces = generateTestName.DEFAULT_INDENT_SPACES;
        boolean useTabCharacter = false;

        try {
            Editor editor = EditorIntegration.getEditor();
            EditorSettings settings = editor.getSettings();
            Project project = editor.getProject();

            useTabCharacter = settings.isUseTabCharacter(project);
            indentSpaces = settings.getTabSize(project);

        } catch (Exception e) {
            // will default to using spaces
        }

        if (useTabCharacter) {
            return "\t";
        }

        char[] charArray = new char[indentSpaces];
        Arrays.fill(charArray, ' ');
        return new String(charArray);
    }

    public static String getLineDelimiter()
    {
        return "\n";
        // return SystemProperties.getLineSeparator();
    }
}

