package observer;

/**
 * Наблюдаван обект (Subject): пази списък с наблюдатели и ги уведомява при промяна.
 */
public interface Observable {

    void addObserver(Observer observer);

    void removeObserver(Observer observer);

    void notifyObservers(EditorEvent event);
}
