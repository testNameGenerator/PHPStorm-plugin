package ro.bogdananton.testNameGenerator.datastructure;

public class ChangeEntry {

    public ChangeEntry(int offsetStart, int offsetEnd, String lineContents) {
        this.offsetStart = offsetStart;
        this.offsetEnd = offsetEnd;
        this.lineContents = lineContents;
    }

    public int offsetStart = 0;
    public int offsetEnd = 0;
    public String lineContents = "";
}
