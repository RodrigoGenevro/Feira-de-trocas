package br.com.unimart.view.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.util.Theme;

/**
 * Diálogo modal com abas para Login e Cadastro de usuários.
 */
public class LoginCadastroDialog extends JDialog {

    private final FeiraController controller;
    private final Runnable onAuthSuccess;
    private final Runnable onCancel;

    private boolean autenticado = false;

    private JTextField loginEmailField;
    private JPasswordField loginSenhaField;

    private JTextField cadNomeField;
    private JTextField cadEmailField;
    private JTextField cadTurmaField;
    private JPasswordField cadSenhaField;
    private JPasswordField cadConfirmarField;

    public LoginCadastroDialog(JFrame parent, FeiraController controller, Runnable onAuthSuccess) {
        this(parent, controller, onAuthSuccess, null);
    }

    public LoginCadastroDialog(JFrame parent, FeiraController controller, Runnable onAuthSuccess, Runnable onCancel) {
        super(parent, "Acesso UniMart", true);
        this.controller = controller;
        this.onAuthSuccess = onAuthSuccess;
        this.onCancel = onCancel;

        setSize(450, 560);
        setResizable(false);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!autenticado) {
                    dispose();
                    if (onCancel != null) {
                        onCancel.run();
                    }
                }
            }
        });

        construirUI();
    }

    public boolean isAutenticado() {
        return autenticado;
    }

    private void construirUI() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Theme.INTRO_BG);
        header.setBorder(new EmptyBorder(18, 24, 16, 24));

        JLabel title = new JLabel("\u267B Bem-vindo ao UniMart");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Theme.DARK);

        JLabel subtitle = new JLabel("Entre ou crie sua conta para acessar a feira.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(Theme.TEXT_MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.setBackground(Color.WHITE);
        tabs.addTab("Entrar", criarPainelLogin());

        JScrollPane cadastroScroll = new JScrollPane(criarPainelCadastro());
        cadastroScroll.setBorder(null);
        cadastroScroll.getVerticalScrollBar().setUnitIncrement(16);
        cadastroScroll.getViewport().setBackground(Color.WHITE);
        tabs.addTab("Criar conta", cadastroScroll);

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel criarPainelLogin() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(22, 24, 22, 24));

        loginEmailField = criarTextField();
        loginSenhaField = criarPasswordField();

        panel.add(criarLabel("E-mail"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(loginEmailField);
        panel.add(Box.createVerticalStrut(14));

        panel.add(criarLabel("Senha"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(loginSenhaField);
        panel.add(Box.createVerticalStrut(24));

        JButton entrarBtn = Theme.createButton("Entrar", Theme.GREEN, Color.WHITE);
        entrarBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        entrarBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        entrarBtn.addActionListener(e -> executarLogin());

        loginEmailField.addActionListener(e -> executarLogin());
        loginSenhaField.addActionListener(e -> executarLogin());

        panel.add(entrarBtn);
        return panel;
    }

    private JPanel criarPainelCadastro() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 24, 18, 24));

        cadNomeField = criarTextField();
        cadEmailField = criarTextField();
        cadTurmaField = criarTextField();
        cadSenhaField = criarPasswordField();
        cadConfirmarField = criarPasswordField();

        panel.add(criarLabel("Nome completo *"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(cadNomeField);
        panel.add(Box.createVerticalStrut(9));

        panel.add(criarLabel("E-mail *"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(cadEmailField);
        panel.add(Box.createVerticalStrut(9));

        panel.add(criarLabel("Turma / Curso (opcional)"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(cadTurmaField);
        panel.add(Box.createVerticalStrut(9));

        panel.add(criarLabel("Senha (mín. 6 caracteres) *"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(cadSenhaField);
        panel.add(Box.createVerticalStrut(9));

        panel.add(criarLabel("Confirmar senha *"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(cadConfirmarField);
        panel.add(Box.createVerticalStrut(14));

        JButton cadastrarBtn = Theme.createButton("Criar conta e entrar", Theme.GREEN, Color.WHITE);
        cadastrarBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        cadastrarBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        cadastrarBtn.addActionListener(e -> executarCadastro());

        cadNomeField.addActionListener(e -> executarCadastro());
        cadEmailField.addActionListener(e -> executarCadastro());
        cadTurmaField.addActionListener(e -> executarCadastro());
        cadSenhaField.addActionListener(e -> executarCadastro());
        cadConfirmarField.addActionListener(e -> executarCadastro());

        panel.add(cadastrarBtn);
        return panel;
    }

    private void executarLogin() {
        try {
            String email = loginEmailField.getText();
            String senha = new String(loginSenhaField.getPassword());

            controller.login(email, senha);
            autenticado = true;
            dispose();
            if (onAuthSuccess != null) {
                onAuthSuccess.run();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro no banco de dados: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executarCadastro() {
        try {
            String nome = cadNomeField.getText();
            String email = cadEmailField.getText();
            String turma = cadTurmaField.getText();
            String senha = new String(cadSenhaField.getPassword());
            String confirmar = new String(cadConfirmarField.getPassword());

            controller.cadastrar(nome, email, turma, senha, confirmar);
            autenticado = true;
            JOptionPane.showMessageDialog(this, "Conta criada com sucesso!", "UniMart", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (onAuthSuccess != null) {
                onAuthSuccess.run();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Atenção", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao cadastrar usuário: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel criarLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(Theme.TEXT_MAIN);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField criarTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JPasswordField criarPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }
}