package ui;

import editor.Editor;
import observer.EditorEvent;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

/**
 * Главният прозорец: горна лента с името, реда с позицията и инструментите,
 * карта с текста и карти "Операции по позиция" и "История на командите" вдясно.
 * Сглобява панелите и ги записва като наблюдатели (Observer) на редактора.
 */
public class EditorFrame extends JFrame {

    private static final String APP_NAME = "TextPad";

    private final Editor editor;
    private final StatusBar statusBar;
    private final EditorActions actions;
    private final TextPanel textPanel;
    private final JLabel fileLabel = new JLabel();

    /** Името на файла ("Безименен", докато не се запише или отвори файл). */
    private String fileName = "Безименен";
    /** Текстът при последното записване/отваряне. Ако текущият се различава, има незаписани промени. */
    private String savedText;
    /** Папката от последното отваряне/записване - следващият диалог започва от нея. */
    private String lastDirectory;

    public EditorFrame(Editor editor) {
        super(APP_NAME);
        this.editor = editor;
        this.savedText = editor.getDocument().getText();

        statusBar = new StatusBar(editor);
        actions = new EditorActions(this, editor, statusBar);
        textPanel = new TextPanel(editor, actions);
        ControlPanel controlPanel = new ControlPanel(editor, actions);
        HistoryPanel historyPanel = new HistoryPanel(editor);

        // Observer: всеки панел се записва за промените в редактора и се обновява сам.
        editor.addObserverToAll(textPanel);
        editor.addObserverToAll(actions);
        editor.addObserverToAll(statusBar);
        editor.addObserverToAll(controlPanel);
        editor.addObserverToAll(historyPanel);
        // И самият прозорец: обновява името на файла и "*" в заглавието при незаписани промени.
        editor.addObserverToAll(event -> {
            if (event == EditorEvent.TEXT_CHANGED || event == EditorEvent.STATE_CHANGED) {
                updateTitle();
            }
        });

        // Дясната колона: двете карти една под друга.
        JPanel side = new JPanel(new BorderLayout(0, 14));
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(380, 100));
        side.add(Theme.card("Операции по позиция", controlPanel), BorderLayout.NORTH);
        side.add(Theme.card("История на командите", historyPanel), BorderLayout.CENTER);

