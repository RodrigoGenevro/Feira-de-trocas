package br.com.unimart.view.dialogs;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.util.Theme;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

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
        JTextField nomeField = Theme.createInputField();
        JTextField turmaField = Theme.createInputField();
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
        formPanel.add(Theme.createFieldLabel("Link da foto (opcional)"));
        formPanel.add(fotoField);
        formPanel.add(Box.createVerticalStrut(18));

        JButton submitBtn = Theme.createButton("Publicar anúncio", Theme.GREEN, Color.WHITE);
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        submitBtn.addActionListener(e -> {
            try {
                controller.criarAnuncio(
                        nomeField.getText(),
                        turmaField.getText(),
                        tituloField.getText(),
                        descArea.getText(),
                        (String) catBox.getSelectedItem(),
                        (String) modBox.getSelectedItem(),
                        fotoField.getText()
                );
                dispose();
                if (onSuccessCallback != null) {
                    onSuccessCallback.run();
                }
            } catch (IllegalArgumentException ex) {
                showWarning(ex.getMessage());
            } catch (Exception ex) {
                showError("Não foi possível publicar o anúncio: " + ex.getMessage());
            }
        });

        formPanel.add(submitBtn);
    }
}
