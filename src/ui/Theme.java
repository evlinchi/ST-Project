package ui;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.List;

/**
 * Темата на приложението: цветове, шрифтове и заоблени елементи за модерен, мек вид.
 * Всичко е със стандартния Swing, без външни библиотеки.
 */
public final class Theme {

    // Цветове: мек пастел - синьо (основно) и оранжево (рамки, маркер, посочване)
    public static final Color BACKGROUND = new Color(244, 245, 249);
    public static final Color CARD = Color.WHITE;
    public static final Color BORDER = new Color(228, 230, 238);
    public static final Color TEXT = new Color(38, 42, 56);
    public static final Color MUTED = new Color(128, 134, 150);
    public static final Color ACCENT = new Color(76, 127, 214);          // синьо
    public static final Color ACCENT_SOFT = new Color(230, 238, 251);
    public static final Color ORANGE = new Color(242, 160, 97);          // оранжево
    public static final Color ORANGE_BORDER = new Color(246, 202, 165);
    public static final Color ORANGE_SOFT = new Color(255, 243, 232);
    public static final Color ERROR = new Color(214, 69, 80);
    public static final Color SUCCESS = new Color(40, 160, 100);

    public static final int RADIUS = 14;

    /** Шрифтът на интерфейса: Nunito от папката ui/fonts, а ако не се зареди - Segoe UI. */
    private static final String UI_FONT = loadNunito()
            ? "Nunito" : pick("Segoe UI", "Inter", "Noto Sans", "DejaVu Sans");
    private static final String CODE_FONT = pick("Cascadia Mono", "Consolas", "JetBrains Mono", "DejaVu Sans Mono");

    private Theme() {
    }

