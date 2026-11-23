package state;

import command.DeleteCommand;
import command.InsertCommand;
import editor.Editor;
import editor.EditorException;
import model.TextDocument;

/**
 * Няма избран текст, има само курсор.
 * Добавянето и поставянето стават при курсора, а изтриването маха символа след него.
 * Копиране и маркиране не са възможни, защото няма какво да се копира или маркира.
 */
public class NoSelectionState implements EditorState {

    @Override
    public void insert(Editor editor, String text) {
        int caret = editor.getCaret();
        editor.executeCommand(new InsertCommand(editor.getDocument(), caret, text));
        editor.setSelection(caret + text.length(), 0);
    }

    @Override
    public void delete(Editor editor) {
        TextDocument document = editor.getDocument();
        int caret = editor.getCaret();
        if (caret >= document.length()) {
            throw new EditorException("Няма символ след курсора за изтриване.");
        }
        editor.executeCommand(new DeleteCommand(document, caret, 1));
    }

    @Override
    public void copy(Editor editor) {
        throw new EditorException("Изберете текст, който да копирате.");
    }

    @Override
    public void cut(Editor editor) {
        throw new EditorException("Изберете текст, който да изрежете.");
    }

    @Override
    public void paste(Editor editor) {
        String content = editor.getClipboard().getContent();
        if (content.isEmpty()) {
            throw new EditorException("Клипбордът е празен.");
        }
        int caret = editor.getCaret();
        editor.executeCommand(new InsertCommand(editor.getDocument(), caret, content, "Поставяне"));
        editor.setSelection(caret + content.length(), 0);
    }

    @Override
    public void mark(Editor editor) {
        throw new EditorException("Изберете текст, който да оцветите.");
    }

    @Override
    public void unmark(Editor editor) {
        throw new EditorException("Изберете текст, от който да премахнете фона.");
    }

    @Override
    public void undo(Editor editor) {
        String before = editor.getDocument().getText();
        editor.getCommandManager().undo();
        editor.moveCaretToChange(before);
    }

    @Override
    public void redo(Editor editor) {
        String before = editor.getDocument().getText();
        editor.getCommandManager().redo();
        editor.moveCaretToChange(before);
    }

    @Override
    public void select(Editor editor, int start, int length) {
        editor.setSelection(start, length);
        if (length > 0) {
            editor.setState(new SelectionState());
        }
    }

    @Override
    public void toggleReadOnly(Editor editor) {
        editor.setState(new ReadOnlyState());
    }

    @Override
    public void load(Editor editor, String text) {
        editor.resetDocument(text);
    }

    @Override
    public String getName() {
        return "Без селекция";
    }

    @Override
    public boolean canEdit(Editor editor) {
        return true;
    }

    @Override
    public boolean canCopy(Editor editor) {
        return false;
    }

    @Override
    public boolean canMark(Editor editor) {
        return false;
    }
}
