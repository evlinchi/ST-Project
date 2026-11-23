package command;

import model.TextDocument;

/** Добавя текст на дадена позиция. Отмяната изтрива същия брой символи от същата позиция. */
public class InsertCommand implements Command {

    private final TextDocument document;
    private final int position;
    private final String text;
    private final String label;

    public InsertCommand(TextDocument document, int position, String text) {
        this(document, position, text, "Добавяне");
    }

    /** label е думата в историята: "Добавяне" или "Поставяне", ако текстът идва от клипборда. */
    public InsertCommand(TextDocument document, int position, String text, String label) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Няма текст за добавяне.");
        }
        this.document = document;
        this.position = position;
        this.text = text;
        this.label = label;
    }

    @Override
    public void execute() {
        document.insert(position, text);
    }

    @Override
    public void undo() {
        document.delete(position, text.length());
    }

    @Override
    public String getDescription() {
        return label + " (поз. " + position + "): " + Command.preview(text);
    }
}
