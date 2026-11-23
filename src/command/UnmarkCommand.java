package command;

import model.Highlight;
import model.TextDocument;

import java.util.List;

/** Премахва маркировките в дадена част от текста. Отмяната ги връща. */
public class UnmarkCommand implements Command {

    private final TextDocument document;
    private final int position;
    private final int length;
    private List<Highlight> highlightsBefore;

    public UnmarkCommand(TextDocument document, int position, int length) {
        this.document = document;
        this.position = position;
        this.length = length;
    }

    @Override
    public void execute() {
        highlightsBefore = document.getHighlights();
        document.removeHighlightsIn(position, length);
    }

    @Override
    public void undo() {
        document.setHighlights(highlightsBefore);
    }

    @Override
    public String getDescription() {
        return "Премахване на фон (поз. " + position + ", " + length + " симв.)";
    }
}
