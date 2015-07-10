package ro.bogdananton.testNameGenerator.datastructure;

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.TextRange;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;

public class CodeChunk
{
    public int startLine;
    public int endLine;
    public String contents;
    public String filename;

    public CodeChunk(int startLine, int endLine, String contents, String filename) {
        this.startLine = startLine;
        this.endLine = endLine;
        this.contents = contents;
        this.filename = filename;
    }

    public static CodeChunk getInstance(Document doc, int fromLine, int toLine)
    {
        int startLine = doc.getLineStartOffset(fromLine);
        int endLine = doc.getLineEndOffset(toLine);

        TextRange lineRange = new TextRange(startLine, endLine);
        String contents = doc.getText(lineRange).trim();

        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(doc);
        String filename = "";

        try {
            filename = currentFile.getPath();
        } catch (NullPointerException e) {
            // ignore
        }

        return new CodeChunk(startLine, endLine, contents, filename);
    }
}
