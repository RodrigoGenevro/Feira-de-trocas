package br.com.unimart.view.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.net.URI;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import br.com.unimart.model.Anuncio;
import br.com.unimart.util.Theme;

/**
 * Componente visual encapsulado que representa um card de Anuncio na grade.
 */
public class AnuncioCard extends JPanel {

    private final Anuncio anuncio;
    private final Consumer<Anuncio> onInteresseClick;

    public AnuncioCard(Anuncio anuncio, Consumer<Anuncio> onInteresseClick) {
        this.anuncio = anuncio;
        this.onInteresseClick = onInteresseClick;
        construirCard();
    }

    private void construirCard() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new LineBorder(new Color(0xE5, 0xEB, 0xE8), 1, true));

        // Topo (Imagem ou placeholder)
        JPanel imgContainer = new JPanel(new BorderLayout());
        imgContainer.setBackground(Theme.PALE);
        imgContainer.setPreferredSize(new Dimension(300, 150));

        JLabel imgLabel = new JLabel("[ Item ]", JLabel.CENTER);
        imgLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        imgLabel.setForeground(Theme.GREEN);
        imgContainer.add(imgLabel, BorderLayout.CENTER);

        if (anuncio.getFotoUrl() != null && !anuncio.getFotoUrl().isBlank()) {
            carregarImagemAssincrona(anuncio.getFotoUrl(), imgLabel);
        }

        // Corpo do Card
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel pill = new JLabel(
                "  " + anuncio.getModalidadeFormatada()
                        + " · " + anuncio.getCategoria() + "  "
        );

        pill.setOpaque(true);
        pill.setBackground(Theme.PALE);
        pill.setForeground(Theme.GREEN);
        pill.setFont(new Font("SansSerif", Font.BOLD, 11));
        pill.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titulo = new JLabel(anuncio.getTitulo());
        titulo.setFont(new Font("SansSerif", Font.BOLD, 17));
        titulo.setForeground(Theme.TEXT_MAIN);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea desc = new JTextArea(anuncio.getDescricao());
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        desc.setForeground(Theme.TEXT_MUTED);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setEditable(false);
        desc.setBackground(Color.WHITE);
        desc.setRows(3);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Rodapé do Card
        JPanel foot = new JPanel(new BorderLayout(8, 0));
        foot.setBackground(Color.WHITE);
        foot.setAlignmentX(Component.LEFT_ALIGNMENT);
        foot.setBorder(new EmptyBorder(12, 0, 0, 0));

        JLabel autorLabel = new JLabel(anuncio.getAutorCompleto());
        autorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        autorLabel.setForeground(Theme.TEXT_MUTED);

        // Botão com a mesma cor do botão principal
        JButton btnInteresse = Theme.createButton(
                "Tenho interesse",
                Theme.GREEN,
                Color.WHITE
        );

        btnInteresse.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnInteresse.setBorder(new EmptyBorder(8, 12, 8, 12));

        btnInteresse.addActionListener(e -> {
            if (onInteresseClick != null) {
                onInteresseClick.accept(anuncio);
            }
        });

        foot.add(autorLabel, BorderLayout.CENTER);
        foot.add(btnInteresse, BorderLayout.EAST);

        body.add(pill);
        body.add(Box.createVerticalStrut(8));
        body.add(titulo);
        body.add(Box.createVerticalStrut(6));
        body.add(desc);
        body.add(Box.createVerticalGlue());
        body.add(foot);

        add(imgContainer, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    private void carregarImagemAssincrona(String urlStr, JLabel targetLabel) {
        new SwingWorker<ImageIcon, Void>() {

            @Override
            protected ImageIcon doInBackground() {
                try {
                    URL url = URI.create(urlStr).toURL();
                    ImageIcon raw = new ImageIcon(url);

                    if (raw.getIconWidth() > 0) {
                        Image scaled = raw.getImage().getScaledInstance(
                                340,
                                150,
                                Image.SCALE_SMOOTH
                        );

                        return new ImageIcon(scaled);
                    }

                } catch (Exception ignored) {
                }

                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();

                    if (icon != null) {
                        targetLabel.setText("");
                        targetLabel.setIcon(icon);
                    }

                } catch (Exception ignored) {
                }
            }
        }.execute();
    }
}