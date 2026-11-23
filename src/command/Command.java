package command;

/**
 * Команда (Command): едно действие върху документа, превърнато в обект.
 * Обектът знае как да изпълни действието и как да го отмени.
 */
public interface Command {

    /** Изпълнява действието. */
    void execute();

    /** Отменя действието и връща документа както е бил преди execute(). */
    void undo();

    /** Кратко описание за панела с историята, например: Добавяне (поз. 5): "текст". */
    String getDescription();

    /**
     * Дали командата се пази за отмяна.
     * Командите, които не променят документа (например копиране), връщат false
     * и CommandManager не ги слага в стека за undo.
     */
    default boolean isUndoable() {
        return true;
    }

    /** Помощен метод: съкращава текста и го слага в кавички, за да се побере в историята. */
    static String preview(String text) {
        String oneLine = text.replace("\n", "\\n");
        if (oneLine.length() > 25) {
            oneLine = oneLine.substring(0, 22) + "...";
        }
        return "\"" + oneLine + "\"";
    }
}
