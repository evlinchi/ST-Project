package state;

import command.Command;
import command.CopyCommand;
import command.DeleteCommand;
import command.InsertCommand;
import command.MacroCommand;
import command.MarkCommand;
import command.UnmarkCommand;
import editor.Editor;
import editor.EditorException;
import model.TextDocument;

/**
 * Има избран текст.
 * Всички действия работят върху селекцията: изтриване, копиране, маркиране,
 * а добавянето и поставянето заменят избрания текст.
 */
public class SelectionState implements EditorState {

    @Override
    public void insert(Editor editor, String text) {
        replaceSelection(editor, text, "Замяна");
    }

    @Override
    public void delete(Editor editor) {
        deleteSelection(editor, "Изтриване");
    }

    @Override
    public void copy(Editor editor) {
        editor.executeCommand(new CopyCommand(editor.getDocument(), editor.getClipboard(),
                editor.getSelectionStart(), editor.getSelectionLength()));
    }

    /**
     * Изрязване: първо CopyCommand (не влиза в историята), после DeleteCommand с надпис "Изрязване".
     * Undo връща изрязания текст, а клипбордът остава с него - както в повечето редактори.
     */
    @Override
    public void cut(Editor editor) {
        copy(editor);
        deleteSelection(editor, "Изрязване");
    }

    @Override
    public void paste(Editor editor) {
        String content = editor.getClipboard().getContent();
        if (content.isEmpty()) {
            throw new EditorException("Клипбордът е празен.");
        }
        replaceSelection(editor, content, "Поставяне върху");
    }

    @Override
    public void mark(Editor editor) {
        int start = editor.getSelectionStart();
        int length = editor.getSelectionLength();
        if (editor.getDocument().isFullyHighlighted(start, length)) {
            throw new EditorException("Избраният текст вече е оцветен.");
        }
        editor.executeCommand(new MarkCommand(editor.getDocument(), start, length));
    }

    @Override
    public void unmark(Editor editor) {
        int start = editor.getSelectionStart();
        int length = editor.getSelectionLength();
        if (!editor.getDocument().hasHighlightsIn(start, length)) {
            throw new EditorException("В избрания текст няма оцветяване.");
        }
        editor.executeCommand(new UnmarkCommand(editor.getDocument(), start, length));
    }

    @Override
    public void undo(Editor editor) {
        String before = editor.getDocument().getText();
        editor.getCommandManager().undo();
        editor.moveCaretToChange(before);
        editor.setState(new NoSelectionState());
    }

    @Override
    public void redo(Editor editor) {
        String before = editor.getDocument().getText();
        editor.getCommandManager().redo();
        editor.moveCaretToChange(before);
        editor.setState(new NoSelectionState());
    }

    @Override
    public void select(Editor editor, int start, int length) {
        editor.setSelection(start, length);
        if (length == 0) {
            editor.setState(new NoSelectionState());
        }
    }

    @Override
    public void toggleReadOnly(Editor editor) {
        editor.setState(new ReadOnlyState());
    }

    @Override
    public void load(Editor editor, String text) {
        editor.resetDocument(text);
        editor.setState(new NoSelectionState());
    }

    @Override
    public String getName() {
        return "Селекция";
    }

    @Override
    public boolean canEdit(Editor editor) {
        return true;
    }

    @Override
    public boolean canCopy(Editor editor) {
        return true;
    }

    @Override
    public boolean canMark(Editor editor) {
        return true;
    }

    /** Изтрива селекцията с една команда и минава в състояние без селекция. */
    private void deleteSelection(Editor editor, String label) {
        int start = editor.getSelectionStart();
        editor.executeCommand(new DeleteCommand(editor.getDocument(), start, editor.getSelectionLength(), label));
        editor.setSelection(start, 0);
        editor.setState(new NoSelectionState());
    }

    /**
     * Замяна = изтриване на селекцията + добавяне на новия текст на нейно място.
     * Двете команди са обединени в MacroCommand, за да се отменят с едно Undo.
     */
    private void replaceSelection(Editor editor, String text, String label) {
        TextDocument document = editor.getDocument();
        int start = editor.getSelectionStart();
        int length = editor.getSelectionLength();
        String oldText = document.getText(start, length);

        Command replace = new MacroCommand(
                label + " (поз. " + start + "): " + Command.preview(oldText) + " -> " + Command.preview(text),
                new DeleteCommand(document, start, length),
                new InsertCommand(document, start, text));

        editor.executeCommand(replace);
        editor.setSelection(start + text.length(), 0);
        editor.setState(new NoSelectionState());
    }
}
