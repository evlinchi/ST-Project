package ui;

import javax.swing.Icon;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Иконите на лентата с инструменти: тънки едноцветни линии (в модерен стил).
 * Рисуват се с код, затова проектът няма нужда от файлове с картинки.
 * Всяка се рисува в квадрат 24x24 и се мащабира до нужния размер.
 */
public final class Icons {

    private static final Color LINE = new Color(70, 76, 94);
    private static final Color ACCENT = Theme.ORANGE;

    private Icons() {
    }

    /** Нещо, което знае как да нарисува икона в квадрат 24x24. */
    private interface Painter {
        void paint(Graphics2D g);
    }

    private static Icon icon(int size, Painter painter) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                g2.translate(x, y);
                g2.scale(size / 24.0, size / 24.0);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(LINE);
                if (c != null && !c.isEnabled()) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                }
                painter.paint(g2);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    public static Icon newFile(int size) {
        return icon(size, g -> {
            page(g);
            g.draw(line(12, 11, 12, 17));
            g.draw(line(9, 14, 15, 14));
        });
    }

    public static Icon open(int size) {
        return icon(size, g -> {
            Path2D folder = new Path2D.Double();
            folder.moveTo(3, 7);
            folder.lineTo(3, 18);
            folder.quadTo(3, 19, 4, 19);
            folder.lineTo(20, 19);
            folder.quadTo(21, 19, 21, 18);
            folder.lineTo(21, 9);
            folder.quadTo(21, 8, 20, 8);
            folder.lineTo(12, 8);
            folder.lineTo(10, 5);
            folder.lineTo(4, 5);
            folder.quadTo(3, 5, 3, 6);
            folder.closePath();
            g.draw(folder);
        });
    }

    public static Icon save(int size) {
        return icon(size, g -> {
            Path2D body = new Path2D.Double();
            body.moveTo(5, 4);
            body.lineTo(16, 4);
            body.lineTo(20, 8);
            body.lineTo(20, 19);
            body.quadTo(20, 20, 19, 20);
            body.lineTo(5, 20);
            body.quadTo(4, 20, 4, 19);
            body.lineTo(4, 5);
            body.quadTo(4, 4, 5, 4);
            body.closePath();
            g.draw(body);
            g.draw(new RoundRectangle2D.Double(8, 13, 8, 7, 1, 1));
            g.draw(line(8, 4, 8, 8));
            g.draw(line(8, 8, 14, 8));
            g.draw(line(14, 8, 14, 4));
        });
    }

    public static Icon cut(int size) {
        return icon(size, g -> {
            g.draw(new java.awt.geom.Ellipse2D.Double(4, 15, 5, 5));
            g.draw(new java.awt.geom.Ellipse2D.Double(15, 15, 5, 5));
            g.draw(line(8.2, 15.8, 17, 4));
            g.draw(line(15.8, 15.8, 7, 4));
        });
    }

    public static Icon copy(int size) {
        return icon(size, g -> {
            g.draw(new RoundRectangle2D.Double(9, 9, 11, 11, 4, 4));
            Path2D back = new Path2D.Double();
            back.moveTo(15, 9);
            back.lineTo(15, 6);
            back.quadTo(15, 4, 13, 4);
            back.lineTo(6, 4);
            back.quadTo(4, 4, 4, 6);
            back.lineTo(4, 13);
            back.quadTo(4, 15, 6, 15);
            back.lineTo(9, 15);
            g.draw(back);
        });
    }

    /** "+" в кръг - за Постави. */
    public static Icon plus(int size) {
        return icon(size, g -> {
            g.draw(new java.awt.geom.Ellipse2D.Double(3, 3, 18, 18));
            g.draw(line(12, 8, 12, 16));
            g.draw(line(8, 12, 16, 12));
        });
    }

    /** "-" в кръг - за Изтрий. */
    public static Icon minus(int size) {
        return icon(size, g -> {
            g.draw(new java.awt.geom.Ellipse2D.Double(3, 3, 18, 18));
            g.draw(line(8, 12, 16, 12));
        });
    }

    public static Icon undo(int size) {
        return icon(size, g -> arrow(g, false));
    }

    public static Icon redo(int size) {
        return icon(size, g -> arrow(g, true));
    }

    public static Icon mark(int size) {
        return icon(size, g -> {
            // Маркер под ъгъл и оранжева черта под него.
            Path2D pen = new Path2D.Double();
            pen.moveTo(14, 4);
            pen.lineTo(19, 9);
            pen.lineTo(11, 17);
            pen.lineTo(8, 17);
            pen.lineTo(7, 16);
            pen.lineTo(7, 13);
            pen.closePath();
            g.draw(pen);
            g.draw(line(7, 16, 5, 18));
            g.setColor(ACCENT);
            g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(line(4, 21, 20, 21));
        });
    }

    public static Icon unmark(int size) {
        return icon(size, g -> {
            // Гума.
            Path2D eraser = new Path2D.Double();
            eraser.moveTo(14, 4);
            eraser.lineTo(20, 10);
            eraser.lineTo(11, 19);
            eraser.lineTo(7, 19);
            eraser.lineTo(4, 16);
            eraser.closePath();
            g.draw(eraser);
            g.draw(line(9, 9, 15, 15));
            g.draw(line(11, 19, 20, 19));
        });
    }

    public static Icon readOnly(int size) {
        return icon(size, g -> {
            g.draw(new RoundRectangle2D.Double(5, 11, 14, 10, 4, 4));
            Path2D shackle = new Path2D.Double();
            shackle.moveTo(8, 11);
            shackle.lineTo(8, 8);
            shackle.curveTo(8, 3, 16, 3, 16, 8);
            shackle.lineTo(16, 11);
            g.draw(shackle);
            g.draw(line(12, 15, 12, 17));
        });
    }

    // ---------- Помощни рисунки ----------

    private static void page(Graphics2D g) {
        Path2D sheet = new Path2D.Double();
        sheet.moveTo(6, 3);
        sheet.lineTo(14, 3);
        sheet.lineTo(19, 8);
        sheet.lineTo(19, 20);
        sheet.quadTo(19, 21, 18, 21);
        sheet.lineTo(6, 21);
        sheet.quadTo(5, 21, 5, 20);
        sheet.lineTo(5, 4);
        sheet.quadTo(5, 3, 6, 3);
        sheet.closePath();
        g.draw(sheet);
        g.draw(line(14, 3, 14, 8));
        g.draw(line(14, 8, 19, 8));
    }

    private static void arrow(Graphics2D g, boolean redo) {
        Graphics2D a = (Graphics2D) g.create();
        if (redo) {
            a.translate(24, 0);
            a.scale(-1, 1);
        }
        Path2D path = new Path2D.Double();
        path.moveTo(5, 9);
        path.lineTo(15, 9);
        path.curveTo(21, 9, 21, 19, 15, 19);
        path.lineTo(9, 19);
        a.draw(path);
        a.draw(line(5, 9, 9, 5));
        a.draw(line(5, 9, 9, 13));
        a.dispose();
    }

    private static java.awt.geom.Line2D line(double x1, double y1, double x2, double y2) {
        return new java.awt.geom.Line2D.Double(x1, y1, x2, y2);
    }
}
