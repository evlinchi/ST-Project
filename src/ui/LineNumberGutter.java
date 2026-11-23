package ui;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;

/**
 * Колоната с номерата на редовете вляво от текста.
 * Слага се като "row header" на JScrollPane, затова се превърта заедно с текста.
 * Номерът на реда с курсора е в акцентния цвят.
 */
public class LineNumberGutter extends JComponent {

    private static final Color BACKGROUND = Color.WHITE;
    private static final Color NUMBER = new Color(184, 188, 202);
    private static final Color CURRENT_NUMBER = Theme.ACCENT;
    private static final int PADDING = 12;

    private final JTextArea textArea;

    public LineNumberGutter(JTextArea textArea) {
        this.textArea = textArea;
        setOpaque(true);

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
        });
        textArea.addCaretListener(e -> repaint());
        textArea.addPropertyChangeListener("font", e -> changed());
        textArea.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // При пренасяне на редове височината на текста се сменя.
                changed();
            }
        });
    }

    private void changed() {
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics metrics = getFontMetrics(numberFont());
        int digits = Math.max(3, String.valueOf(textArea.getLineCount()).length());
        int width = metrics.charWidth('0') * digits + 2 * PADDING;
        return new Dimension(width, textArea.getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle clip = g2.getClipBounds();
        g2.setColor(BACKGROUND);
        g2.fillRect(clip.x, clip.y, clip.width, clip.height);

        Font font = numberFont();
        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics();
        FontMetrics textMetrics = textArea.getFontMetrics(textArea.getFont());

        try {
            // Рисуваме само редовете, които се виждат в момента.
            int startOffset = textArea.viewToModel2D(new Point(0, clip.y));
            int endOffset = textArea.viewToModel2D(new Point(0, clip.y + clip.height));
            int firstLine = textArea.getLineOfOffset(startOffset);
            int lastLine = textArea.getLineOfOffset(endOffset);
            int caretLine = textArea.getLineOfOffset(Math.min(textArea.getCaretPosition(),
                    textArea.getDocument().getLength()));

            for (int line = firstLine; line <= lastLine; line++) {
                Rectangle2D row = textArea.modelToView2D(textArea.getLineStartOffset(line));
                if (row == null) {
                    continue;
                }
                String number = String.valueOf(line + 1);
                int x = getWidth() - PADDING - metrics.stringWidth(number);
                // Подравняваме по базовата линия на текста, за да са номерата точно срещу редовете.
                int y = (int) row.getY() + textMetrics.getAscent();
                g2.setColor(line == caretLine ? CURRENT_NUMBER : NUMBER);
                g2.drawString(number, x, y);
            }
        } catch (BadLocationException e) {
            // Позициите идват от самото текстово поле, така че са винаги валидни.
        }
        g2.dispose();
    }

    private Font numberFont() {
        Font font = textArea.getFont();
        return font.deriveFont(Math.max(9f, font.getSize2D() - 1));
    }
}
