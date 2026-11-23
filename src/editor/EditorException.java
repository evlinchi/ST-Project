package editor;

/**
 * Грешка, когато действие не е позволено в текущото състояние
 * или е подадена невалидна позиция. Интерфейсът показва съобщението на потребителя.
 */
public class EditorException extends RuntimeException {

    public EditorException(String message) {
        super(message);
    }
}
