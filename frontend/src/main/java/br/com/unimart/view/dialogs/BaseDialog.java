package br.com.unimart.view.dialogs;

import br.com.unimart.util.Theme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Superclasse base para os modais (JDialog) da aplicação.
 * Demonstra herança e reutilização de layout padrão na camada View.
 */
public abstract class BaseDialog extends JDialog {

    protected final JPanel formPanel;

    protected BaseDialog(Frame owner, String title, int width, int height) {
        super(owner, title, true);
        setSize(width, height);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JScrollPane scroll = new JScrollPane(formPanel);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    protected JLabel createDialogHeader(String text) {
        JLabel head = new JLabel(text);
        head.setFont(new Font("SansSerif", Font.BOLD, 20));
        head.setForeground(Theme.DARK);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        return head;
    }

    protected void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
