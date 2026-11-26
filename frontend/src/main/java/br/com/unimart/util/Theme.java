
package br.com.unimart.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public final class Theme {

    public static final Color GREEN = new Color(0x16, 0x7D, 0x70);
    public static final Color DARK = new Color(0x16, 0x3D, 0x3B);
    public static final Color PALE = new Color(0xE8, 0xF5, 0xF1);
    public static final Color INTRO_BG = new Color(0xDF, 0xF1, 0xEB);
    public static final Color BG_MAIN = new Color(0xF6, 0xF8, 0xF7);
    public static final Color TEXT_MAIN = new Color(0x20, 0x33, 0x31);
    public static final Color TEXT_MUTED = new Color(0x71, 0x81, 0x7E);
    public static final Color BORDER_COLOR = new Color(0xD9, 0xE2, 0xDF);

    private Theme() {
    }

    public static JButton createButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBackground(bg);
        button.setForeground(fg);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBorder(new EmptyBorder(11, 18, 11, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    public static JTextField createInputField() {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        return field;
    }

    public static void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT_MAIN);
    }
}