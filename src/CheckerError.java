public class CheckerError {
    public int line, startOffset, endOffset;
    public String message;
    public CheckerError(int line, int startOffset, int endOffset, String message) {
        this.line = line;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.message = message;
    }

    @Override
    public String toString() {
        return "Line " + line + ": " + message;
    }
}
