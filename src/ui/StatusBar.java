package ui;

import editor.Editor;
import observer.EditorEvent;
import observer.Observer;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;

/**
 * Редът с информацията горе, до инструментите: ред, позиция на курсора,
 * брой избрани символи и дължина на текста. Когато има грешка или съобщение
 * (например "Документът е само за четене"), то се показва на същото място в цвят.
 *
 * Наблюдател (Observer): обновява се при всяко събитие от редактора.
 */
public class StatusBar extends JLabel implements Observer {

    private final Editor editor;

    public StatusBar(Editor editor) {
        this.editor = editor;
        setHorizontalAlignment(SwingConstants.RIGHT);
        setFont(Theme.font(12, Font.PLAIN));
        showInfoLine();
    }

    /** Всяка промяна в редактора маха старото съобщение и показва новата позиция. */
    @Override
    public void update(EditorEvent event) {
        showInfoLine();
    }

    public void showError(String text) {
        setForeground(Theme.ERROR);
        setText(text);
        setToolTipText(text);
    }

    public void showInfo(String text) {
        setForeground(Theme.SUCCESS);
        setText(text);
        setToolTipText(text);
    }

    public void clearMessage() {
        showInfoLine();
    }

    private void showInfoLine() {
        // Ред: броим новите редове преди курсора.
        String text = editor.getDocument().getText();
        int caret = Math.min(editor.getCaret(), text.length());
        int line = countLines(text.substring(0, caret));

        setForeground(Theme.MUTED);
        setToolTipText(null);
        setText("Ред " + line
                + "   ·   Позиция " + caret
                + "   ·   Избрани " + editor.getSelectionLength()
                + "   ·   " + text.length() + " символа");
    }

    /** Брой редове = брой знаци за нов ред + 1. */
    private static int countLines(String text) {
        int lines = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lines++;
            }
        }
        return lines;
    }
}
