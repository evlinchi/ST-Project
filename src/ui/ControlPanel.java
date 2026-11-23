package ui;

import editor.Editor;
import observer.EditorEvent;
import observer.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

/**
 * Панелът "Операции по позиция": добавяне, изтриване, маркиране и копиране на текст
 * на дадена позиция - точно действията от заданието.
 *
 * Полетата "Позиция" и "Дължина" винаги показват селекцията на редактора (Observer).
 * Когато потребителят ги промени, селекцията се сменя веднага и се вижда в текста,
 * а редакторът минава в друго състояние (Без селекция / Селекция).
 */
public class ControlPanel extends JPanel implements Observer {

    private static final Color INVALID_COLOR = new Color(255, 236, 238);

    private final Editor editor;
    private final EditorActions actions;
    private final JTextField positionField = new JTextField(6);
    private final JTextField lengthField = new JTextField(6);
    private final JLabel limitLabel = new JLabel();
    private final JTextArea inputArea = new JTextArea(2, 20);
    private final Action insert;

    /** true, докато прехвърляме стойности между полетата и редактора, за да не се получи безкраен цикъл. */
    private boolean syncing;

    public ControlPanel(Editor editor, EditorActions actions) {
        this.editor = editor;
        this.actions = actions;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        insert = new AbstractAction("Добави текста") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputArea.getText();
                actions.perform(() -> editor.insert(text));
            }
        };
        inputArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl ENTER"), "insert-at-position");
        inputArea.getActionMap().put("insert-at-position", insert);

        add(row(field("Позиция", positionField), field("Дължина", lengthField)));
        add(Box.createVerticalStrut(4));
        limitLabel.setFont(Theme.font(12, Font.PLAIN));
        limitLabel.setForeground(Theme.MUTED);
        add(left(limitLabel));
        add(Box.createVerticalStrut(12));
        add(left(caption("Текст за добавяне")));
        add(Box.createVerticalStrut(4));
        add(left(createInputArea()));
        add(Box.createVerticalStrut(10));
        add(left(Theme.primary(new JButton(insert))));

        DocumentListener rangeListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyRange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyRange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        };
        positionField.getDocument().addDocumentListener(rangeListener);
        lengthField.getDocument().addDocumentListener(rangeListener);

        refresh();
    }

    @Override
    public void update(EditorEvent event) {
        refresh();
    }

    /** Прехвърля позицията и дължината от полетата към редактора (сменя селекцията). */
    private void applyRange() {
        if (syncing) {
            return;
        }
        Integer position = parse(positionField.getText());
        Integer length = lengthField.getText().trim().isEmpty() ? Integer.valueOf(0) : parse(lengthField.getText());
        if (position == null || length == null) {
            markInvalid(positionField, position == null);
            markInvalid(lengthField, length == null);
            return;
        }
        syncing = true;
        try {
            boolean ok = actions.perform(() -> editor.select(position, length));
            markInvalid(positionField, !ok);
            markInvalid(lengthField, !ok);
        } finally {
            syncing = false;
        }
    }

    /** Показва в полетата текущата селекция на редактора. */
    private void refresh() {
        insert.setEnabled(editor.canEdit());
        if (!syncing) {
            syncing = true;
            try {
                setTextIfDifferent(positionField, String.valueOf(editor.getSelectionStart()));
                setTextIfDifferent(lengthField, String.valueOf(editor.getSelectionLength()));
                markInvalid(positionField, false);
                markInvalid(lengthField, false);
            } finally {
                syncing = false;
            }
        }
        limitLabel.setText("Позициите са от 0 до " + editor.getDocument().length());
    }

    // ---------- Изглед ----------

    private Component createInputArea() {
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(Theme.font(13, Font.PLAIN));
        inputArea.setToolTipText("Ctrl+Enter добавя текста");
        inputArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane scroll = Theme.softScroll(new JScrollPane(inputArea));
        scroll.setBorder(new Theme.RoundBorder(null, Theme.BORDER, 10));
        scroll.setPreferredSize(new Dimension(320, 56));
        return scroll;
    }

    private static JLabel caption(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(12, Font.PLAIN));
        label.setForeground(Theme.MUTED);
        return label;
    }

    /** Етикет над поле за въвеждане. */
    private static JPanel field(String title, JTextField field) {
        field.setBorder(Theme.fieldBorder());
        field.setFont(Theme.font(13, Font.PLAIN));
        field.setForeground(Theme.TEXT);
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.add(caption(title), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private static JComponent row(JComponent first, JComponent second) {
        JPanel row = new JPanel(new GridLayout(1, 2, 10, 0));
        row.setOpaque(false);
        row.add(first);
        row.add(second);
        return left(row);
    }

    private static JComponent left(Component component) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(component, BorderLayout.CENTER);
        wrapper.setAlignmentX(LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, wrapper.getPreferredSize().height));
        return wrapper;
    }

    private static Integer parse(String text) {
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void markInvalid(JTextField field, boolean invalid) {
        field.setBackground(invalid ? INVALID_COLOR : Color.WHITE);
    }

    private static void setTextIfDifferent(JTextField field, String value) {
        if (!field.getText().equals(value)) {
            field.setText(value);
        }
    }
}
