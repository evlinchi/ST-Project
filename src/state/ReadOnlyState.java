package state;

import command.CopyCommand;
import editor.Editor;
import editor.EditorException;

/**
 * Документът е заключен: всякакви промени се отказват.
 * Позволени са само селекция и копиране, защото те не променят текста.
 * Селекцията се пази, така че при изключване на режима редакторът се връща
 * в Selection или NoSelection според това дали има избран текст.
 */
public class ReadOnlyState implements EditorState {

    private static final String LOCKED = "Документът е само за четене (Ctrl+R отключва).";

    @Override
    public void insert(Editor editor, String text) {
        throw new EditorException(LOCKED);
    }

    @Override
    public void delete(Editor editor) {
        throw new EditorException(LOCKED);
    }

    @Override
    public void copy(Editor editor) {
        if (!editor.hasSelection()) {
            throw new EditorException("Изберете текст, който да копирате.");
        }
        editor.executeCommand(new CopyCommand(editor.getDocument(), editor.getClipboard(),
                editor.getSelectionStart(), editor.getSelectionLength()));
    }

    @Override
    public void cut(Editor editor) {
        // Изрязването трие текст, затова е забранено, макар копирането да е позволено.
        throw new EditorException(LOCKED);
    }

    @Override
    public void paste(Editor editor) {
        throw new EditorException(LOCKED);
    }

    @Override
    public void mark(Editor editor) {
        throw new EditorException(LOCKED);
    }

    @Override
    public void unmark(Editor editor) {
        throw new EditorException(LOCKED);
    }

    @Override
    public void undo(Editor editor) {
        throw new EditorException(LOCKED);
    }

    @Override
    public void redo(Editor editor) {
        throw new EditorException(LOCKED);
    }

    @Override
    public void select(Editor editor, int start, int length) {
        // Селекцията е позволена, но редакторът остава в това състояние.
        editor.setSelection(start, length);
    }

    @Override
    public void toggleReadOnly(Editor editor) {
        if (editor.hasSelection()) {
            editor.setState(new SelectionState());
        } else {
            editor.setState(new NoSelectionState());
        }
    }

    @Override
    public void load(Editor editor, String text) {
        // Нов или отворен файл би заменил заключения документ, затова и това се отказва.
        throw new EditorException(LOCKED);
    }

    @Override
    public String getName() {
        return "Само за четене";
    }

    @Override
    public boolean canEdit(Editor editor) {
        return false;
    }

    @Override
    public boolean canCopy(Editor editor) {
        return editor.hasSelection();
    }

    @Override
    public boolean canMark(Editor editor) {
        return false;
    }
}
