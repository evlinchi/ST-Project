package ui;

import editor.Editor;
import model.Highlight;
import observer.EditorEvent;
import observer.Observer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Полето с текста, с номера на редовете и оцветен текущ ред.
 *
 * В полето се пише директно, но Swing не променя текста сам:
 * полето е "само за показване" (setEditable(false)), а всеки натиснат клавиш се превръща
 * в извикване на редактора - editor.insert(), editor.delete() и т.н. Така всяка промяна
 * минава през текущото състояние (State) и става команда (Command) с възможност за Undo.
 *
 * Наблюдател (Observer): когато документът се промени, полето се пречертава само.
 */
public class TextPanel extends JScrollPane implements Observer {

    /** Полупрозрачно оранжево, за да се вижда и селекцията върху оцветения текст. */
    private static final Color MARK_COLOR = new Color(255, 190, 130, 120);
    private static final Color CURRENT_LINE_COLOR = new Color(246, 248, 253);
    private static final Color SELECTION_COLOR = new Color(214, 227, 249);

    private final Editor editor;
    private final EditorActions actions;
    private final JTextArea textArea = new CurrentLineTextArea();
    private final LineNumberGutter gutter = new LineNumberGutter(textArea);
    private final Highlighter.HighlightPainter markPainter = new DefaultHighlighter.DefaultHighlightPainter(MARK_COLOR);
    private final List<Object> markTags = new ArrayList<>();

    /** true, докато ние сменяме текста или селекцията, за да не ги пращаме обратно към редактора. */
    private boolean updating;

    /**
     * @param actions изпълнява операциите и показва грешката горе (до бутоните), ако състоянието ги откаже
     */
    public TextPanel(Editor editor, EditorActions actions) {
        this.editor = editor;
        this.actions = actions;

        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setBackground(Color.WHITE);
        textArea.setFont(Theme.codeFont(15));
        textArea.setForeground(Theme.TEXT);
        textArea.setTabSize(4);
        textArea.setMargin(new Insets(4, 10, 4, 10));
        textArea.setCaretColor(Theme.ACCENT);
        textArea.setSelectionColor(SELECTION_COLOR);
        textArea.setSelectedTextColor(Theme.TEXT);
        // Tab трябва да стига до полето като символ, а не да мести фокуса към следващия компонент.
        textArea.setFocusTraversalKeysEnabled(false);

        AlwaysVisibleCaret caret = new AlwaysVisibleCaret();
        caret.setBlinkRate(500);
        textArea.setCaret(caret);
        caret.setVisible(true);
        caret.setSelectionVisible(true);

        setupKeys();
        textArea.addCaretListener(e -> {
            onUserSelection();
            textArea.repaint();   // текущият ред се е сменил
        });

        setViewportView(textArea);
        setRowHeaderView(gutter);
        getViewport().setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder());
        setOpaque(false);
        Theme.softScroll(this);

        refreshText();
        refreshMarks();
        refreshSelection();
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    @Override
    public void update(EditorEvent event) {
        switch (event) {
            case TEXT_CHANGED:
                refreshText();
                refreshMarks();
                refreshSelection();
                break;
            case MARKS_CHANGED:
                refreshMarks();
                break;
            case SELECTION_CHANGED:
                refreshSelection();
                break;
            default:
                break;
        }
    }

    // ---------- Клавиатура: всеки клавиш става извикване на редактора ----------

