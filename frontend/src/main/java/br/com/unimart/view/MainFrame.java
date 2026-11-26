package br.com.unimart.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.model.Anuncio;
import br.com.unimart.util.Theme;
import br.com.unimart.view.components.AnuncioCard;
import br.com.unimart.view.dialogs.InteresseDialog;
import br.com.unimart.view.dialogs.LoginCadastroDialog;
import br.com.unimart.view.dialogs.NovoAnuncioDialog;
import br.com.unimart.view.dialogs.PerfilDialog;
import br.com.unimart.view.dialogs.HistoricoAnunciosDialog;

/**
 * Janela principal da aplicação (Camada View).
 */
public class MainFrame extends JFrame {

    private static final int GRID_COLUMNS = 3;
    private static final int GRID_GAP = 18;

    private final FeiraController controller;

    private JTextField buscaField;
    private JComboBox<String> categoriaCombo;
    private JComboBox<String> modalidadeCombo;
    private JLabel contadorLabel;
    private JPanel gridPanel;
    private JPanel authActionsPanel;
    private final Timer buscaTimer = new Timer(300, e -> atualizarGrid());
    private SwingWorker<List<Anuncio>, Void> gridWorker;

    public MainFrame(FeiraController controller) {
        this.controller = controller;
        buscaTimer.setRepeats(false);
        configurarJanela();
        construirLayout();
        atualizarHeaderAuth();
        atualizarGrid();
    }

