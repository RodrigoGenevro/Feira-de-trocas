package br.com.unimart.view.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.model.Anuncio;
import br.com.unimart.util.Theme;

public class HistoricoAnunciosDialog extends JDialog {

    private final FeiraController controller;
    private JPanel resumoPanel;
    private JPanel anunciosPanel;

    public HistoricoAnunciosDialog(JFrame parent, FeiraController controller) {
        super(parent, "Meu histórico de anúncios", true);
        this.controller = controller;

        setSize(900, 620);
        setResizable(true);
        setMinimumSize(new Dimension(720, 500));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());

        construirUI();
        carregarHistorico();
    }

    private void construirUI() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(0xCFE8DE), getWidth(), 0, new Color(0xEAF5F1)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setOpaque(true);
        header.setBorder(new EmptyBorder(24, 30, 22, 30));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel tag = new JLabel("MINHA ATIVIDADE");
        tag.setFont(new Font("SansSerif", Font.BOLD, 11));
        tag.setForeground(Theme.GREEN);

        JLabel title = new JLabel("Meu histórico de anúncios");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(Theme.DARK);

        JLabel subtitle = new JLabel("Acompanhe tudo que você publicou na comunidade UniMart.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(Theme.TEXT_MUTED);

        textPanel.add(tag);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitle);
        header.add(textPanel, BorderLayout.CENTER);

        JLabel icon = new JLabel("♻", SwingConstants.CENTER);
        icon.setPreferredSize(new Dimension(74, 74));
        icon.setFont(new Font("SansSerif", Font.BOLD, 38));
        icon.setForeground(Color.WHITE);
        icon.setOpaque(true);
        icon.setBackground(Theme.GREEN);
        icon.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        header.add(icon, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBackground(Theme.BG_MAIN);
        main.setBorder(new EmptyBorder(18, 28, 24, 28));

        resumoPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        resumoPanel.setOpaque(false);
        resumoPanel.setPreferredSize(new Dimension(0, 92));

        main.add(resumoPanel, BorderLayout.NORTH);

        anunciosPanel = new JPanel();
        anunciosPanel.setLayout(new BoxLayout(anunciosPanel, BoxLayout.Y_AXIS));
        anunciosPanel.setBackground(Theme.BG_MAIN);

        JScrollPane scroll = new JScrollPane(anunciosPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getViewport().setBackground(Theme.BG_MAIN);
        main.add(scroll, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);
    }

    private void carregarHistorico() {
        new SwingWorker<List<Anuncio>, Void>() {
            @Override
            protected List<Anuncio> doInBackground() throws SQLException {
                return controller.buscarHistoricoUsuario();
            }

            @Override
            protected void done() {
                try {
                    List<Anuncio> anuncios = get();
                    int disponiveis = 0;
                    int reservados = 0;
                    int concluidos = 0;
                    for (Anuncio anuncio : anuncios) {
                        if (Anuncio.STATUS_CONCLUIDO.equalsIgnoreCase(anuncio.getStatus())) {
                            concluidos++;
                        } else if (Anuncio.STATUS_RESERVADO.equalsIgnoreCase(anuncio.getStatus())) {
                            reservados++;
                        } else {
                            disponiveis++;
                        }
                    }

                    resumoPanel.removeAll();
                    resumoPanel.add(criarResumo("TOTAL", String.valueOf(anuncios.size()), "anúncios publicados", Theme.DARK));
                    resumoPanel.add(criarResumo("DISPONÍVEIS", String.valueOf(disponiveis), "recebendo interesse", Theme.GREEN));
                    resumoPanel.add(criarResumo("RESERVADOS", String.valueOf(reservados), "em negociação", new Color(0x397A68)));
                    resumoPanel.add(criarResumo("CONCLUÍDOS", String.valueOf(concluidos), "finalizados", new Color(0xB56A35)));

                    anunciosPanel.removeAll();
                    if (anuncios.isEmpty()) {
                        anunciosPanel.add(criarEstadoVazio());
                    } else {
                        for (Anuncio anuncio : anuncios) {
                            anunciosPanel.add(criarCardAnuncio(anuncio));
                            anunciosPanel.add(Box.createVerticalStrut(12));
                        }
                    }

                    resumoPanel.revalidate();
                    resumoPanel.repaint();
                    anunciosPanel.revalidate();
                    anunciosPanel.repaint();
                } catch (java.util.concurrent.ExecutionException ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(HistoricoAnunciosDialog.this,
                            "Erro ao carregar histórico: " + causa.getMessage(),
                            "Erro", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private JPanel criarResumo(String titulo, String valor, String descricao, Color cor) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(0xDEE7E3), 1, true),
                new EmptyBorder(14, 16, 12, 16)
        ));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(titulo);
        title.setFont(new Font("SansSerif", Font.BOLD, 10));
        title.setForeground(Theme.TEXT_MUTED);

        JLabel number = new JLabel(valor);
        number.setFont(new Font("SansSerif", Font.BOLD, 26));
        number.setForeground(cor);

        JLabel desc = new JLabel(descricao);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(Theme.TEXT_MUTED);

        text.add(title);
        text.add(number);
        text.add(desc);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel criarCardAnuncio(Anuncio anuncio) {
        JPanel card = new JPanel(new BorderLayout(16, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 176));
        card.setPreferredSize(new Dimension(0, 176));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel icone = new JLabel(anuncio.getModalidadeFormatada().equals("DOAÇÃO") ? "♥" : "⇄", SwingConstants.CENTER);
        icone.setPreferredSize(new Dimension(58, 58));
        icone.setOpaque(true);
        icone.setBackground(anuncio.getModalidadeFormatada().equals("DOAÇÃO")
                ? new Color(0xF7E8DE) : new Color(0xDFF1EA));
        icone.setForeground(anuncio.getModalidadeFormatada().equals("DOAÇÃO")
                ? new Color(0xB56A35) : Theme.GREEN);
        icone.setFont(new Font("SansSerif", Font.BOLD, 24));
        card.add(icone, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel(anuncio.getTitulo());
        titulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        titulo.setForeground(Theme.TEXT_MAIN);

        JLabel detalhes = new JLabel(anuncio.getCategoria() + "  •  " + anuncio.getModalidadeFormatada());
        detalhes.setFont(new Font("SansSerif", Font.PLAIN, 13));
        detalhes.setForeground(Theme.TEXT_MUTED);

        JLabel data = new JLabel("Publicado em " + (anuncio.getCriadoEm() != null ? anuncio.getCriadoEm() : "data indisponível"));
        data.setFont(new Font("SansSerif", Font.PLAIN, 12));
        data.setForeground(Theme.TEXT_MUTED);

        info.add(titulo);
        info.add(Box.createVerticalStrut(7));
        info.add(detalhes);
        info.add(Box.createVerticalStrut(5));
        info.add(data);
        card.add(info, BorderLayout.CENTER);

        boolean concluido = Anuncio.STATUS_CONCLUIDO.equalsIgnoreCase(anuncio.getStatus());
        boolean reservado = Anuncio.STATUS_RESERVADO.equalsIgnoreCase(anuncio.getStatus());
        String textoStatus = concluido ? "✓ Concluído" : reservado ? "● Reservado" : "● Disponível";
        JLabel status = new JLabel(textoStatus, SwingConstants.CENTER);
        status.setOpaque(true);
        status.setPreferredSize(new Dimension(118, 32));
        status.setFont(new Font("SansSerif", Font.BOLD, 12));
        status.setForeground(concluido ? new Color(0xA05F38) : Theme.GREEN);
        status.setBackground(concluido ? new Color(0xF7E9DE) : new Color(0xDFF1EA));
        status.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(176, 150));
        status.setAlignmentX(Component.CENTER_ALIGNMENT);
        right.add(status);

        JButton interessesBtn = Theme.createButton(
                "Ver interesses",
                new Color(0xEE, 0xF2, 0xF0),
                Theme.TEXT_MAIN
        );
        interessesBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        interessesBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        interessesBtn.addActionListener(e ->
                new InteressesAnuncioDialog(this, controller, anuncio, this::carregarHistorico).setVisible(true)
        );
        right.add(Box.createVerticalStrut(5));
        right.add(interessesBtn);

        if (!concluido) {
            JButton statusBtn = Theme.createButton(
                    reservado ? "Reabrir anúncio" : "Encerrar anúncio",
                    reservado ? new Color(0xEE, 0xF2, 0xF0) : new Color(0xF7, 0xE9, 0xDE),
                    reservado ? Theme.TEXT_MAIN : new Color(0xA05F38)
            );
            statusBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            statusBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            statusBtn.addActionListener(e -> atualizarStatusAnuncio(anuncio, reservado));
            right.add(Box.createVerticalStrut(5));
            right.add(statusBtn);

            if (reservado) {
                JButton concluirBtn = Theme.createButton(
                        "Concluir anúncio",
                        new Color(0xF7, 0xE9, 0xDE),
                        new Color(0xA0, 0x5F, 0x38)
                );
                concluirBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                concluirBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                concluirBtn.addActionListener(e -> atualizarStatusAnuncio(anuncio, false));
                right.add(Box.createVerticalStrut(5));
                right.add(concluirBtn);
            }
        }

        card.add(right, BorderLayout.EAST);

        return card;
    }

    private void atualizarStatusAnuncio(Anuncio anuncio, boolean reabrir) {
        String acao;
        if (reabrir) {
            acao = "reabrir";
        } else if (Anuncio.STATUS_RESERVADO.equalsIgnoreCase(anuncio.getStatus())) {
            acao = "concluir";
        } else {
            acao = "encerrar";
        }
        int resposta = JOptionPane.showConfirmDialog(
                this,
                "Deseja " + acao + " o anúncio \"" + anuncio.getTitulo() + "\"?",
                "Confirmar alteração",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws SQLException {
                if (reabrir) {
                    controller.reabrirAnuncio(anuncio.getId());
                } else {
                    controller.concluirAnuncio(anuncio.getId());
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    carregarHistorico();
                } catch (java.util.concurrent.ExecutionException e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(
                            HistoricoAnunciosDialog.this,
                            "Não foi possível " + acao + " o anúncio: " + causa.getMessage(),
                            "Erro",
                            JOptionPane.ERROR_MESSAGE
                    );
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private JPanel criarEstadoVazio() {
        JPanel empty = new JPanel();
        empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
        empty.setBackground(Color.WHITE);
        empty.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(48, 30, 48, 30)
        ));
        empty.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel icon = new JLabel("✦", SwingConstants.CENTER);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        icon.setFont(new Font("SansSerif", Font.BOLD, 36));
        icon.setForeground(Theme.GREEN);

        JLabel title = new JLabel("Seu histórico está esperando o primeiro anúncio", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(Theme.DARK);

        JLabel subtitle = new JLabel("Publique um item e ele aparecerá aqui.", SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(Theme.TEXT_MUTED);

        empty.add(icon);
        empty.add(Box.createVerticalStrut(10));
        empty.add(title);
        empty.add(Box.createVerticalStrut(6));
        empty.add(subtitle);
        return empty;
    }
}
