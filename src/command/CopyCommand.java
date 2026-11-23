package command;

import model.Clipboard;
import model.TextDocument;

/**
 * Копира част от текста в клипборда.
 * Не променя документа, затова isUndoable() връща false и командата не влиза в стека за отмяна.
 */
public class CopyCommand implements Command {

    private final TextDocument document;
    private final Clipboard clipboard;
    private final int position;
    private final int length;
    private String previousContent;

    public CopyCommand(TextDocument document, Clipboard clipboard, int position, int length) {
        this.document = document;
        this.clipboard = clipboard;
        this.position = position;
        this.length = length;
    }

    @Override
    public void execute() {
        previousContent = clipboard.getContent();
        clipboard.setContent(document.getText(position, length));
    }

    /** Връща предишното съдържание на клипборда (реализирано за пълнота на интерфейса). */
    @Override
    public void undo() {
        clipboard.setContent(previousContent);
    }

    @Override
    public boolean isUndoable() {
        return false;
    }

    @Override
    public String getDescription() {
        return "Копиране (поз. " + position + ", " + length + " симв.)";
    }
}
