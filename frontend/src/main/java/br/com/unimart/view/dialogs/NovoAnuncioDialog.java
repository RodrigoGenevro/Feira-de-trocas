package br.com.unimart.view.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.model.Anuncio;
import br.com.unimart.model.Usuario;
import br.com.unimart.util.Theme;

/**
 * Modal de cadastro de novo anúncio. Herda de BaseDialog.
 */
public class NovoAnuncioDialog extends BaseDialog {

    private final FeiraController controller;
    private final Runnable onSuccessCallback;

    public NovoAnuncioDialog(Frame owner, FeiraController controller, Runnable onSuccessCallback) {
        super(owner, "Novo anúncio", 480, 560);
        this.controller = controller;
        this.onSuccessCallback = onSuccessCallback;
        construirFormulario();
    }

    private void construirFormulario() {
        Usuario logado = controller.getUsuarioLogado();

        JTextField nomeField = Theme.createInputField();
        JTextField turmaField = Theme.createInputField();

        if (logado != null) {
            nomeField.setText(logado.getNome());
            nomeField.setEditable(false);
            nomeField.setBackground(new Color(0xF4, 0xF6, 0xF5));

            if (logado.getTurma() != null && !logado.getTurma().isBlank()) {
                turmaField.setText(logado.getTurma());
                turmaField.setEditable(false);
                turmaField.setBackground(new Color(0xF4, 0xF6, 0xF5));
            }
        }

        JTextField tituloField = Theme.createInputField();

        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> catBox = new JComboBox<>(new String[]{
                "Livros", "Materiais escolares", "Eletrônicos", "Equipamentos", "Outros"
        });
        Theme.styleComboBox(catBox);
        catBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> modBox = new JComboBox<>(new String[]{"TROCA", "DOACAO"});
        Theme.styleComboBox(modBox);
        modBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField fotoField = Theme.createInputField();

        formPanel.add(createDialogHeader("Novo anúncio"));
        formPanel.add(Box.createVerticalStrut(14));
        formPanel.add(Theme.createFieldLabel("Seu nome *"));
        formPanel.add(nomeField);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(Theme.createFieldLabel("Turma (opcional)"));
        formPanel.add(turmaField);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(Theme.createFieldLabel("Título do item *"));
        formPanel.add(tituloField);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(Theme.createFieldLabel("Descrição *"));
        formPanel.add(descScroll);
        formPanel.add(Box.createVerticalStrut(10));

        JPanel twoCols = new JPanel(new GridLayout(1, 2, 12, 0));
        twoCols.setBackground(Color.WHITE);
        twoCols.setAlignmentX(Component.LEFT_ALIGNMENT);
        twoCols.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JPanel col1 = new JPanel();
        col1.setLayout(new BoxLayout(col1, BoxLayout.Y_AXIS));
        col1.setBackground(Color.WHITE);
        col1.add(Theme.createFieldLabel("Categoria"));
        col1.add(catBox);

        JPanel col2 = new JPanel();
        col2.setLayout(new BoxLayout(col2, BoxLayout.Y_AXIS));
        col2.setBackground(Color.WHITE);
        col2.add(Theme.createFieldLabel("Modalidade"));
        col2.add(modBox);

        twoCols.add(col1);
        twoCols.add(col2);
        formPanel.add(twoCols);

        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(Theme.createFieldLabel("Link da foto *"));
        fotoField.setToolTipText("Informe um link público de imagem iniciado por http:// ou https://.");
        formPanel.add(fotoField);
        formPanel.add(Box.createVerticalStrut(18));

        JButton submitBtn = Theme.createButton("Publicar anúncio", Theme.GREEN, Color.WHITE);
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        submitBtn.addActionListener(e -> {
            String nome = nomeField.getText();
            String turma = turmaField.getText();
            String titulo = tituloField.getText();
            String descricao = descArea.getText();
            String categoria = (String) catBox.getSelectedItem();
            String modalidade = (String) modBox.getSelectedItem();
            String foto = fotoField.getText();

            new SwingWorker<Anuncio, Void>() {
                @Override
                protected Anuncio doInBackground() throws Exception {
                    return controller.criarAnuncio(nome, turma, titulo, descricao,
                            categoria, modalidade, foto);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        dispose();
                        if (onSuccessCallback != null) {
                            onSuccessCallback.run();
                        }
                    } catch (java.util.concurrent.ExecutionException ex) {
                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                        if (causa instanceof IllegalArgumentException) {
                            showWarning(causa.getMessage());
                        } else {
                            showError("Não foi possível publicar o anúncio: " + causa.getMessage());
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }.execute();
        });

        formPanel.add(submitBtn);
    }
}