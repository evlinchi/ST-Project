package command;

import model.Highlight;
import model.TextDocument;

import java.util.List;

/**
 * Маркира (оцветява) част от текста.
 * Новата маркировка може да се слее със съседни, затова при execute() се запомнят
 * всички маркировки преди промяната, а undo() ги връща точно.
 */
public class MarkCommand implements Command {

    private final TextDocument document;
    private final int position;
    private final int length;
    private String markedText;
    private List<Highlight> highlightsBefore;

    public MarkCommand(TextDocument document, int position, int length) {
        this.document = document;
        this.position = position;
        this.length = length;
    }

    @Override
    public void execute() {
        markedText = document.getText(position, length);
        highlightsBefore = document.getHighlights();
        document.addHighlight(new Highlight(position, length));
    }

    @Override
    public void undo() {
        document.setHighlights(highlightsBefore);
    }

    @Override
    public String getDescription() {
        String what = markedText != null ? Command.preview(markedText) : length + " симв.";
        return "Оцветяване (поз. " + position + "): " + what;
    }
}
