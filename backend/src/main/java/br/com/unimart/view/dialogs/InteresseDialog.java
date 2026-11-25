package br.com.unimart.view.dialogs;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.model.Anuncio;
import br.com.unimart.util.Theme;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modal de registro de interesse em um anúncio. Herda de BaseDialog.
 */
public class InteresseDialog extends BaseDialog {

    private final FeiraController controller;
    private final Anuncio anuncio;

    public InteresseDialog(Frame owner, FeiraController controller, Anuncio anuncio) {
        super(owner, "Tenho interesse", 440, 360);
        this.controller = controller;
        this.anuncio = anuncio;
        construirFormulario();
    }

    private void construirFormulario() {
        JLabel sub = new JLabel("Item: " + anuncio.getTitulo());
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(Theme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nomeField = Theme.createInputField();
        JTextArea msgArea = new JTextArea(4, 20);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane msgScroll = new JScrollPane(msgArea);
        msgScroll.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        msgScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(createDialogHeader("Tenho interesse"));
        formPanel.add(Box.createVerticalStrut(4));
        formPanel.add(sub);
        formPanel.add(Box.createVerticalStrut(14));
        formPanel.add(Theme.createFieldLabel("Seu nome *"));
        formPanel.add(nomeField);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(Theme.createFieldLabel("Mensagem (como gostaria de combinar) *"));
        formPanel.add(msgScroll);
        formPanel.add(Box.createVerticalStrut(18));

        JButton enviarBtn = Theme.createButton("Enviar interesse", Theme.GREEN, Color.WHITE);
        enviarBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        enviarBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        enviarBtn.addActionListener(e -> {
            try {
                controller.enviarInteresse(
                        anuncio.getId(),
                        nomeField.getText(),
                        msgArea.getText()
                );
                dispose();
                JOptionPane.showMessageDialog(getOwner(), "Interesse enviado com sucesso!");
            } catch (IllegalArgumentException ex) {
                showWarning(ex.getMessage());
            } catch (Exception ex) {
                showError("Não foi possível enviar: " + ex.getMessage());
            }
        });

        formPanel.add(enviarBtn);
    }
}
