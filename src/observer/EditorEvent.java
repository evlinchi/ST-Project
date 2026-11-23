package observer;

/**
 * Видовете промени, за които наблюдателите (Observer) получават известие.
 * Всяко събитие се изпраща от точно един вид наблюдаван обект.
 */
public enum EditorEvent {

    /** TextDocument: добавен или изтрит текст (маркировките също може да са се преместили). */
    TEXT_CHANGED,

    /** TextDocument: добавени или премахнати маркировки. */
    MARKS_CHANGED,

    /** Clipboard: ново съдържание в клипборда. */
    CLIPBOARD_CHANGED,

    /** CommandManager: изпълнена, отменена или повторена команда. */
    HISTORY_CHANGED,

    /** Editor: преместен курсор или нова селекция. */
    SELECTION_CHANGED,

    /** Editor: редакторът премина в друго състояние (State). */
    STATE_CHANGED
}