    private void setupKeys() {
        // Махаме вградените клавиши на текстовото поле, за да стигнат до действията на прозореца
        // (Ctrl+C, Ctrl+V, Delete...), които минават през редактора и командите.
        InputMap map = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        String[] disabled = {"ctrl C", "ctrl X", "ctrl V", "ctrl Z", "ctrl Y", "ctrl M", "ctrl shift M", "ctrl R",
                "ctrl ENTER", "DELETE", "ctrl INSERT", "shift INSERT", "shift DELETE", "ENTER", "TAB",
                "ctrl H", "ctrl BACK_SPACE", "ctrl DELETE", "ctrl shift O", "ctrl shift T"};
        for (String key : disabled) {
            map.put(KeyStroke.getKeyStroke(key), "none");
        }

        // Backspace няма бутон горе, затова го свързваме директно с полето.
        map.put(KeyStroke.getKeyStroke("BACK_SPACE"), "backspace");
        map.put(KeyStroke.getKeyStroke("shift BACK_SPACE"), "backspace");
        textArea.getActionMap().put("backspace", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actions.perform(TextPanel.this::backspace);
            }
        });

        // Обикновените символи (букви, цифри, Enter, Tab) идват като "keyTyped".
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                boolean altGraph = e.isControlDown() && e.isAltDown();   // AltGr на Windows = Ctrl+Alt
                if ((e.isControlDown() || e.isAltDown() || e.isMetaDown()) && !altGraph) {
                    return;   // клавишни комбинации (Ctrl+Z, Ctrl+C...) - обработват се от прозореца
                }
                if (c == '\n' || c == '\t' || (c != KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(c))) {
                    String typed = String.valueOf(c);
                    actions.perform(() -> editor.insert(typed));
                    e.consume();
                }
            }
        });
    }

    /**
     * Backspace: ако има селекция - изтрива я (SelectionState), иначе премества курсора
     * един символ назад и изтрива символа след него (NoSelectionState).
     * Ако документът е само за четене, курсорът не се мести и състоянието отказва изтриването.
     */
    private void backspace() {
        if (editor.hasSelection() || !editor.canEdit()) {
            editor.delete();
            return;
        }
        int caret = editor.getCaret();
        if (caret == 0) {
            return;
        }
        // Ако преди курсора има "\r\n", не се случва, защото при отваряне на файл те стават "\n".
        editor.select(caret - 1, 0);
        editor.delete();
    }

    // ---------- Синхронизация с редактора ----------

    /** Потребителят е избрал текст или е преместил курсора с мишката/стрелките: казваме на редактора. */
    private void onUserSelection() {
        if (updating) {
            return;
        }
        int start = textArea.getSelectionStart();
        int length = textArea.getSelectionEnd() - start;
        if (start != editor.getSelectionStart() || length != editor.getSelectionLength()) {
            editor.select(start, length);
        }
    }

    /**
     * Прехвърля текста от документа в полето.
     * Не заменяме целия текст (това би върнало скрола в началото), а само частта, която се различава:
     * намираме общото начало и общия край на стария и новия текст и сменяме само средата.
     */
    private void refreshText() {
        String newText = editor.getDocument().getText();
        String oldText = textArea.getText();
        if (newText.equals(oldText)) {
            return;
        }
        int max = Math.min(oldText.length(), newText.length());
        int prefix = 0;
        while (prefix < max && oldText.charAt(prefix) == newText.charAt(prefix)) {
            prefix++;
        }
        int suffix = 0;
        while (suffix < max - prefix
                && oldText.charAt(oldText.length() - 1 - suffix) == newText.charAt(newText.length() - 1 - suffix)) {
            suffix++;
        }
        updating = true;
        try {
            Document document = textArea.getDocument();
            document.remove(prefix, oldText.length() - suffix - prefix);
            document.insertString(prefix, newText.substring(prefix, newText.length() - suffix), null);
        } catch (BadLocationException e) {
            textArea.setText(newText);
        } finally {
            updating = false;
        }
    }

    private void refreshMarks() {
        Highlighter highlighter = textArea.getHighlighter();
        for (Object tag : markTags) {
            highlighter.removeHighlight(tag);
        }
        markTags.clear();
        for (Highlight mark : editor.getDocument().getHighlights()) {
            try {
                markTags.add(highlighter.addHighlight(mark.getStart(), mark.getEnd(), markPainter));
            } catch (BadLocationException e) {
                // Маркировките винаги са в границите на текста, така че това не би трябвало да се случи.
            }
        }
    }

    private void refreshSelection() {
        int textLength = textArea.getDocument().getLength();
        int start = Math.min(editor.getSelectionStart(), textLength);
        int end = Math.min(editor.getSelectionStart() + editor.getSelectionLength(), textLength);
        if (textArea.getSelectionStart() == start && textArea.getSelectionEnd() == end) {
            return;
        }
        updating = true;
        try {
            textArea.setCaretPosition(start);
            textArea.moveCaretPosition(end);
        } finally {
            updating = false;
        }
    }

    /** Текстово поле, което оцветява реда с курсора в много светъл цвят. */
    private static class CurrentLineTextArea extends JTextArea {
        @Override
        protected void paintComponent(Graphics g) {
            // Полето е прозрачно, затова първо рисуваме фона и текущия ред, а после текста отгоре.
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            try {
                Rectangle2D line = modelToView2D(getCaretPosition());
                if (line != null) {
                    g.setColor(CURRENT_LINE_COLOR);
                    g.fillRect(0, (int) line.getY(), getWidth(), (int) Math.ceil(line.getHeight()));
                }
            } catch (BadLocationException e) {
                // Курсорът винаги е в текста.
            }
            super.paintComponent(g);
        }
    }

    /**
     * Курсор, който се вижда и когато полето е "само за показване" и няма фокус.
     * Иначе при натискане на бутон селекцията и курсорът ще изчезват от екрана.
     */
    private static class AlwaysVisibleCaret extends DefaultCaret {
        @Override
        public void setVisible(boolean visible) {
            super.setVisible(true);
        }

        @Override
        public void setSelectionVisible(boolean visible) {
            super.setSelectionVisible(true);
        }
    }
}
