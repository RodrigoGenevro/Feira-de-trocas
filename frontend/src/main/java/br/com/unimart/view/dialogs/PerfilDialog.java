package br.com.unimart.view.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.model.Usuario;
import br.com.unimart.util.Theme;

/**
 * Modal para visualizar e alterar os dados do perfil do usuário logado.
 */
public class PerfilDialog extends JDialog {

    private final FeiraController controller;
    private final Runnable onProfileUpdated;

    private JTextField nomeField;
    private JTextField emailField;
    private JTextField turmaField;
    private JPasswordField novaSenhaField;
    private JPasswordField confirmarSenhaField;

    public PerfilDialog(JFrame parent, FeiraController controller, Runnable onProfileUpdated) {
        super(parent, "Meu perfil", true);
        this.controller = controller;
        this.onProfileUpdated = onProfileUpdated;

        setSize(450, 540);
        setResizable(false);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        construirUI();
        preencherDadosAtuais();
    }

    private void construirUI() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Theme.INTRO_BG);
        header.setBorder(new EmptyBorder(18, 24, 16, 24));

        JLabel title = new JLabel("Meu perfil");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Theme.DARK);

        JLabel subtitle = new JLabel("Altere suas informações pessoais quando quiser.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(Theme.TEXT_MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(16, 24, 20, 24));

        nomeField = criarTextField();
        emailField = criarTextField();
        turmaField = criarTextField();
        novaSenhaField = criarPasswordField();
        confirmarSenhaField = criarPasswordField();

        form.add(criarLabel("Nome completo *"));
        form.add(Box.createVerticalStrut(4));
        form.add(nomeField);
        form.add(Box.createVerticalStrut(10));

        form.add(criarLabel("E-mail *"));
        form.add(Box.createVerticalStrut(4));
        form.add(emailField);
        form.add(Box.createVerticalStrut(10));

        form.add(criarLabel("Turma / Curso"));
        form.add(Box.createVerticalStrut(4));
        form.add(turmaField);
        form.add(Box.createVerticalStrut(10));

        form.add(criarLabel("Nova senha (deixe em branco para manter)"));
        form.add(Box.createVerticalStrut(4));
        form.add(novaSenhaField);
        form.add(Box.createVerticalStrut(10));

        form.add(criarLabel("Confirmar nova senha"));
        form.add(Box.createVerticalStrut(4));
        form.add(confirmarSenhaField);
        form.add(Box.createVerticalStrut(18));

        JButton salvarBtn = Theme.createButton("Salvar alterações", Theme.GREEN, Color.WHITE);
        salvarBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        salvarBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        salvarBtn.addActionListener(e -> salvarAlteracoes());

        nomeField.addActionListener(e -> salvarAlteracoes());
        emailField.addActionListener(e -> salvarAlteracoes());
        turmaField.addActionListener(e -> salvarAlteracoes());
        novaSenhaField.addActionListener(e -> salvarAlteracoes());
        confirmarSenhaField.addActionListener(e -> salvarAlteracoes());

        form.add(salvarBtn);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);
    }

    private void preencherDadosAtuais() {
        Usuario u = controller.getUsuarioLogado();
        if (u != null) {
            nomeField.setText(u.getNome());
            emailField.setText(u.getEmail());
            turmaField.setText(u.getTurma() != null ? u.getTurma() : "");
        }
    }

    private void salvarAlteracoes() {
        String novoNome = nomeField.getText();
        String novoEmail = emailField.getText();
        String novaTurma = turmaField.getText();
        String novaSenha = new String(novaSenhaField.getPassword());
        String confirmarSenha = new String(confirmarSenhaField.getPassword());

        new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() throws SQLException {
                return controller.atualizarPerfil(novoNome, novoEmail, novaTurma,
                        novaSenha, confirmarSenha);
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(PerfilDialog.this, "Perfil atualizado com sucesso!",
                            "Trocaê", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    if (onProfileUpdated != null) {
                        onProfileUpdated.run();
                    }
                } catch (java.util.concurrent.ExecutionException ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    if (causa instanceof IllegalArgumentException || causa instanceof IllegalStateException) {
                        JOptionPane.showMessageDialog(PerfilDialog.this, causa.getMessage(),
                                "Atenção", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(PerfilDialog.this,
                                "Erro ao atualizar perfil: " + causa.getMessage(),
                                "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
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