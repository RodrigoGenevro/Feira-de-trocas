package br.com.unimart.view.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.sql.SQLException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.model.Anuncio;
import br.com.unimart.util.Theme;

public class DenunciaDialog extends BaseDialog {

    public DenunciaDialog(Frame owner, FeiraController controller, Anuncio anuncio) {
        super(owner, "Denunciar anúncio", 460, 420);
        JLabel item = new JLabel("Anúncio: " + anuncio.getTitulo());
        item.setFont(new Font("SansSerif", Font.BOLD, 14));
        item.setForeground(Theme.TEXT_MAIN);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> motivo = new JComboBox<>(new String[]{
                "Conteúdo inadequado",
                "Informação falsa",
                "Produto fora do contexto do campus",
                "Comercialização ou cobrança",
                "Outros"
        });
        Theme.styleComboBox(motivo);
        motivo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea detalhes = new JTextArea(5, 20);
        detalhes.setLineWrap(true);
        detalhes.setWrapStyleWord(true);
        detalhes.setFont(new Font("SansSerif", Font.PLAIN, 14));
        detalhes.getAccessibleContext().setAccessibleName("Detalhes da denúncia");
        JScrollPane detalhesScroll = new JScrollPane(detalhes);
        detalhesScroll.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        detalhesScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton enviar = Theme.createButton("Enviar denúncia", new Color(0xA05F38), Color.WHITE);
        enviar.setAlignmentX(Component.LEFT_ALIGNMENT);
        enviar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        enviar.addActionListener(event -> {
            enviar.setEnabled(false);
            enviar.setText("Enviando...");
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws SQLException {
                    controller.denunciarAnuncio(
                            anuncio.getId(),
                            (String) motivo.getSelectedItem(),
                            detalhes.getText()
                    );
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        dispose();
                        showWarning("Denúncia enviada. Ela será revisada pela equipe responsável.");
                    } catch (java.util.concurrent.ExecutionException ex) {
                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                        showError("Não foi possível enviar a denúncia: " + causa.getMessage());
                        enviar.setEnabled(true);
                        enviar.setText("Enviar denúncia");
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        showError("O envio da denúncia foi interrompido.");
                        enviar.setEnabled(true);
                        enviar.setText("Enviar denúncia");
                    }
                }
            }.execute();
        });

        formPanel.add(createDialogHeader("Denunciar conteúdo"));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(item);
        formPanel.add(Box.createVerticalStrut(14));
        formPanel.add(Theme.createFieldLabel("Motivo *"));
        formPanel.add(motivo);
        formPanel.add(Box.createVerticalStrut(12));
        formPanel.add(Theme.createFieldLabel("Detalhes (opcional)"));
        formPanel.add(detalhesScroll);
        formPanel.add(Box.createVerticalStrut(18));
        formPanel.add(enviar);
    }
}
