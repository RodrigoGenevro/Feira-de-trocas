package br.com.unimart.view.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.model.Interesse;
import br.com.unimart.util.Theme;

public class InteressesUsuarioDialog extends javax.swing.JDialog {

    private final FeiraController controller;
    private final JPanel panel = new JPanel();

    public InteressesUsuarioDialog(JFrame owner, FeiraController controller) {
        super(owner, "Meus interesses", true);
        this.controller = controller;
        setSize(620, 500);
        setMinimumSize(new Dimension(520, 400));
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BG_MAIN);
        panel.setBorder(new EmptyBorder(18, 22, 18, 22));
        add(new JScrollPane(panel), BorderLayout.CENTER);
        carregar();
    }

    private void carregar() {
        panel.removeAll();
        panel.add(new JLabel("Carregando interesses...", SwingConstants.CENTER));
        panel.revalidate();
        panel.repaint();
        new SwingWorker<List<Interesse>, Void>() {
            @Override
            protected List<Interesse> doInBackground() throws SQLException {
                return controller.buscarInteressesDoUsuario();
            }

            @Override
            protected void done() {
                try {
                    List<Interesse> interesses = get();
                    panel.removeAll();
                    if (interesses.isEmpty()) {
                        JLabel vazio = new JLabel("Você ainda não demonstrou interesse em anúncios.", SwingConstants.CENTER);
                        vazio.setForeground(Theme.TEXT_MUTED);
                        panel.add(vazio);
                    } else {
                        for (Interesse interesse : interesses) {
                            panel.add(criarCard(interesse));
                            panel.add(Box.createVerticalStrut(10));
                        }
                    }
                    panel.revalidate();
                    panel.repaint();
                } catch (java.util.concurrent.ExecutionException ex) {
                    JOptionPane.showMessageDialog(
                            InteressesUsuarioDialog.this,
                            "Não foi possível carregar seus interesses.",
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private JPanel criarCard(Interesse interesse) {
        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel(interesse.getTituloAnuncio());
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        JLabel status = new JLabel(formatarStatus(interesse.getStatus()));
        status.setForeground(Theme.TEXT_MUTED);
        text.add(titulo);
        text.add(Box.createVerticalStrut(6));
        text.add(status);
        card.add(text, BorderLayout.CENTER);

        if (Interesse.STATUS_PENDENTE.equalsIgnoreCase(interesse.getStatus())) {
            JButton cancelar = Theme.createButton("Cancelar interesse", new Color(0xF7E9DE), new Color(0xA05F38));
            cancelar.addActionListener(event -> {
                int confirmacao = JOptionPane.showConfirmDialog(
                        this,
                        "Cancelar o interesse em \"" + interesse.getTituloAnuncio() + "\"?",
                        "Confirmar cancelamento",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (confirmacao == JOptionPane.YES_OPTION) {
                    cancelar.setEnabled(false);
                    new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws SQLException {
                            controller.cancelarInteresse(interesse.getId());
                            return null;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();
                                carregar();
                            } catch (java.util.concurrent.ExecutionException ex) {
                                Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                                JOptionPane.showMessageDialog(
                                        InteressesUsuarioDialog.this,
                                        "Não foi possível cancelar: " + causa.getMessage(),
                                        "Erro",
                                        JOptionPane.ERROR_MESSAGE
                                );
                                cancelar.setEnabled(true);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }.execute();
                }
            });
            card.add(cancelar, BorderLayout.EAST);
        }
        return card;
    }

    private String formatarStatus(String status) {
        if (Interesse.STATUS_ACEITO.equalsIgnoreCase(status)) return "Aceito";
        if (Interesse.STATUS_ENCERRADO.equalsIgnoreCase(status)) return "Encerrado";
        if (Interesse.STATUS_CANCELADO.equalsIgnoreCase(status)) return "Cancelado";
        return "Pendente";
    }
}