        JPanel editorCard = new JPanel(new BorderLayout());
        editorCard.setOpaque(false);
        editorCard.setBorder(BorderFactory.createCompoundBorder(
                new Theme.RoundBorder(Theme.CARD, Theme.BORDER, Theme.RADIUS),
                BorderFactory.createEmptyBorder(8, 4, 8, 4)));
        editorCard.add(textPanel, BorderLayout.CENTER);

        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 18));
        body.add(editorCard, BorderLayout.CENTER);
        body.add(side, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(Theme.BACKGROUND);
        content.add(createHeader(), BorderLayout.NORTH);
        content.add(body, BorderLayout.CENTER);
        setContentPane(content);
        registerShortcuts(content);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1320, 780);
        setMinimumSize(new Dimension(960, 560));
        setLocationRelativeTo(null);
        updateTitle();
    }

    // ---------- Файлове ----------

    public void newDocument() {
        if (isModified() && !confirm("Текущият текст и историята ще бъдат изчистени. Продължавате ли?")) {
            return;
        }
        editor.load("");
        fileName = "Безименен";
        markSaved();
    }

    public void openFile() {
        if (isModified() && !confirm("Незаписаните промени ще бъдат загубени. Продължавате ли?")) {
            return;
        }
        File file = chooseFile("Отваряне на файл", FileDialog.LOAD);
        if (file == null) {
            return;
        }
        try {
            String text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            if (text.startsWith("\uFEFF")) {
                // Премахваме BOM знака, който някои програми (напр. Notepad) слагат в началото на UTF-8 файл.
                text = text.substring(1);
            }
            editor.load(text.replace("\r\n", "\n"));
            fileName = file.getName();
            markSaved();
            statusBar.showInfo("Отворен файл: " + file.getAbsolutePath());
        } catch (IOException e) {
            statusBar.showError("Файлът не може да бъде отворен - " + describe(e, file));
        }
    }

    public void saveFile() {
        File file = chooseFile("Запазване на файл", FileDialog.SAVE);
        if (file == null) {
            return;
        }
        if (!file.getName().contains(".")) {
            file = new File(file.getParentFile(), file.getName() + ".txt");
        }
        try {
            Files.write(file.toPath(), editor.getDocument().getText().getBytes(StandardCharsets.UTF_8));
            fileName = file.getName();
            markSaved();
            statusBar.showInfo("Запазено: " + file.getAbsolutePath());
        } catch (IOException e) {
            statusBar.showError("Файлът не може да бъде запазен - " + describe(e, file));
        }
    }

    /**
     * Показва системния прозорец за избор на файл (на Windows - същият като в Word и Notepad).
     * Започва от последно ползваната папка. Връща null, ако потребителят откаже.
     */
    private File chooseFile(String title, int mode) {
        FileDialog dialog = new FileDialog(this, title, mode);
        if (lastDirectory != null) {
            dialog.setDirectory(lastDirectory);
        }
        if (mode == FileDialog.SAVE) {
            dialog.setFile(fileName.endsWith(".txt") ? fileName : "текст.txt");
        } else if (System.getProperty("os.name").startsWith("Windows")) {
            dialog.setFile("*.txt");   // на Windows това показва само .txt файловете
        }
        dialog.setVisible(true);       // чака, докато потребителят избере файл или затвори прозореца

        focusText();
        if (dialog.getFile() == null) {
            return null;
        }
        lastDirectory = dialog.getDirectory();
        return new File(dialog.getDirectory(), dialog.getFile());
    }

    /**
     * Разбираемо обяснение на грешката. Java дава само пътя като съобщение,
     * затова по вида на грешката казваме какво точно не е наред.
     */
    private static String describe(IOException e, File file) {
        if (e instanceof NoSuchFileException) {
            return "няма такъв файл или папка: " + file.getAbsolutePath();
        }
        if (e instanceof AccessDeniedException) {
            return "няма достъп (папката е защитена или файлът е отворен в друга програма): "
                    + file.getAbsolutePath();
        }
        if (file.isDirectory()) {
            return "това е папка, а не файл: " + file.getAbsolutePath();
        }
        return e.getMessage() + " (" + file.getAbsolutePath() + ")";
    }

    private void markSaved() {
        savedText = editor.getDocument().getText();
        updateTitle();
    }

    private boolean isModified() {
        return !editor.getDocument().getText().equals(savedText);
    }

    /** Името на файла горе и в заглавието на прозореца; "*" в заглавието означава незаписани промени. */
    private void updateTitle() {
        String lock = editor.canEdit() ? "" : "  (само за четене)";
        fileLabel.setText(fileName + lock);
        setTitle((isModified() ? "*" : "") + fileName + " - " + APP_NAME);
    }

    private boolean confirm(String question) {
        return JOptionPane.showConfirmDialog(this, question, APP_NAME, JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    /** Фокусът отива в полето с текста, за да може веднага да се пише (и клавишите да работят). */
    public void focusText() {
        javax.swing.SwingUtilities.invokeLater(() -> textPanel.getTextArea().requestFocusInWindow());
    }

    // ---------- Горна лента ----------

    private JComponent createHeader() {
        JLabel name = new JLabel(APP_NAME);
        name.setFont(Theme.font(17, Font.BOLD));
        name.setForeground(Theme.TEXT);
        fileLabel.setFont(Theme.font(13, Font.PLAIN));
        fileLabel.setForeground(Theme.MUTED);

        // GridBagLayout центрира реда вертикално спрямо лентата с инструментите.
        JPanel brand = new JPanel(new GridBagLayout());
        brand.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 0, 10);
        brand.add(new Logo(), c);
        // "TextPad" и името на файла са в общ панел (подравнени по една линия),
        // а целият панел е центриран вертикално спрямо логото.
        JPanel names = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        names.setOpaque(false);
        ((FlowLayout) names.getLayout()).setAlignOnBaseline(true);
        fileLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        names.add(name);
        names.add(fileLabel);
        c.insets = new Insets(0, 0, 0, 0);
        brand.add(names, c);

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        tools.setOpaque(false);
        tools.add(tool(new JButton(actions.newFile)));
        tools.add(tool(new JButton(actions.open)));
        tools.add(tool(new JButton(actions.save)));
        tools.add(divider());
        tools.add(tool(new JButton(actions.undo)));
        tools.add(tool(new JButton(actions.redo)));
        tools.add(divider());
        tools.add(tool(new JButton(actions.cut)));
        tools.add(tool(new JButton(actions.copy)));
        tools.add(tool(new JButton(actions.paste)));
        tools.add(tool(new JButton(actions.delete)));
        tools.add(divider());
        tools.add(tool(new JButton(actions.mark)));
        tools.add(tool(new JButton(actions.unmark)));
        tools.add(tool(new JToggleButton(actions.readOnly)));

        // Инструментите са в бяла заоблена "капсула".
        JPanel pill = new JPanel(new BorderLayout());
        pill.setOpaque(false);
        pill.setBorder(BorderFactory.createCompoundBorder(
                new Theme.RoundBorder(Theme.CARD, Theme.BORDER, Theme.RADIUS),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        pill.add(tools, BorderLayout.CENTER);

        // Редът с позицията (и съобщенията за грешки) е между името и инструментите.
        statusBar.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 0, 18));
        header.add(brand, BorderLayout.WEST);
        header.add(statusBar, BorderLayout.CENTER);
        header.add(pill, BorderLayout.EAST);
        return header;
    }

    private static AbstractButton tool(AbstractButton button) {
        return Theme.icon(button);
    }

    private static JComponent divider() {
        JPanel line = new JPanel();
        line.setBackground(Theme.BORDER);
        line.setPreferredSize(new Dimension(1, 22));
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 7));
        wrapper.setOpaque(false);
        wrapper.add(line);
        return wrapper;
    }

    /**
     * Клавишите (Ctrl+Z, Ctrl+C...) се свързват с действията за целия прозорец.
     * Полето с текста е изключило своите вградени клавиши, затова те стигат дотук.
     */
    private void registerShortcuts(JComponent root) {
        Action[] all = {actions.newFile, actions.open, actions.save, actions.undo, actions.redo, actions.cut,
                actions.copy, actions.paste, actions.delete, actions.mark, actions.unmark, actions.readOnly};
        for (Action action : all) {
            KeyStroke key = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
            root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, action.getValue(Action.NAME));
            root.getActionMap().put(action.getValue(Action.NAME), action);
        }
    }

    /** Малко лого: заоблен квадрат в акцентния цвят с буквата "T". */
    private static class Logo extends JComponent {
        Logo() {
            setPreferredSize(new Dimension(30, 30));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Theme.smooth(g);
            g2.setColor(Theme.ACCENT);
            g2.fillRoundRect(0, 0, 30, 30, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(Theme.font(16, Font.BOLD));
            int width = g2.getFontMetrics().stringWidth("T");
            g2.drawString("T", (30 - width) / 2, 21);
            g2.dispose();
        }
    }
}
