package command;

import model.Highlight;
import model.TextDocument;

import java.util.List;

/**
 * Изтрива част от текста.
 * При execute() запомня изтрития текст и маркировките, за да може undo() да ги върне точно.
 */
public class DeleteCommand implements Command {

    private final TextDocument document;
    private final int position;
    private final int length;
    private final String label;

    private String deletedText;
    private List<Highlight> highlightsBefore;

    public DeleteCommand(TextDocument document, int position, int length) {
        this(document, position, length, "Изтриване");
    }

    /** label е думата в историята: "Изтриване" или "Изрязване". */
    public DeleteCommand(TextDocument document, int position, int length, String label) {
        this.document = document;
        this.position = position;
        this.length = length;
        this.label = label;
    }

    @Override
    public void execute() {
        highlightsBefore = document.getHighlights();
        deletedText = document.delete(position, length);
    }

    @Override
    public void undo() {
        document.insert(position, deletedText);
        document.setHighlights(highlightsBefore);
    }

    @Override
    public String getDescription() {
        String what = deletedText != null ? Command.preview(deletedText) : length + " симв.";
        return label + " (поз. " + position + "): " + what;
    }
}
