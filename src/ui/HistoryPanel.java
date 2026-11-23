package ui;

import command.Command;
import command.CommandManager;
import editor.Editor;
import observer.EditorEvent;
import observer.Observer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

/**
 * Показва историята на командите от CommandManager.
 * Изпълнените команди са с тъмен текст (последната е удебелена), а отменените - бледи и в курсив.
 * Наблюдател (Observer): обновява се при HISTORY_CHANGED.
 */
public class HistoryPanel extends JPanel implements Observer {

    private final CommandManager commandManager;
    private final DefaultListModel<Row> model = new DefaultListModel<>();
    private final JList<Row> list = new JList<>(model);
    private final JLabel summary = new JLabel();

    /** Колко от редовете в списъка са изпълнени команди. Останалите са отменени. */
    private int doneCount;

    public HistoryPanel(Editor editor) {
        super(new BorderLayout(0, 8));
        this.commandManager = editor.getCommandManager();
        setOpaque(false);

        list.setFocusable(false);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new HistoryRenderer());
        list.setBackground(Theme.CARD);

        summary.setFont(Theme.font(12, Font.PLAIN));
        summary.setForeground(Theme.MUTED);

        JScrollPane scroll = Theme.softScroll(new JScrollPane(list,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        scroll.getViewport().setBackground(Theme.CARD);
        add(scroll, BorderLayout.CENTER);
        add(summary, BorderLayout.SOUTH);
        refresh();
    }

    @Override
    public void update(EditorEvent event) {
        if (event == EditorEvent.HISTORY_CHANGED) {
            refresh();
        }
    }

    private void refresh() {
        List<Command> done = commandManager.getUndoHistory();
        List<Command> undone = commandManager.getRedoHistory();

        model.clear();
        doneCount = done.size();
        int number = 1;
        for (Command command : done) {
            model.addElement(new Row(number++, command.getDescription()));
        }
        for (Command command : undone) {
            model.addElement(new Row(number++, command.getDescription() + "  - отменена"));
        }

        summary.setText(model.isEmpty() ? "Още няма команди"
                : "За отмяна (undo): " + done.size() + "   ·   За повторение (redo): " + undone.size());
        if (!model.isEmpty()) {
            list.ensureIndexIsVisible(Math.max(doneCount - 1, 0));
        }
    }

    /** Един ред от историята: пореден номер и описание на командата. */
    private record Row(int number, String text) {
    }

    /** Рисува реда като два етикета: бледият номер вляво и описанието до него. */
    private class HistoryRenderer extends JPanel implements ListCellRenderer<Row> {
        private final JLabel numberLabel = new JLabel();
        private final JLabel textLabel = new JLabel();

        HistoryRenderer() {
            super(new BorderLayout(8, 0));
            setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
            numberLabel.setPreferredSize(new Dimension(22, 10));
            numberLabel.setForeground(new Color(160, 165, 181));
            numberLabel.setFont(Theme.font(13, Font.PLAIN));
            add(numberLabel, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Row> list, Row row, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            numberLabel.setText(String.valueOf(row.number()));
            textLabel.setText(row.text());
            textLabel.setToolTipText(row.text());
            setBackground(Theme.CARD);
            if (index >= doneCount) {
                // Отменените команди: бледи и в курсив.
                textLabel.setForeground(Theme.MUTED);
                textLabel.setFont(Theme.font(13, Font.ITALIC));
            } else if (index == doneCount - 1) {
                // Последната изпълнена команда (тя ще се отмени с Undo): удебелена, с мек син фон.
                textLabel.setForeground(Theme.TEXT);
                textLabel.setFont(Theme.font(13, Font.BOLD));
                setBackground(Theme.ACCENT_SOFT);
            } else {
                textLabel.setForeground(Theme.TEXT);
                textLabel.setFont(Theme.font(13, Font.PLAIN));
            }
            return this;
        }
    }
}
