import editor.Editor;
import ui.EditorFrame;
import ui.Theme;

import javax.swing.SwingUtilities;

/**
 * Входна точка: отваря прозореца на редактора (TextPad).
 */
public class Main {

    private static final String SAMPLE_TEXT =
            "Това е примерен текст за курсовата работа по Софтуерни шаблони.\n"
                    + "Пишете направо в полето - всяка буква минава през редактора като команда.\n"
                    + "Изберете текст с мишката или задайте позиция и дължина в панела \"Операции по позиция\".\n"
                    + "Ctrl+M оцветява избрания текст, Ctrl+Z отменя, а Ctrl+R заключва документа (само за четене).";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            Editor editor = new Editor();
            editor.load(SAMPLE_TEXT);
            EditorFrame frame = new EditorFrame(editor);
            frame.setVisible(true);
            frame.focusText();
        });
    }
}
