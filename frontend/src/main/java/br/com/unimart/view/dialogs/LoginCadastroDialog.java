package br.com.unimart.view.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.model.Usuario;
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
    private JButton entrarBtn;
    private JButton cadastrarBtn;
    private boolean autenticacaoEmProgresso;

    private JComboBox<String> loginContaCombo;
    private final Map<String, String> emailsPorConta = new HashMap<>();

    public LoginCadastroDialog(JFrame parent, FeiraController controller, Runnable onAuthSuccess) {
        this(parent, controller, onAuthSuccess, null);
    }

    public LoginCadastroDialog(JFrame parent, FeiraController controller, Runnable onAuthSuccess, Runnable onCancel) {
        super(parent, "Trocaê | Acesse sua conta", true);
        this.controller = controller;
        this.onAuthSuccess = onAuthSuccess;
        this.onCancel = onCancel;

        setSize(940, 620);
        setMinimumSize(new Dimension(820, 560));
        setResizable(true);
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
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        JPanel welcomePanel = criarPainelBoasVindas();
        welcomePanel.setPreferredSize(new Dimension(390, 0));
        content.add(welcomePanel, BorderLayout.WEST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.setBackground(Color.WHITE);
        tabs.addTab("Entrar", criarPainelLogin());

        JScrollPane cadastroScroll = new JScrollPane(criarPainelCadastro());
        cadastroScroll.setBorder(null);
        cadastroScroll.getVerticalScrollBar().setUnitIncrement(16);
        cadastroScroll.getViewport().setBackground(Color.WHITE);
        tabs.addTab("Criar conta", cadastroScroll);

        JPanel accessPanel = new JPanel(new BorderLayout());
        accessPanel.setBackground(Color.WHITE);
        accessPanel.setBorder(new EmptyBorder(38, 38, 30, 38));

        JPanel accessHeader = new JPanel();
        accessHeader.setLayout(new BoxLayout(accessHeader, BoxLayout.Y_AXIS));
        accessHeader.setBackground(Color.WHITE);
        accessHeader.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Acesse sua conta");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(Theme.DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Entre ou crie sua conta para continuar.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(Theme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        accessHeader.add(title);
        accessHeader.add(Box.createVerticalStrut(7));
        accessHeader.add(subtitle);

        accessPanel.add(accessHeader, BorderLayout.NORTH);
        accessPanel.add(tabs, BorderLayout.CENTER);
        content.add(accessPanel, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel criarPainelBoasVindas() {
        JPanel panel = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D graphics2D = (Graphics2D) graphics.create();
                graphics2D.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );
                graphics2D.setPaint(new GradientPaint(
                        0, 0, Theme.DARK,
                        getWidth(), getHeight(), Theme.GREEN
                ));
                graphics2D.fillRect(0, 0, getWidth(), getHeight());
                graphics2D.setColor(new Color(255, 255, 255, 22));
                graphics2D.fillOval(getWidth() - 150, -90, 260, 260);
                graphics2D.fillOval(-130, getHeight() - 170, 300, 300);
                graphics2D.dispose();
            }
        };
        panel.setBorder(new EmptyBorder(42, 38, 36, 34));
        panel.setOpaque(false);

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brandPanel.setOpaque(false);
        JLabel brand = new JLabel("\u267B Trocaê");
        brand.setFont(new Font("SansSerif", Font.BOLD, 25));
        brand.setForeground(Color.WHITE);
        brandPanel.add(brand);
        panel.add(brandPanel, BorderLayout.NORTH);

        JPanel messagePanel = new JPanel();
        messagePanel.setOpaque(false);
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("<html>Bem-vindo<br>à Trocaê!</html>");
        title.setFont(new Font("SansSerif", Font.BOLD, 34));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "<html>Encontre o que precisa, compartilhe o que não usa "
                        + "e faça bons itens circularem pela comunidade.</html>"
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitle.setForeground(Theme.INTRO_BG);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setMaximumSize(new Dimension(310, 90));

        messagePanel.add(title);
        messagePanel.add(Box.createVerticalStrut(18));
        messagePanel.add(subtitle);
        panel.add(messagePanel, BorderLayout.CENTER);

        JLabel footer = new JLabel("TROQUE  \u2022  DOE  \u2022  RECIRCULE");
        footer.setFont(new Font("SansSerif", Font.BOLD, 11));
        footer.setForeground(Theme.INTRO_BG);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel criarPainelLogin() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(22, 24, 22, 24));

        loginEmailField = criarTextField();
        loginSenhaField = criarPasswordField();

        loginContaCombo = new JComboBox<>();
        loginContaCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        loginContaCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        loginContaCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginContaCombo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
        loginContaCombo.addActionListener(e -> {
            String selecao = (String) loginContaCombo.getSelectedItem();
            if (selecao == null || selecao.startsWith("Selecione") || selecao.startsWith("Outro")) {
                if (selecao != null && selecao.startsWith("Outro")) {
                    loginEmailField.setText("");
                    loginEmailField.requestFocusInWindow();
                }
                return;
            }

            String email = emailsPorConta.get(selecao);
            if (email != null) {
                loginEmailField.setText(email);
                loginSenhaField.requestFocusInWindow();
            }
        });
        carregarContasRapidas();

        panel.add(criarLabel("E-mail"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(loginEmailField);
        panel.add(Box.createVerticalStrut(4));

        panel.add(criarLabel("Conta rápida"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(loginContaCombo);
        panel.add(Box.createVerticalStrut(14));

        panel.add(criarLabel("Senha"));
        panel.add(Box.createVerticalStrut(6));
        panel.add(loginSenhaField);
        panel.add(Box.createVerticalStrut(24));

        entrarBtn = Theme.createButton("Entrar", Theme.GREEN, Color.WHITE);
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

        cadastrarBtn = Theme.createButton("Criar conta e entrar", Theme.GREEN, Color.WHITE);
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
        if (autenticacaoEmProgresso) {
            return;
        }
        atualizarEstadoAutenticacao(true, false);
        String email = loginEmailField.getText();
        String senha = new String(loginSenhaField.getPassword());

        new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() throws SQLException {
                return controller.login(email, senha);
            }

            @Override
            protected void done() {
                try {
                    get();
                    autenticado = true;
                    dispose();
                    if (onAuthSuccess != null) {
                        onAuthSuccess.run();
                    }
                } catch (java.util.concurrent.ExecutionException ex) {
                    atualizarEstadoAutenticacao(false, false);
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    if (causa instanceof IllegalArgumentException) {
                        JOptionPane.showMessageDialog(LoginCadastroDialog.this, causa.getMessage(),
                                "Atenção", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(LoginCadastroDialog.this,
                                "Erro no banco de dados: " + causa.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    atualizarEstadoAutenticacao(false, false);
                    JOptionPane.showMessageDialog(LoginCadastroDialog.this,
                            "A tentativa de login foi interrompida.",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void executarCadastro() {
        if (autenticacaoEmProgresso) {
            return;
        }
        atualizarEstadoAutenticacao(true, true);
        String nome = cadNomeField.getText();
        String email = cadEmailField.getText();
        String turma = cadTurmaField.getText();
        String senha = new String(cadSenhaField.getPassword());
        String confirmar = new String(cadConfirmarField.getPassword());

        new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() throws SQLException {
                return controller.cadastrar(nome, email, turma, senha, confirmar);
            }

            @Override
            protected void done() {
                try {
                    get();
                    autenticado = true;
                    JOptionPane.showMessageDialog(LoginCadastroDialog.this, "Conta criada com sucesso!",
                            "Trocaê", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    if (onAuthSuccess != null) {
                        onAuthSuccess.run();
                    }
                } catch (java.util.concurrent.ExecutionException ex) {
                    atualizarEstadoAutenticacao(false, true);
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    if (causa instanceof IllegalArgumentException) {
                        JOptionPane.showMessageDialog(LoginCadastroDialog.this, causa.getMessage(),
                                "Atenção", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(LoginCadastroDialog.this,
                                "Erro ao cadastrar usuário: " + causa.getMessage(),
                                "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    atualizarEstadoAutenticacao(false, true);
                    JOptionPane.showMessageDialog(LoginCadastroDialog.this,
                            "A criação da conta foi interrompida.",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void atualizarEstadoAutenticacao(boolean emProgresso, boolean cadastro) {
        autenticacaoEmProgresso = emProgresso;
        entrarBtn.setEnabled(!emProgresso);
        cadastrarBtn.setEnabled(!emProgresso);
        entrarBtn.setText(emProgresso && !cadastro ? "Entrando..." : "Entrar");
        cadastrarBtn.setText(emProgresso && cadastro ? "Criando conta..." : "Criar conta e entrar");
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

    private void carregarContasRapidas() {
        loginContaCombo.removeAllItems();
        emailsPorConta.clear();
        loginContaCombo.addItem("Selecione uma conta cadastrada");
        loginContaCombo.addItem("Carregando contas...");

        new SwingWorker<List<Usuario>, Void>() {
            @Override
            protected List<Usuario> doInBackground() throws SQLException {
                return controller.buscarUsuariosCadastrados();
            }

            @Override
            protected void done() {
                try {
                    List<Usuario> usuarios = get();
                    loginContaCombo.removeAllItems();
                    emailsPorConta.clear();
                    loginContaCombo.addItem("Selecione uma conta cadastrada");

                    if (usuarios.isEmpty()) {
                        loginContaCombo.addItem("Nenhuma conta cadastrada");
                    } else {
                        for (Usuario usuario : usuarios) {
                            String descricao = usuario.getNome() + " — " + usuario.getEmail();
                            emailsPorConta.put(descricao, usuario.getEmail());
                            loginContaCombo.addItem(descricao);
                        }
                    }
                    loginContaCombo.addItem("Outro e-mail");
                } catch (java.util.concurrent.ExecutionException ex) {
                    loginContaCombo.removeAllItems();
                    loginContaCombo.addItem("Selecione uma conta cadastrada");
                    loginContaCombo.addItem("Outro e-mail");
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(LoginCadastroDialog.this,
                            "Não foi possível carregar o histórico de contas: " + causa.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }
}