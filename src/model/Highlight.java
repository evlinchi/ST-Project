package model;

/**
 * Една маркировка (оцветяване с маркер) върху част от текста.
 * Обектът е неизменяем: при промяна на текста документът създава нова маркировка с новите позиции.
 */
public final class Highlight {

    private final int start;
    private final int length;

    public Highlight(int start, int length) {
        if (start < 0 || length <= 0) {
            throw new IllegalArgumentException("Невалидна маркировка: позиция " + start + ", дължина " + length);
        }
        this.start = start;
        this.length = length;
    }

    public int getStart() {
        return start;
    }

    public int getLength() {
        return length;
    }

    /** Позицията веднага след последния маркиран символ. */
    public int getEnd() {
        return start + length;
    }

    /** Дали маркировката се застъпва с частта от position с дължина count. */
    public boolean overlaps(int position, int count) {
        return start < position + count && position < getEnd();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Highlight)) {
            return false;
        }
        Highlight that = (Highlight) other;
        return start == that.start && length == that.length;
    }

    @Override
    public int hashCode() {
        return 31 * start + length;
    }

    @Override
    public String toString() {
        return "[" + start + ".." + getEnd() + ")";
    }
}
