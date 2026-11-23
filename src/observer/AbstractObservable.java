package observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Обща реализация на Observable.
 * TextDocument, Clipboard, CommandManager и Editor я наследяват,
 * за да не повтарят кода за списъка с наблюдатели.
 */
public abstract class AbstractObservable implements Observable {

    private final List<Observer> observers = new ArrayList<>();

    @Override
    public void addObserver(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(EditorEvent event) {
        // Обхождаме копие на списъка, за да може наблюдател да се отпише по време на уведомяването.
        for (Observer observer : new ArrayList<>(observers)) {
            observer.update(event);
        }
    }
}
