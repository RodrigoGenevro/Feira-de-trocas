package br.com.unimart.view.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.model.Anuncio;
import br.com.unimart.model.Interesse;
import br.com.unimart.util.Theme;

public class InteressesAnuncioDialog extends JDialog {

    private final FeiraController controller;
    private final Anuncio anuncio;
    private final Runnable onUpdated;
    private final JPanel interessesPanel = new JPanel();

    public InteressesAnuncioDialog(
            Dialog parent, FeiraController controller, Anuncio anuncio, Runnable onUpdated
    ) {
        super(parent, "Interesses recebidos", true);
        this.controller = controller;
        this.anuncio = anuncio;
        this.onUpdated = onUpdated;

        setSize(560, 480);
        setMinimumSize(new Dimension(480, 360));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());

        construirInterface();
        carregarInteresses();
    }

    private void construirInterface() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Theme.INTRO_BG);
        header.setBorder(new EmptyBorder(20, 24, 18, 24));

        JLabel title = new JLabel("Interesses recebidos");
        title.setFont(new Font("SansSerif", Font.BOLD, 21));
        title.setForeground(Theme.DARK);

        JLabel subtitle = new JLabel(anuncio.getTitulo());
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(Theme.TEXT_MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        interessesPanel.setLayout(new BoxLayout(interessesPanel, BoxLayout.Y_AXIS));
        interessesPanel.setBackground(Theme.BG_MAIN);
        interessesPanel.setBorder(new EmptyBorder(18, 22, 18, 22));

        JScrollPane scroll = new JScrollPane(interessesPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(Theme.BG_MAIN);
        add(scroll, BorderLayout.CENTER);
    }

    private void carregarInteresses() {
        interessesPanel.removeAll();
        JLabel loading = new JLabel("Carregando interesses...", SwingConstants.CENTER);
        loading.setForeground(Theme.TEXT_MUTED);
        interessesPanel.add(loading);
        interessesPanel.revalidate();
        interessesPanel.repaint();

        new SwingWorker<List<Interesse>, Void>() {
            @Override
            protected List<Interesse> doInBackground() throws SQLException {
                return controller.buscarInteressesDoAnuncio(anuncio.getId());
            }

            @Override
            protected void done() {
                try {
                    List<Interesse> interesses = get();
                    interessesPanel.removeAll();
                    if (interesses.isEmpty()) {
                        interessesPanel.add(criarEstadoVazio());
                    } else {
                        for (Interesse interesse : interesses) {
                            interessesPanel.add(criarCard(interesse));
                            interessesPanel.add(Box.createVerticalStrut(10));
                        }
                    }
                    interessesPanel.revalidate();
                    interessesPanel.repaint();
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    interessesPanel.removeAll();
                    JLabel error = new JLabel("Não foi possível carregar os interesses.", SwingConstants.CENTER);
                    error.setForeground(Theme.TEXT_MUTED);
                    interessesPanel.add(error);
                    interessesPanel.revalidate();
                    interessesPanel.repaint();
                    exibirErro("Não foi possível carregar os interesses", causa);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    interessesPanel.removeAll();
                    JLabel error = new JLabel("O carregamento dos interesses foi interrompido.", SwingConstants.CENTER);
                    error.setForeground(Theme.TEXT_MUTED);
                    interessesPanel.add(error);
                    interessesPanel.revalidate();
                    interessesPanel.repaint();
                    exibirErro("O carregamento dos interesses foi interrompido", e);
                }
            }
        }.execute();
    }

    private JPanel criarCard(Interesse interesse) {
        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setBackground(Color.WHITE);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 176));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(13, 15, 13, 15)
        ));

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(interesse.getNomeInteressado());
        name.setFont(new Font("SansSerif", Font.BOLD, 15));
        name.setForeground(Theme.TEXT_MAIN);

        JTextArea message = new JTextArea(interesse.getMensagem(), 2, 32);
        message.setFont(new Font("SansSerif", Font.PLAIN, 13));
        message.setForeground(Theme.TEXT_MUTED);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setEditable(false);
        message.setOpaque(false);
        message.setFocusable(false);
        message.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JLabel status = new JLabel(formatarStatus(interesse.getStatus()));
        status.setFont(new Font("SansSerif", Font.BOLD, 11));
        status.setForeground(corStatus(interesse.getStatus()));

        details.add(name);
        details.add(Box.createVerticalStrut(7));
        details.add(message);
        details.add(Box.createVerticalStrut(7));
        details.add(status);
        card.add(details, BorderLayout.CENTER);

        if (Interesse.STATUS_PENDENTE.equalsIgnoreCase(interesse.getStatus())
                && Anuncio.STATUS_DISPONIVEL.equalsIgnoreCase(anuncio.getStatus())) {
            JButton aceitar = Theme.createButton("Aceitar e reservar", Theme.GREEN, Color.WHITE);
            aceitar.setFont(new Font("SansSerif", Font.BOLD, 11));
            aceitar.addActionListener(e -> aceitarInteresse(interesse, aceitar));
            card.add(aceitar, BorderLayout.SOUTH);
        }

        return card;
    }

    private JPanel criarEstadoVazio() {
        JPanel empty = new JPanel(new BorderLayout());
        empty.setBackground(Color.WHITE);
        empty.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(28, 22, 28, 22)
        ));

        JLabel message = new JLabel(
                "Ainda não há interesse recebido para este anúncio.",
                SwingConstants.CENTER
        );
        message.setFont(new Font("SansSerif", Font.PLAIN, 14));
        message.setForeground(Theme.TEXT_MUTED);
        empty.add(message, BorderLayout.CENTER);
        return empty;
    }

    private void aceitarInteresse(Interesse interesse, JButton aceitarButton) {
        int resposta = JOptionPane.showConfirmDialog(
                this,
                "Aceitar o interesse de " + interesse.getNomeInteressado()
                        + " e marcar o anúncio como reservado?",
                "Confirmar reserva",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        aceitarButton.setEnabled(false);
        aceitarButton.setText("Reservando...");
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws SQLException {
                controller.aceitarInteresse(interesse.getId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (onUpdated != null) {
                        onUpdated.run();
                    }
                    dispose();
                } catch (java.util.concurrent.ExecutionException e) {
                    exibirErro("Não foi possível aceitar o interesse", e.getCause());
                    aceitarButton.setEnabled(true);
                    aceitarButton.setText("Aceitar e reservar");
                    carregarInteresses();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    aceitarButton.setEnabled(true);
                    aceitarButton.setText("Aceitar e reservar");
                    exibirErro("A reserva do anúncio foi interrompida", e);
                }
            }
        }.execute();
    }

    private String formatarStatus(String status) {
        if (Interesse.STATUS_ACEITO.equalsIgnoreCase(status)) {
            return "Aceito";
        }
        if (Interesse.STATUS_ENCERRADO.equalsIgnoreCase(status)) {
            return "Encerrado";
        }
        if (Interesse.STATUS_CANCELADO.equalsIgnoreCase(status)) {
            return "Reserva cancelada";
        }
        return "Pendente";
    }

    private Color corStatus(String status) {
        return Interesse.STATUS_ACEITO.equalsIgnoreCase(status)
                ? Theme.GREEN : Theme.TEXT_MUTED;
    }

    private void exibirErro(String mensagem, Throwable causa) {
        String detalhe = causa != null && causa.getMessage() != null
                ? ": " + causa.getMessage()
                : ".";
        JOptionPane.showMessageDialog(
                this,
                mensagem + detalhe,
                "Erro",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
