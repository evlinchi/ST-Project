package editor;

import command.Command;
import command.CommandManager;
import model.Clipboard;
import model.TextDocument;
import observer.AbstractObservable;
import observer.EditorEvent;
import observer.Observer;
import state.EditorState;
import state.NoSelectionState;

/**
 * Редакторът - централният клас, през който интерфейсът работи с документа.
 *
 * В State шаблона това е Context-ът: пази текущото състояние и му предава всяко действие.
 * В Command шаблона е Client: състоянията създават командите, а Editor ги подава на CommandManager.
 * В Observer шаблона е наблюдаван обект: уведомява при смяна на селекцията и състоянието.
 */
public class Editor extends AbstractObservable {

    private final TextDocument document = new TextDocument();
    private final Clipboard clipboard = new Clipboard();
    private final CommandManager commandManager = new CommandManager();

    private EditorState state = new NoSelectionState();
    private int selectionStart = 0;
    private int selectionLength = 0;

    // ---------- Действия: всяко се предава на текущото състояние ----------

    public void insert(String text) {
        state.insert(this, text);
    }

    public void delete() {
        state.delete(this);
    }

    public void copy() {
        state.copy(this);
    }

    /** Изрязване = копиране на селекцията + изтриването ѝ. */
    public void cut() {
        state.cut(this);
    }

    public void paste() {
        state.paste(this);
    }

    public void mark() {
        state.mark(this);
    }

    public void unmark() {
        state.unmark(this);
    }

    public void undo() {
        state.undo(this);
    }

    public void redo() {
        state.redo(this);
    }

    public void select(int start, int length) {
        state.select(this, start, length);
    }

    public void toggleReadOnly() {
        state.toggleReadOnly(this);
    }

    /** Зарежда нов текст (нов или отворен файл). Състоянието решава дали е позволено. */
    public void load(String text) {
        state.load(this, text);
    }

    // ---------- Методи, които ползват състоянията ----------

    /** Смяна на състоянието. Вика се само от самите състояния. */
    public void setState(EditorState newState) {
        state = newState;
        notifyObservers(EditorEvent.STATE_CHANGED);
    }

    /** Подава команда на CommandManager (Invoker), който я изпълнява и пази за undo. */
    public void executeCommand(Command command) {
        commandManager.executeCommand(command);
    }

    /** Задава селекцията директно, без смяна на състоянието. Проверява дали е в границите на текста. */
    public void setSelection(int start, int length) {
        if (start < 0 || length < 0 || start + length > document.length()) {
            throw new EditorException("Невалидни позиция " + start + " и дължина " + length
                    + ": текстът има " + document.length() + " символа.");
        }
        if (start == selectionStart && length == selectionLength) {
            return;
        }
        selectionStart = start;
        selectionLength = length;
        notifyObservers(EditorEvent.SELECTION_CHANGED);
    }

    /**
     * Заменя текста: историята се изчиства, курсорът отива в началото.
     * Вика се само от състоянията, след като са решили, че зареждането е позволено.
     */
    public void resetDocument(String text) {
        document.setText(text);
        commandManager.clear();
        setSelection(0, 0);
    }

    /**
     * След undo/redo слага курсора в края на промененото място, както в повечето редактори.
     * Сравнява текста преди и след: общото начало и общият край са непроменени,
     * а курсорът отива след новата среда. Ако текстът не е сменен (напр. отменено маркиране),
     * курсорът само се връща в границите на текста.
     */
    public void moveCaretToChange(String textBefore) {
        String textAfter = document.getText();
        if (textAfter.equals(textBefore)) {
            collapseSelection();
            return;
        }
        int max = Math.min(textBefore.length(), textAfter.length());
        int prefix = 0;
        while (prefix < max && textBefore.charAt(prefix) == textAfter.charAt(prefix)) {
            prefix++;
        }
        int suffix = 0;
        while (suffix < max - prefix
                && textBefore.charAt(textBefore.length() - 1 - suffix) == textAfter.charAt(textAfter.length() - 1 - suffix)) {
            suffix++;
        }
        setSelection(textAfter.length() - suffix, 0);
    }

    /** Премахва селекцията и оставя курсора в границите на текста (след undo/redo текстът може да е по-къс). */
    public void collapseSelection() {
        setSelection(Math.min(selectionStart, document.length()), 0);
    }

    // ---------- Информация ----------

    public TextDocument getDocument() {
        return document;
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public EditorState getState() {
        return state;
    }

    public int getSelectionStart() {
        return selectionStart;
    }

    public int getSelectionLength() {
        return selectionLength;
    }

    /** Курсорът е в началото на селекцията (когато няма селекция, дължината е 0). */
    public int getCaret() {
        return selectionStart;
    }

    public boolean hasSelection() {
        return selectionLength > 0;
    }

    public boolean canEdit() {
        return state.canEdit(this);
    }

    public boolean canCopy() {
        return state.canCopy(this);
    }

    public boolean canMark() {
        return state.canMark(this);
    }

    public boolean canCut() {
        return state.canEdit(this) && state.canCopy(this);
    }

    public boolean canPaste() {
        return state.canEdit(this) && !clipboard.isEmpty();
    }

    public boolean canUndo() {
        return state.canEdit(this) && commandManager.canUndo();
    }

    public boolean canRedo() {
        return state.canEdit(this) && commandManager.canRedo();
    }

    /** Записва наблюдателя за промените във всички части на редактора наведнъж. */
    public void addObserverToAll(Observer observer) {
        addObserver(observer);
        document.addObserver(observer);
        clipboard.addObserver(observer);
        commandManager.addObserver(observer);
    }
}
