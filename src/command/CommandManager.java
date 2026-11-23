package command;

import observer.AbstractObservable;
import observer.EditorEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Invoker в Command шаблона: изпълнява командите и пази историята им.
 *
 * Стекът за undo съдържа изпълнените команди, а стекът за redo - отменените.
 * Нова команда изчиства стека за redo, защото след нея старото "бъдеще" вече не е валидно.
 */
public class CommandManager extends AbstractObservable {

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    /** Изпълнява командата и, ако е обратима, я запазва за отмяна. */
    public void executeCommand(Command command) {
        command.execute();
        if (command.isUndoable()) {
            undoStack.push(command);
            redoStack.clear();
            notifyObservers(EditorEvent.HISTORY_CHANGED);
        }
    }

    /** Отменя последната изпълнена команда. */
    public void undo() {
        if (!canUndo()) {
            throw new IllegalStateException("Няма действие за отмяна.");
        }
        Command command = undoStack.peek();
        command.undo();
        undoStack.pop();
        redoStack.push(command);
        notifyObservers(EditorEvent.HISTORY_CHANGED);
    }

    /** Изпълнява отново последната отменена команда. */
    public void redo() {
        if (!canRedo()) {
            throw new IllegalStateException("Няма действие за повторение.");
        }
        Command command = redoStack.peek();
        command.execute();
        redoStack.pop();
        undoStack.push(command);
        notifyObservers(EditorEvent.HISTORY_CHANGED);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /** Изпълнените команди, от най-старата към най-новата. */
    public List<Command> getUndoHistory() {
        List<Command> history = new ArrayList<>(undoStack);
        Collections.reverse(history);
        return history;
    }

    /** Отменените команди, като първа е тази, която ще се повтори с redo. */
    public List<Command> getRedoHistory() {
        return new ArrayList<>(redoStack);
    }

    /** Изчиства историята (при нов или отворен файл). */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
        notifyObservers(EditorEvent.HISTORY_CHANGED);
    }
}
