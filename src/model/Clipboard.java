package model;

import observer.AbstractObservable;
import observer.EditorEvent;

/**
 * Вътрешният клипборд на редактора.
 * Receiver за CopyCommand и наблюдаван обект (уведомява при ново съдържание).
 */
public class Clipboard extends AbstractObservable {

    private String content = "";

    public String getContent() {
        return content;
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public void setContent(String content) {
        this.content = content == null ? "" : content;
        notifyObservers(EditorEvent.CLIPBOARD_CHANGED);
    }
}
