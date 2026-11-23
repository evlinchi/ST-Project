package ui;

import editor.Editor;
import editor.EditorException;
import observer.EditorEvent;
import observer.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

/**
 * Действията от лентата с инструменти и клавишните комбинации.
 * Едно и също действие се ползва и от бутона, и от клавиша, затова се включва и изключва на едно място.
 *
 * Наблюдател (Observer): след всяка промяна пита редактора (а той - текущото състояние)
 * кои действия са позволени. Така бутоните сами следят състоянието (State).
 */
public class EditorActions implements Observer {

    private static final int ICON = 20;

    public final Action newFile;
    public final Action open;
    public final Action save;

    public final Action undo;
    public final Action redo;
    public final Action cut;
    public final Action copy;
    public final Action paste;
    public final Action delete;
    public final Action mark;
    public final Action unmark;
    public final Action readOnly;

    private final Editor editor;
    private final StatusBar statusBar;

    public EditorActions(EditorFrame frame, Editor editor, StatusBar statusBar) {
        this.editor = editor;
        this.statusBar = statusBar;

        newFile = create("Нов документ", "ctrl N", Icons.newFile(ICON), frame::newDocument);
        open = create("Отваряне на файл", "ctrl O", Icons.open(ICON), frame::openFile);
        save = create("Запазване", "ctrl S", Icons.save(ICON), frame::saveFile);

        undo = create("Отмяна", "ctrl Z", Icons.undo(ICON), editor::undo);
        redo = create("Възвръщане", "ctrl Y", Icons.redo(ICON), editor::redo);
        cut = create("Изрязване", "ctrl X", Icons.cut(ICON), editor::cut);
        copy = create("Копиране", "ctrl C", Icons.copy(ICON), editor::copy);
        paste = create("Поставяне", "ctrl V", Icons.plus(ICON), editor::paste);
        delete = create("Изтриване", "DELETE", Icons.minus(ICON), editor::delete);
        mark = create("Добавяне на фон", "ctrl M", Icons.mark(ICON), editor::mark);
        unmark = create("Премахване на фон", "ctrl shift M", Icons.unmark(ICON), editor::unmark);
        readOnly = create("Заключване", "ctrl R", Icons.readOnly(ICON), editor::toggleReadOnly);

        refresh();
    }

    @Override
    public void update(EditorEvent event) {
        refresh();
    }

    /**
     * Изпълнява операция и показва грешката горе (до бутоните), ако състоянието я откаже.
     * Връща true при успех.
     */
    public boolean perform(Runnable operation) {
        statusBar.clearMessage();
        try {
            operation.run();
            return true;
        } catch (EditorException | IllegalArgumentException | IllegalStateException e) {
            statusBar.showError(e.getMessage());
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
    }

    /** Включва и изключва бутоните според текущото състояние на редактора. */
    private void refresh() {
        undo.setEnabled(editor.canUndo());
        redo.setEnabled(editor.canRedo());
        cut.setEnabled(editor.canCut());
        copy.setEnabled(editor.canCopy());
        paste.setEnabled(editor.canPaste());
        delete.setEnabled(editor.canEdit());
        mark.setEnabled(editor.canMark());
        unmark.setEnabled(editor.canMark());
        newFile.setEnabled(editor.canEdit());
        open.setEnabled(editor.canEdit());
        readOnly.putValue(Action.SELECTED_KEY, !editor.canEdit());
        // Подсказката казва какво ще направи бутонът сега, а не двете неща наведнъж.
        readOnly.putValue(Action.SHORT_DESCRIPTION, editor.canEdit()
                ? "Заключване (само за четене) (Ctrl+R)"
                : "Отключване (Ctrl+R)");
    }

    /** Действие с име (то е и подсказката на бутона), клавиш и икона. */
    private Action create(String name, String key, Icon icon, Runnable operation) {
        Action action = new AbstractAction(name, icon) {
            @Override
            public void actionPerformed(ActionEvent e) {
                perform(operation);
            }
        };
        action.putValue(Action.SHORT_DESCRIPTION, name + " (" + keyText(key) + ")");
        action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(key));
        return action;
    }

    /** "ctrl shift M" -> "Ctrl+Shift+M" за подсказките. */
    private static String keyText(String key) {
        StringBuilder result = new StringBuilder();
        for (String part : key.split(" ")) {
            if (result.length() > 0) {
                result.append('+');
            }
            result.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
        }
        return result.toString();
    }
}
