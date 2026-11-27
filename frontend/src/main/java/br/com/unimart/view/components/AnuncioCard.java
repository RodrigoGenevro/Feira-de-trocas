
package br.com.unimart.view.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Image;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JComponent;
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
import br.com.unimart.util.ImageThumbnailLoader;
import br.com.unimart.util.Theme;

/**
 * Componente visual encapsulado que representa um card de Anuncio na grade.
 */
public class AnuncioCard extends JPanel {

    private static final int CACHE_SIZE = 64;
    private static final int IMAGE_WIDTH = 340;
    private static final int IMAGE_HEIGHT = 150;
    private static final Logger LOGGER = Logger.getLogger(AnuncioCard.class.getName());
    private static final Map<String, ImageIcon> IMAGE_CACHE =
            new LinkedHashMap<>(CACHE_SIZE, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, ImageIcon> eldest) {
                    return size() > CACHE_SIZE;
                }
            };

    private final Anuncio anuncio;
    private final Consumer<Anuncio> onInteresseClick;
    private final Consumer<Anuncio> onDenunciaClick;
    private final boolean permitirInteresse;

    public AnuncioCard(Anuncio anuncio, Consumer<Anuncio> onInteresseClick) {
        this(anuncio, onInteresseClick, null, true);
    }

    public AnuncioCard(Anuncio anuncio, Consumer<Anuncio> onInteresseClick, boolean permitirInteresse) {
        this(anuncio, onInteresseClick, null, permitirInteresse);
    }

    public AnuncioCard(
            Anuncio anuncio,
            Consumer<Anuncio> onInteresseClick,
            Consumer<Anuncio> onDenunciaClick,
            boolean permitirInteresse
    ) {
        this.anuncio = anuncio;
        this.onInteresseClick = onInteresseClick;
        this.onDenunciaClick = onDenunciaClick;
        this.permitirInteresse = permitirInteresse;
        construirCard();
    }

    private void construirCard() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        boolean anuncioProprio = !permitirInteresse;
        setBorder(new LineBorder(
                anuncioProprio ? Theme.GREEN : new Color(0xE5, 0xEB, 0xE8),
                anuncioProprio ? 2 : 1,
                true
        ));
        getAccessibleContext().setAccessibleName("Anúncio: " + anuncio.getTitulo());

        // Topo (Imagem ou placeholder)
        JPanel imgContainer = new JPanel(new BorderLayout());
        imgContainer.setBackground(Theme.PALE);
        imgContainer.setPreferredSize(new Dimension(300, 150));

        JLabel imgLabel = new JLabel("[ Item ]", JLabel.CENTER);
        imgLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        imgLabel.setForeground(Theme.GREEN);
        imgLabel.getAccessibleContext().setAccessibleName("Imagem do anúncio " + anuncio.getTitulo());
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
                (anuncioProprio ? "  SEU ANÚNCIO · " : "  ")
                        + anuncio.getModalidadeFormatada()
                        + " · " + anuncio.getCategoria() + "  "
        );

        pill.setOpaque(true);
        pill.setBackground(anuncioProprio ? Theme.INTRO_BG : Theme.PALE);
        pill.setForeground(anuncioProprio ? Theme.DARK : Theme.GREEN);
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
        desc.getAccessibleContext().setAccessibleName("Descrição do anúncio");

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
                permitirInteresse ? "Tenho interesse" : "Seu anúncio",
                Theme.GREEN,
                Color.WHITE
        );
        btnInteresse.setEnabled(permitirInteresse);
        btnInteresse.setFocusable(true);
        btnInteresse.getAccessibleContext().setAccessibleName(
                permitirInteresse ? "Demonstrar interesse em " + anuncio.getTitulo() : "Anúncio publicado por você"
        );
        if (!permitirInteresse) {
            btnInteresse.setBackground(Theme.PALE);
            btnInteresse.setForeground(Theme.DARK);
            btnInteresse.setToolTipText("Você não pode demonstrar interesse no próprio anúncio.");
        } else {
            btnInteresse.setToolTipText("Demonstrar interesse neste anúncio");
            // Mantém a mesma aparência dos outros botões mesmo desabilitado
            btnInteresse.setDisabledIcon(null);
            btnInteresse.setForeground(Color.WHITE);
            btnInteresse.setBackground(Theme.GREEN);
            btnInteresse.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                @Override
                protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
                    FontMetrics fm = g.getFontMetrics();
                    g.setColor(Color.WHITE);
                    g.setFont(c.getFont());
                    g.drawString(text, textRect.x, textRect.y + fm.getAscent());
                }
            });
        }

        btnInteresse.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnInteresse.setBorder(new EmptyBorder(8, 12, 8, 12));

        btnInteresse.addActionListener(e -> {
            if (onInteresseClick != null) {
                onInteresseClick.accept(anuncio);
            }
        });

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.add(btnInteresse);

        if (onDenunciaClick != null) {
            JButton denunciar = Theme.createButton(
                    "Denunciar",
                    new Color(0xF7E9DE),
                    new Color(0xA05F38)
            );
            denunciar.setFont(new Font("SansSerif", Font.BOLD, 11));
            denunciar.setToolTipText("Denunciar conteúdo inadequado deste anúncio");
            denunciar.addActionListener(e -> onDenunciaClick.accept(anuncio));
            actions.add(Box.createHorizontalStrut(6));
            actions.add(denunciar);
        }

        foot.add(autorLabel, BorderLayout.CENTER);
        foot.add(actions, BorderLayout.EAST);

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
        synchronized (IMAGE_CACHE) {
            ImageIcon cached = IMAGE_CACHE.get(urlStr);
            if (cached != null) {
                targetLabel.setText("");
                targetLabel.setIcon(cached);
                return;
            }
        }

        new SwingWorker<ImageIcon, Void>() {

            @Override
            protected ImageIcon doInBackground() {
                try {
                    return ImageThumbnailLoader.load(urlStr, IMAGE_WIDTH, IMAGE_HEIGHT);
                } catch (IOException | IllegalArgumentException e) {
                    LOGGER.log(Level.FINE, "Não foi possível carregar a imagem do anúncio: " + urlStr, e);
                }

                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();

                    if (icon != null) {
                        synchronized (IMAGE_CACHE) {
                            IMAGE_CACHE.put(urlStr, icon);
                        }
                        targetLabel.setText("");
                        targetLabel.setIcon(icon);
                    } else {
                        targetLabel.setText("Prévia indisponível");
                        targetLabel.setToolTipText("Não foi possível carregar a imagem deste anúncio.");
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.FINE, "Carregamento da imagem interrompido: " + urlStr, e);
                } catch (ExecutionException e) {
                    LOGGER.log(Level.WARNING,
                            "Falha inesperada ao carregar a imagem do anúncio: " + urlStr,
                            e.getCause() != null ? e.getCause() : e);
                }
            }
        }.execute();
    }
}