    private void configurarJanela() {
        setTitle("UniMart | Feira de trocas");
        setSize(1180, 820);
        setMinimumSize(new Dimension(900, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());
    }

    private void construirLayout() {
        add(createHeader(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(Theme.BG_MAIN);
        mainContent.setBorder(new EmptyBorder(24, 32, 40, 32));

        mainContent.add(createIntroBanner());
        mainContent.add(Box.createVerticalStrut(22));
        mainContent.add(createToolsBar());
        mainContent.add(Box.createVerticalStrut(24));
        mainContent.add(createSectionHeader());
        mainContent.add(Box.createVerticalStrut(14));
        mainContent.add(createGridContainer());

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(Theme.BG_MAIN);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE5, 0xEB, 0xE8)),
                new EmptyBorder(16, 32, 16, 32)
        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        leftPanel.setBackground(Color.WHITE);

        JLabel brand = new JLabel("\u267B UniMart");
        brand.setFont(new Font("SansSerif", Font.BOLD, 24));
        brand.setForeground(Theme.GREEN);

        JLabel subtitle = new JLabel("Feira de trocas e doações da comunidade Senac");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(Theme.TEXT_MUTED);

        leftPanel.add(brand);
        leftPanel.add(subtitle);

        authActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        authActionsPanel.setBackground(Color.WHITE);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(authActionsPanel, BorderLayout.EAST);
        return header;
    }

    public void atualizarHeaderAuth() {
        authActionsPanel.removeAll();

        if (controller.isLogado()) {
            JButton perfilBtn = Theme.createButton("Meu perfil", new Color(0xEE, 0xF2, 0xF0), Theme.TEXT_MAIN);
            perfilBtn.addActionListener(e -> new PerfilDialog(this, controller, this::atualizarHeaderAuth).setVisible(true));

            JButton historicoBtn = Theme.createButton("Meus anúncios", new Color(0xEE, 0xF2, 0xF0), Theme.TEXT_MAIN);
            historicoBtn.addActionListener(e ->
                    new HistoricoAnunciosDialog(this, controller, this::atualizarGrid).setVisible(true)
            );

            JButton sairBtn = Theme.createButton("Sair", new Color(0xF6, 0xF7, 0xF7), Theme.TEXT_MUTED);
            sairBtn.addActionListener(e -> {
                controller.logout();
                setVisible(false);
                new LoginCadastroDialog(
                        null,
                        controller,
                        () -> {
                            atualizarHeaderAuth();
                            atualizarGrid();
                            setVisible(true);
                        },
                        () -> System.exit(0)
                ).setVisible(true);
            });

            authActionsPanel.add(perfilBtn);
            authActionsPanel.add(historicoBtn);
            authActionsPanel.add(sairBtn);
        } else {
            JButton entrarBtn = Theme.createButton("Entrar / Cadastrar", new Color(0xEE, 0xF2, 0xF0), Theme.TEXT_MAIN);
            entrarBtn.addActionListener(e -> abrirLoginECarregar(null));
            authActionsPanel.add(entrarBtn);
        }

        JButton novoBtn = Theme.createButton("+ Publicar anúncio", Theme.GREEN, Color.WHITE);
        novoBtn.addActionListener(e -> executarAcaoAutenticada(() ->
                new NovoAnuncioDialog(this, controller, this::atualizarGrid).setVisible(true)
        ));

        authActionsPanel.add(novoBtn);
        authActionsPanel.revalidate();
        authActionsPanel.repaint();
    }

    private void executarAcaoAutenticada(Runnable acao) {
        if (controller.isLogado()) {
            acao.run();
        } else {
            abrirLoginECarregar(acao);
        }
    }

    private void abrirLoginECarregar(Runnable acaoPosLogin) {
        new LoginCadastroDialog(this, controller, () -> {
            atualizarHeaderAuth();
            atualizarGrid();
            if (acaoPosLogin != null) {
                acaoPosLogin.run();
            }
        }).setVisible(true);
    }

    private JPanel createIntroBanner() {
        JPanel intro = new JPanel();
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        intro.setBackground(Theme.INTRO_BG);
        intro.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCE, 0xE6, 0xDE), 1, true),
                new EmptyBorder(32, 36, 32, 36)
        ));
        intro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        intro.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tag = new JLabel("COMUNIDADE SENAC");
        tag.setFont(new Font("SansSerif", Font.BOLD, 11));
        tag.setForeground(Theme.GREEN);

        JLabel title = new JLabel("<html>Dê uma nova vida<br>ao que você não usa.</html>");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(Theme.DARK);

        JLabel desc = new JLabel("Encontre materiais, compartilhe conhecimento e faça a diferença.");
        desc.setFont(new Font("SansSerif", Font.PLAIN, 15));
        desc.setForeground(new Color(0x56, 0x70, 0x6A));

        intro.add(tag);
        intro.add(Box.createVerticalStrut(10));
        intro.add(title);
        intro.add(Box.createVerticalStrut(10));
        intro.add(desc);

        return intro;
    }

    private JPanel createToolsBar() {
        JPanel tools = new JPanel(new GridBagLayout());
        tools.setBackground(Theme.BG_MAIN);
        tools.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        tools.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        buscaField = new JTextField();
        buscaField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        buscaField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        buscaField.setToolTipText("Buscar livros, materiais, equipamentos...");
        buscaField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                agendarAtualizacaoGrid();
            }
        });

        categoriaCombo = new JComboBox<>(new String[]{
                "Todas as categorias",
                "Livros",
                "Materiais escolares",
                "Eletrônicos",
                "Equipamentos",
                "Outros"
        });
        Theme.styleComboBox(categoriaCombo);
        categoriaCombo.addActionListener(e -> agendarAtualizacaoGrid());

        modalidadeCombo = new JComboBox<>(new String[]{
                "Trocas e doações",
                "Troca",
                "Doação"
        });
        Theme.styleComboBox(modalidadeCombo);
        modalidadeCombo.addActionListener(e -> agendarAtualizacaoGrid());

        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 12);
        tools.add(buscaField, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        categoriaCombo.setPreferredSize(new Dimension(220, 44));
        tools.add(categoriaCombo, gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        modalidadeCombo.setPreferredSize(new Dimension(190, 44));
        tools.add(modalidadeCombo, gbc);

        return tools;
    }

    private JPanel createSectionHeader() {
        JPanel sectionTitle = new JPanel(new BorderLayout());
        sectionTitle.setBackground(Theme.BG_MAIN);
        sectionTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Anúncios disponíveis");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Theme.TEXT_MAIN);

        contadorLabel = new JLabel("0 itens");
        contadorLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        contadorLabel.setForeground(Theme.TEXT_MUTED);

        sectionTitle.add(title, BorderLayout.WEST);
        sectionTitle.add(contadorLabel, BorderLayout.EAST);
        return sectionTitle;
    }

    private JPanel createGridContainer() {
        gridPanel = new JPanel(new GridLayout(0, GRID_COLUMNS, GRID_GAP, GRID_GAP));
        gridPanel.setBackground(Theme.BG_MAIN);
        gridPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return gridPanel;
    }

    public void atualizarGrid() {
        buscaTimer.stop();
        if (gridWorker != null) {
            gridWorker.cancel(true);
        }

        String termoBusca = buscaField.getText();
        String categoria = (String) categoriaCombo.getSelectedItem();
        String modalidade = (String) modalidadeCombo.getSelectedItem();

        gridWorker = new SwingWorker<List<Anuncio>, Void>() {
            @Override
            protected List<Anuncio> doInBackground() throws SQLException {
                return controller.buscarAnuncios(termoBusca, categoria, modalidade);
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    return;
                }

                try {
                    exibirAnuncios(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    exibirErroCarregamento(e);
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    exibirErroCarregamento(causa);
                }
            }
        };
        gridWorker.execute();
    }

    private void exibirAnuncios(List<Anuncio> anuncios) {
        gridPanel.removeAll();
        contadorLabel.setText(anuncios.size() + (anuncios.size() == 1 ? " item" : " itens"));

        if (anuncios.isEmpty()) {
            exibirEstadoVazio();
        } else {
            gridPanel.setLayout(new GridLayout(0, GRID_COLUMNS, GRID_GAP, GRID_GAP));
            for (Anuncio anuncio : anuncios) {
                gridPanel.add(criarCartaoAnuncio(anuncio));
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void exibirEstadoVazio() {
        gridPanel.setLayout(new BorderLayout());
        JLabel empty = new JLabel(
                "Nenhum anúncio encontrado. Tente mudar os filtros ou publique o primeiro!",
                JLabel.CENTER
        );
        empty.setFont(new Font("SansSerif", Font.PLAIN, 15));
        empty.setForeground(Theme.TEXT_MUTED);
        empty.setBorder(new EmptyBorder(50, 20, 50, 20));
        gridPanel.add(empty, BorderLayout.CENTER);
    }

    private AnuncioCard criarCartaoAnuncio(Anuncio anuncio) {
        return new AnuncioCard(anuncio, anuncioSelecionado -> executarAcaoAutenticada(() -> {
            if (controller.anuncioPertenceAoUsuarioLogado(anuncioSelecionado)) {
                JOptionPane.showMessageDialog(this,
                        "Você não pode demonstrar interesse no próprio anúncio.",
                        "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            new InteresseDialog(this, controller, anuncioSelecionado).setVisible(true);
        }),
                !controller.anuncioPertenceAoUsuarioLogado(anuncio));
    }

    private void exibirErroCarregamento(Throwable erro) {
        JOptionPane.showMessageDialog(this,
                "Erro ao carregar anúncios: " + erro.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void agendarAtualizacaoGrid() {
        buscaTimer.restart();
    }
}