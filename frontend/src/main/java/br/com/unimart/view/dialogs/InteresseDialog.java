package br.com.unimart.view.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
        Usuario logado = controller.getUsuarioLogado();

        JLabel sub = new JLabel("Item: " + anuncio.getTitulo());
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(Theme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nomeField = Theme.createInputField();
        if (logado != null) {
            nomeField.setText(logado.getNome());
            nomeField.setEditable(false);
            nomeField.setBackground(new Color(0xF4, 0xF6, 0xF5));
        }

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
            String nome = nomeField.getText();
            String mensagem = msgArea.getText();

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    controller.enviarInteresse(anuncio.getId(), nome, mensagem);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        dispose();
                        JOptionPane.showMessageDialog(getOwner(), "Interesse enviado com sucesso!");
                    } catch (java.util.concurrent.ExecutionException ex) {
                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                        if (causa instanceof IllegalArgumentException) {
                            showWarning(causa.getMessage());
                        } else {
                            showError("Não foi possível enviar: " + causa.getMessage());
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }.execute();
        });

        formPanel.add(enviarBtn);
    }
}