    /** Слага общите настройки на Swing. Вика се веднъж, преди да се създаде прозорецът. */
    public static void install() {
        try {
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            // Остава стандартният изглед.
        }
        Font font = font(13, Font.PLAIN);
        for (String key : new String[]{"Label.font", "Button.font", "ToggleButton.font", "TextField.font",
                "TextArea.font", "List.font", "OptionPane.messageFont", "OptionPane.buttonFont", "ToolTip.font",
                "FileChooser.listFont", "ComboBox.font"}) {
            UIManager.put(key, font);
        }
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", CARD);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TextField.selectionBackground", ACCENT_SOFT);
        UIManager.put("TextArea.selectionBackground", ACCENT_SOFT);
        UIManager.put("ToolTip.background", TEXT);
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.border", BorderFactory.createEmptyBorder(5, 8, 5, 8));
    }

    public static Font font(int size, int style) {
        if ("Nunito".equals(UI_FONT)) {
            // Удебеленият и наклоненият вариант са отделни файлове, затова ги избираме по име.
            String face = style == Font.BOLD ? "Nunito Bold" : style == Font.ITALIC ? "Nunito Italic" : "Nunito Regular";
            return new Font(face, Font.PLAIN, size);
        }
        return new Font(UI_FONT, style, size);
    }

    public static Font codeFont(int size) {
        return new Font(CODE_FONT, Font.PLAIN, size);
    }

    // ---------- Карти ----------

    /** Бяла заоблена карта със заглавие отгоре. */
    public static JPanel card(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(CARD, BORDER, RADIUS),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        if (title != null) {
            JLabel label = new JLabel(title);
            label.setFont(font(14, Font.BOLD));
            label.setForeground(TEXT);
            card.add(label, BorderLayout.NORTH);
        }
        content.setOpaque(false);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // ---------- Бутони ----------

    /** Основен бутон: запълнен със синьо. */
    public static <T extends AbstractButton> T primary(T button) {
        T styled = styled(button, ACCENT, ACCENT.darker(), Color.WHITE, ACCENT, null);
        styled.setFont(font(14, Font.BOLD));
        return styled;
    }

    /** Бутон само с икона: тънка оранжева рамка, оранжев фон при посочване, син при включен. */
    public static <T extends AbstractButton> T icon(T button) {
        styled(button, null, ORANGE_SOFT, TEXT, ACCENT_SOFT, ORANGE_BORDER);
        button.setHideActionText(true);
        button.setPreferredSize(new Dimension(36, 36));
        return button;
    }

    private static <T extends AbstractButton> T styled(T button, Color normal, Color hover, Color text,
                                                       Color selected, Color border) {
        button.setUI(new SoftButtonUI(normal, hover, selected, border));
        button.setForeground(text);
        button.setFont(font(13, Font.PLAIN));
        button.setFocusable(false);
        button.setRolloverEnabled(true);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        return button;
    }

    // ---------- Полета и скролбари ----------

    /** Заоблена рамка за полета за въвеждане. */
    public static Border fieldBorder() {
        return BorderFactory.createCompoundBorder(new RoundBorder(null, BORDER, 10),
                BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    /** Тънки заоблени скролбари без стрелки, без рамка около полето. */
    public static JScrollPane softScroll(JScrollPane scroll) {
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUI(new SoftScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new SoftScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 10));
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(10, 10));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getHorizontalScrollBar().setOpaque(false);
        return scroll;
    }

    // ---------- Помощни класове ----------

    /** Рамка със заоблени ъгли, която по желание запълва и фона. */
    public static class RoundBorder extends AbstractBorder {
        private final Color fill;
        private final Color line;
        private final int radius;

        public RoundBorder(Color fill, Color line, int radius) {
            this.fill = fill;
            this.line = line;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = smooth(g);
            if (fill != null) {
                g2.setColor(fill);
                g2.fillRoundRect(x, y, width - 1, height - 1, radius, radius);
            }
            g2.setColor(line);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
    }

    /**
     * Рисува бутона като заоблен правоъгълник с рамка: нормален, при посочване, натиснат/избран и изключен.
     * Ползва се и за JToggleButton (катинарът "Само за четене").
     */
    private static class SoftButtonUI extends BasicButtonUI {
        private final Color normal;
        private final Color hover;
        private final Color selected;
        private final Color border;

        SoftButtonUI(Color normal, Color hover, Color selected, Color border) {
            this.normal = normal;
            this.hover = hover;
            this.selected = selected;
            this.border = border;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton button = (AbstractButton) c;
            ButtonModel model = button.getModel();
            Color background = normal;
            if (model.isSelected()) {
                background = selected;
            } else if (model.isEnabled() && (model.isPressed() || model.isRollover())) {
                background = model.isPressed() && normal != null ? normal.darker() : hover;
            }
            if (!model.isEnabled() && normal != null) {
                background = new Color(normal.getRed(), normal.getGreen(), normal.getBlue(), 110);
            }
            Graphics2D g2 = smooth(g);
            if (background != null) {
                g2.setColor(background);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);
            }
            if (border != null) {
                // Рамката е по-бледа, когато бутонът е изключен, и по-ярко оранжева при посочване или включен.
                Color line = !model.isEnabled() ? new Color(border.getRed(), border.getGreen(), border.getBlue(), 90)
                        : (model.isRollover() || model.isSelected()) ? ORANGE : border;
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.setColor(line);
                g2.drawRoundRect(1, 1, c.getWidth() - 3, c.getHeight() - 3, 11, 11);
            }
            g2.dispose();
            super.paint(g, c);
        }

        @Override
        protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
            Graphics2D g2 = smooth(g);
            g2.setFont(b.getFont());
            Color color = b.getForeground();
            g2.setColor(b.isEnabled() ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), 120));
            g2.drawString(text, textRect.x, textRect.y + g2.getFontMetrics().getAscent());
            g2.dispose();
        }

        @Override
        protected void paintButtonPressed(Graphics g, AbstractButton b) {
            // Натиснатото състояние вече е нарисувано в paint().
        }

        @Override
        protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect,
                                  Rectangle iconRect) {
            // Без рамка за фокус - бутоните не взимат фокуса.
        }
    }

    /** Тънък заоблен плъзгач без стрелки в краищата. */
    private static class SoftScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            // Прозрачна пътечка.
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) {
                return;
            }
            Graphics2D g2 = smooth(g);
            g2.setColor(isThumbRollover() ? new Color(176, 180, 196) : new Color(208, 211, 222));
            boolean vertical = scrollbar.getOrientation() == JScrollBar.VERTICAL;
            int arc = vertical ? r.width - 4 : r.height - 4;
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, arc, arc);
            g2.dispose();
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return zeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return zeroButton();
        }

        private static JButton zeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }
    }

    /**
     * Зарежда Nunito (свободен шрифт, лиценз OFL - виж ui/fonts/OFL.txt) и го регистрира в Java,
     * за да може да се ползва с new Font("Nunito", ...). Връща false, ако файловете липсват.
     */
    private static boolean loadNunito() {
        boolean loaded = false;
        for (String file : new String[]{"Nunito-Regular.ttf", "Nunito-Bold.ttf", "Nunito-Italic.ttf"}) {
            try (java.io.InputStream in = Theme.class.getResourceAsStream("fonts/" + file)) {
                if (in != null) {
                    Font font = Font.createFont(Font.TRUETYPE_FONT, in);
                    GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                    loaded = true;
                }
            } catch (Exception e) {
                // Без този файл остава резервният шрифт.
            }
        }
        return loaded;
    }

    static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    private static String pick(String... names) {
        List<String> available = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String name : names) {
            if (available.contains(name)) {
                return name;
            }
        }
        return Font.DIALOG;
    }
}
