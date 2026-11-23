package model;

import observer.AbstractObservable;
import observer.EditorEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Документът, който редактираме: самият текст и маркировките върху него.
 *
 * В Command шаблона това е Receiver-ът: командите не пипат текста директно,
 * а викат методите на този клас.
 * В Observer шаблона това е наблюдаван обект: след всяка промяна уведомява наблюдателите.
 */
public class TextDocument extends AbstractObservable {

    private final StringBuilder text = new StringBuilder();
    private final List<Highlight> highlights = new ArrayList<>();

    // ---------- Текст ----------

    /** Връща целия текст. */
    public String getText() {
        return text.toString();
    }

    /** Връща броя символи в документа. */
    public int length() {
        return text.length();
    }

    /** Връща част от текста, без да я променя. Ползва се при копиране. */
    public String getText(int position, int count) {
        checkRange(position, count);
        return text.substring(position, position + count);
    }

    /**
     * Вмъква текст на дадена позиция.
     * Позиция 0 е преди първия символ, а позиция length() е след последния.
     */
    public void insert(int position, String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Няма текст за добавяне.");
        }
        if (position < 0 || position > text.length()) {
            throw new IllegalArgumentException("Невалидна позиция: " + position);
        }
        text.insert(position, value);
        moveHighlightsAfterInsert(position, value.length());
        notifyObservers(EditorEvent.TEXT_CHANGED);
    }

    /**
     * Изтрива count символа, започвайки от position.
     * Връща изтрития текст, защото без него изтриването не може да се отмени (undo).
     */
    public String delete(int position, int count) {
        String removed = getText(position, count);
        text.delete(position, position + count);
        moveHighlightsAfterDelete(position, count);
        notifyObservers(EditorEvent.TEXT_CHANGED);
        return removed;
    }

    /** Заменя целия текст (нов или отворен файл). Маркировките се изчистват. */
    public void setText(String newText) {
        text.setLength(0);
        text.append(newText == null ? "" : newText);
        highlights.clear();
        notifyObservers(EditorEvent.TEXT_CHANGED);
    }

    // ---------- Маркировки ----------

    /** Връща копие на маркировките, за да не може някой да ги промени отвън. */
    public List<Highlight> getHighlights() {
        return Collections.unmodifiableList(new ArrayList<>(highlights));
    }

    /**
     * Добавя маркировка. Ако тя се застъпва със съществуващи маркировки, те се сливат в една,
     * за да няма двойно оцветяване и всеки символ да е маркиран най-много веднъж.
     */
    public void addHighlight(Highlight highlight) {
        checkRange(highlight.getStart(), highlight.getLength());
        int start = highlight.getStart();
        int end = highlight.getEnd();
        List<Highlight> result = new ArrayList<>();
        for (Highlight h : highlights) {
            if (h.overlaps(start, end - start)) {
                start = Math.min(start, h.getStart());
                end = Math.max(end, h.getEnd());
            } else {
                result.add(h);
            }
        }
        result.add(new Highlight(start, end - start));
        highlights.clear();
        highlights.addAll(result);
        notifyObservers(EditorEvent.MARKS_CHANGED);
    }

    /** Дали всеки символ от дадената част от текста вече е маркиран. */
    public boolean isFullyHighlighted(int position, int count) {
        // Вървим отляво надясно: covered е докъде текстът е покрит без прекъсване.
        // Така се отчитат и съседни маркировки, напр. [0..3) и [3..6).
        int end = position + count;
        int covered = position;
        boolean moved = true;
        while (covered < end && moved) {
            moved = false;
            for (Highlight highlight : highlights) {
                if (highlight.getStart() <= covered && highlight.getEnd() > covered) {
                    covered = highlight.getEnd();
                    moved = true;
                }
            }
        }
        return covered >= end;
    }

    /** Дали има маркировка, която се застъпва с дадената част от текста. */
    public boolean hasHighlightsIn(int position, int count) {
        for (Highlight highlight : highlights) {
            if (highlight.overlaps(position, count)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Премахва маркировката само от дадената част от текста.
     * Ако маркировка излиза извън частта, остават парчетата преди и след нея.
     * Пример: маркиран "one two three", премахване от "two" -> остават "one " и " three".
     */
    public void removeHighlightsIn(int position, int count) {
        int end = position + count;
        List<Highlight> result = new ArrayList<>();
        for (Highlight h : highlights) {
            if (!h.overlaps(position, count)) {
                result.add(h);
                continue;
            }
            if (h.getStart() < position) {
                result.add(new Highlight(h.getStart(), position - h.getStart()));
            }
            if (h.getEnd() > end) {
                result.add(new Highlight(end, h.getEnd() - end));
            }
        }
        highlights.clear();
        highlights.addAll(result);
        notifyObservers(EditorEvent.MARKS_CHANGED);
    }

    /** Заменя всички маркировки наведнъж. Ползва се при undo, за да се върне предишното положение. */
    public void setHighlights(List<Highlight> newHighlights) {
        highlights.clear();
        highlights.addAll(newHighlights);
        notifyObservers(EditorEvent.MARKS_CHANGED);
    }

    // ---------- Помощни методи ----------

    /**
     * След вмъкване маркировките след позицията се преместват надясно,
     * а маркировка, в чиято среда е вмъкнат текст, се удължава.
     */
    private void moveHighlightsAfterInsert(int position, int count) {
        for (int i = 0; i < highlights.size(); i++) {
            Highlight h = highlights.get(i);
            if (h.getStart() >= position) {
                highlights.set(i, new Highlight(h.getStart() + count, h.getLength()));
            } else if (h.getEnd() > position) {
                highlights.set(i, new Highlight(h.getStart(), h.getLength() + count));
            }
        }
    }

    /**
     * След изтриване маркировките се скъсяват или преместват наляво,
     * а изцяло изтритите изчезват.
     */
    private void moveHighlightsAfterDelete(int position, int count) {
        List<Highlight> result = new ArrayList<>();
        for (Highlight h : highlights) {
            int start = positionAfterDelete(h.getStart(), position, count);
            int end = positionAfterDelete(h.getEnd(), position, count);
            if (end > start) {
                result.add(new Highlight(start, end - start));
            }
        }
        highlights.clear();
        highlights.addAll(result);
    }

    /** Къде отива дадена позиция, след като са изтрити count символа от position нататък. */
    private static int positionAfterDelete(int index, int position, int count) {
        if (index <= position) {
            return index;
        }
        if (index >= position + count) {
            return index - count;
        }
        return position;
    }

    /** Проверява дали частта от position с дължина count е изцяло в текста. */
    private void checkRange(int position, int count) {
        if (position < 0 || count <= 0 || position + count > text.length()) {
            throw new IllegalArgumentException(
                    "Невалиден диапазон: позиция " + position + ", дължина " + count);
        }
    }
}
