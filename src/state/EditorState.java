package state;

import editor.Editor;

/**
 * Състояние на редактора (State).
 *
 * Editor (Context) не проверява с if-ове дали има селекция или дали документът е заключен.
 * Той просто предава всяко действие на текущото състояние, а то решава:
 * какво да направи, коя команда да създаде, дали да откаже и в кое състояние да премине.
 */
public interface EditorState {

    // ---------- Действия ----------

    /** Добавя текст (без селекция) или заменя селекцията с него. */
    void insert(Editor editor, String text);

    /** Изтрива символа след курсора или селекцията. */
    void delete(Editor editor);

    void copy(Editor editor);

    /** Копира селекцията в клипборда и я изтрива. */
    void cut(Editor editor);

    void paste(Editor editor);

    void mark(Editor editor);

    void unmark(Editor editor);

    void undo(Editor editor);

    void redo(Editor editor);

    /** Избира текст от start с дължина length. При length = 0 само премества курсора. */
    void select(Editor editor, int start, int length);

    void toggleReadOnly(Editor editor);

    /** Зарежда нов текст (нов или отворен файл). В режим само за четене се отказва. */
    void load(Editor editor, String text);

    // ---------- Информация за интерфейса ----------

    /** Име на състоянието за показване. */
    String getName();

    /** Дали са позволени промени по документа (добавяне, изтриване, поставяне, undo/redo). */
    boolean canEdit(Editor editor);

    boolean canCopy(Editor editor);

    boolean canMark(Editor editor);
}
