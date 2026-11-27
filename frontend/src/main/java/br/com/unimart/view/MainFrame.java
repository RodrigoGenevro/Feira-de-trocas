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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JComboBox;
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
import br.com.unimart.model.PaginaAnuncios;
import br.com.unimart.util.Theme;
import br.com.unimart.view.components.AnuncioCard;
import br.com.unimart.view.dialogs.InteresseDialog;
import br.com.unimart.view.dialogs.LoginCadastroDialog;
import br.com.unimart.view.dialogs.NovoAnuncioDialog;
import br.com.unimart.view.dialogs.HistoricoAnunciosDialog;
import br.com.unimart.view.dialogs.PerfilDialog;

/**
 * Janela principal da aplicação (Camada View).
 */
public class MainFrame extends JFrame {

    private static final int GRID_COLUMNS = 3;
    private static final int GRID_GAP = 18;
    private static final int MIN_CARD_WIDTH = 300;
    private static final int PAGE_SIZE = 12;

    private final FeiraController controller;

    private JTextField buscaField;
    private JComboBox<String> categoriaCombo;
    private JComboBox<String> modalidadeCombo;
    private JComboBox<String> ordenacaoCombo;
    private JLabel contadorLabel;
    private JLabel paginaLabel;
    private JPanel gridPanel;
    private JPanel authActionsPanel;
    private JPanel activeFiltersPanel;
    private JButton paginaAnteriorBtn;
    private JButton proximaPaginaBtn;
    private JButton databaseBtn;
    private int paginaAtual;
    private final Timer buscaTimer = new Timer(300, e -> atualizarGrid());
    private SwingWorker<PaginaAnuncios, Void> gridWorker;

    public MainFrame(FeiraController controller) {
        this.controller = controller;
        buscaTimer.setRepeats(false);
        configurarJanela();
        construirLayout();
        atualizarHeaderAuth();
        atualizarGrid();
    }

    private void configurarJanela() {
        setTitle("Trocaê | Feira de trocas");
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
        mainContent.add(Box.createVerticalStrut(12));
        mainContent.add(createPaginationControls());

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(Theme.BG_MAIN);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE5, 0xEB, 0xE8)),
                new EmptyBorder(16, 32, 16, 32)
        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel brand = new JLabel("\u267B Trocaê");
        brand.setFont(new Font("SansSerif", Font.BOLD, 24));
        brand.setForeground(Theme.GREEN);

        JLabel subtitle = new JLabel("Feira de trocas e doações da comunidade Senac");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(Theme.TEXT_MUTED);

        leftPanel.add(brand);
        leftPanel.add(subtitle);

        authActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        authActionsPanel.setBackground(Color.WHITE);
        authActionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        authActionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        header.add(leftPanel);
        header.add(Box.createVerticalStrut(12));
        header.add(authActionsPanel);
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

        databaseBtn = Theme.createButton("Banco de dados", new Color(0xEE, 0xF2, 0xF0), Theme.TEXT_MAIN);
        databaseBtn.setToolTipText("Os dados compartilhados são armazenados no Supabase");
        databaseBtn.getAccessibleContext().setAccessibleName("Opções do banco de dados");
        databaseBtn.addActionListener(e -> abrirOpcoesBanco());
        authActionsPanel.add(databaseBtn);

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
        JPanel tools = new JPanel();
        tools.setLayout(new BoxLayout(tools, BoxLayout.Y_AXIS));
        tools.setBackground(Theme.BG_MAIN);
        tools.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));
        tools.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(Theme.BG_MAIN);
        fields.setAlignmentX(Component.LEFT_ALIGNMENT);

        buscaField = new JTextField();
        buscaField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        buscaField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        buscaField.setToolTipText("Buscar livros, materiais, equipamentos...");
        buscaField.getAccessibleContext().setAccessibleName("Buscar anúncios");
        buscaField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                atualizarFiltrosAtivos();
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
        categoriaCombo.getAccessibleContext().setAccessibleName("Filtrar por categoria");
        categoriaCombo.addActionListener(e -> {
            atualizarFiltrosAtivos();
            agendarAtualizacaoGrid();
        });

        modalidadeCombo = new JComboBox<>(new String[]{
                "Trocas e doações",
                "Troca",
                "Doação"
        });
        Theme.styleComboBox(modalidadeCombo);
        modalidadeCombo.getAccessibleContext().setAccessibleName("Filtrar por modalidade");
        modalidadeCombo.addActionListener(e -> {
            atualizarFiltrosAtivos();
            agendarAtualizacaoGrid();
        });

        ordenacaoCombo = new JComboBox<>(new String[]{
                "Mais recentes", "Título (A–Z)", "Título (Z–A)"
        });
        Theme.styleComboBox(ordenacaoCombo);
        ordenacaoCombo.setPreferredSize(new Dimension(180, 40));
        ordenacaoCombo.getAccessibleContext().setAccessibleName("Ordenar anúncios");
        ordenacaoCombo.addActionListener(e -> agendarAtualizacaoGrid());

        JButton clearButton = Theme.createButton(
                "Limpar filtros", new Color(0xEE, 0xF2, 0xF0), Theme.TEXT_MAIN
        );
        clearButton.addActionListener(e -> limparFiltros());
        clearButton.setToolTipText("Limpar busca, categoria, modalidade e ordenação");
        clearButton.getAccessibleContext().setAccessibleName("Limpar filtros e ordenação");

        addFilterField(fields, "Buscar anúncios", buscaField, 0, 1.0, 280);
        addFilterField(fields, "Categoria", categoriaCombo, 1, 0.0, 190);
        addFilterField(fields, "Modalidade", modalidadeCombo, 2, 0.0, 170);

        JPanel sortControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        sortControls.setBackground(Theme.BG_MAIN);
        sortControls.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sortLabel = new JLabel("Ordenar por");
        sortLabel.setLabelFor(ordenacaoCombo);
        sortLabel.setForeground(Theme.TEXT_MAIN);
        sortLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        sortControls.add(sortLabel);
        sortControls.add(ordenacaoCombo);
        sortControls.add(clearButton);

        activeFiltersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        activeFiltersPanel.setBackground(Theme.BG_MAIN);
        activeFiltersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        activeFiltersPanel.setVisible(false);

        tools.add(fields);
        tools.add(sortControls);
        tools.add(activeFiltersPanel);
        atualizarFiltrosAtivos();

        return tools;
    }

    private void addFilterField(
            JPanel panel, String labelText, Component field, int column, double weight, int preferredWidth
    ) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(Theme.TEXT_MAIN);
        label.setLabelFor(field);

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = column;
        labelConstraints.gridy = 0;
        labelConstraints.weightx = weight;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        labelConstraints.insets = new Insets(0, column == 0 ? 0 : 12, 4, 0);
        labelConstraints.anchor = GridBagConstraints.LINE_START;
        panel.add(label, labelConstraints);

        field.setPreferredSize(new Dimension(preferredWidth, 40));
        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = column;
        fieldConstraints.gridy = 1;
        fieldConstraints.weightx = weight;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(0, column == 0 ? 0 : 12, 0, 0);
        panel.add(field, fieldConstraints);
    }

    private void atualizarFiltrosAtivos() {
        if (activeFiltersPanel == null) {
            return;
        }
        activeFiltersPanel.removeAll();
        boolean hasFilters = false;

        String termo = buscaField.getText().trim();
        if (!termo.isEmpty()) {
            hasFilters = true;
            activeFiltersPanel.add(criarChipFiltro("Busca: " + termo, () -> {
                buscaField.setText("");
                atualizarFiltrosAtivos();
                agendarAtualizacaoGrid();
            }));
        }

        String categoria = (String) categoriaCombo.getSelectedItem();
        if (categoria != null && !"Todas as categorias".equals(categoria)) {
            hasFilters = true;
            activeFiltersPanel.add(criarChipFiltro("Categoria: " + categoria, () ->
                    categoriaCombo.setSelectedIndex(0)
            ));
        }

        String modalidade = (String) modalidadeCombo.getSelectedItem();
        if (modalidade != null && !"Trocas e doações".equals(modalidade)) {
            hasFilters = true;
            activeFiltersPanel.add(criarChipFiltro("Modalidade: " + modalidade, () ->
                    modalidadeCombo.setSelectedIndex(0)
            ));
        }

        activeFiltersPanel.setVisible(hasFilters);
        activeFiltersPanel.revalidate();
        activeFiltersPanel.repaint();
    }

    private JButton criarChipFiltro(String texto, Runnable aoRemover) {
        String textoVisivel = texto.length() > 38 ? texto.substring(0, 35) + "..." : texto;
        JButton chip = Theme.createButton(textoVisivel + "  ×", Theme.PALE, Theme.GREEN);
        chip.setBorder(new EmptyBorder(5, 10, 5, 10));
        chip.setFont(new Font("SansSerif", Font.BOLD, 11));
        chip.setToolTipText("Remover filtro: " + texto);
        chip.getAccessibleContext().setAccessibleDescription("Remover o filtro " + texto);
        chip.addActionListener(e -> aoRemover.run());
        return chip;
    }

    private void limparFiltros() {
        buscaField.setText("");
        categoriaCombo.setSelectedIndex(0);
        modalidadeCombo.setSelectedIndex(0);
        ordenacaoCombo.setSelectedIndex(0);
        atualizarFiltrosAtivos();
        agendarAtualizacaoGrid();
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
        gridPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                atualizarColunasGrade();
            }
        });
        return gridPanel;
    }

    private JPanel createPaginationControls() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        controls.setBackground(Theme.BG_MAIN);
        controls.setAlignmentX(Component.LEFT_ALIGNMENT);

        paginaAnteriorBtn = Theme.createButton("Anterior", new Color(0xEE, 0xF2, 0xF0), Theme.TEXT_MAIN);
        paginaAnteriorBtn.getAccessibleContext().setAccessibleName("Mostrar página anterior");
        paginaAnteriorBtn.setEnabled(false);
        paginaAnteriorBtn.addActionListener(e -> {
            if (paginaAtual > 0) {
                paginaAtual--;
                carregarPaginaAtual();
            }
        });

        paginaLabel = new JLabel("Página 1");
        paginaLabel.setForeground(Theme.TEXT_MUTED);
        paginaLabel.getAccessibleContext().setAccessibleName("Página atual dos anúncios");

        proximaPaginaBtn = Theme.createButton("Próxima", new Color(0xEE, 0xF2, 0xF0), Theme.TEXT_MAIN);
        proximaPaginaBtn.getAccessibleContext().setAccessibleName("Mostrar próxima página");
        proximaPaginaBtn.setEnabled(false);
        proximaPaginaBtn.addActionListener(e -> {
            if (proximaPaginaBtn.isEnabled()) {
                paginaAtual++;
                carregarPaginaAtual();
            }
        });

        controls.add(paginaAnteriorBtn);
        controls.add(paginaLabel);
        controls.add(proximaPaginaBtn);
        return controls;
    }

    private int obterQuantidadeColunas() {
        int largura = gridPanel.getWidth();
        if (largura <= 0) {
            return GRID_COLUMNS;
        }
        return Math.max(1, Math.min(GRID_COLUMNS, largura / (MIN_CARD_WIDTH + GRID_GAP)));
    }

    private void atualizarColunasGrade() {
        if (!(gridPanel.getLayout() instanceof GridLayout)) {
            return;
        }
        int colunas = obterQuantidadeColunas();
        GridLayout layout = (GridLayout) gridPanel.getLayout();
        if (layout.getColumns() != colunas) {
            gridPanel.setLayout(new GridLayout(0, colunas, GRID_GAP, GRID_GAP));
            gridPanel.revalidate();
        }
    }

    public void atualizarGrid() {
        paginaAtual = 0;
        carregarPaginaAtual();
    }

    private void carregarPaginaAtual() {
        buscaTimer.stop();
        if (gridWorker != null) {
            gridWorker.cancel(true);
        }
        exibirCarregandoAnuncios();

        String termoBusca = buscaField.getText();
        String categoria = (String) categoriaCombo.getSelectedItem();
        String modalidade = (String) modalidadeCombo.getSelectedItem();
        int paginaSolicitada = paginaAtual;
        String ordenacao = switch (ordenacaoCombo.getSelectedIndex()) {
            case 1 -> "TITULO_ASC";
            case 2 -> "TITULO_DESC";
            default -> "RECENTES";
        };

        gridWorker = new SwingWorker<PaginaAnuncios, Void>() {
            @Override
            protected PaginaAnuncios doInBackground() throws SQLException {
                return controller.buscarPaginaAnuncios(
                        termoBusca, categoria, modalidade, ordenacao, paginaSolicitada, PAGE_SIZE
                );
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

    private void exibirAnuncios(PaginaAnuncios pagina) {
        List<Anuncio> anuncios = pagina.anuncios();
        gridPanel.removeAll();
        contadorLabel.setText(pagina.total() + (pagina.total() == 1 ? " item" : " itens"));

        if (anuncios.isEmpty()) {
            exibirEstadoVazio();
        } else {
            gridPanel.setLayout(new GridLayout(0, obterQuantidadeColunas(), GRID_GAP, GRID_GAP));
            for (Anuncio anuncio : anuncios) {
                gridPanel.add(criarCartaoAnuncio(anuncio));
            }
        }
        atualizarControlesPagina(pagina.total(), anuncios.size());

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void atualizarControlesPagina(int total, int itensNaPagina) {
        int totalPaginas = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));
        String textoPagina = "Página " + (paginaAtual + 1) + " de " + totalPaginas;
        paginaLabel.setText(textoPagina);
        paginaLabel.getAccessibleContext().setAccessibleDescription(textoPagina);
        paginaAnteriorBtn.setEnabled(paginaAtual > 0);
        proximaPaginaBtn.setEnabled((long) (paginaAtual + 1) * PAGE_SIZE < total && itensNaPagina > 0);
    }

    private void exibirEstadoVazio() {
        exibirMensagemGrid(
                "Nenhum anúncio encontrado. Tente mudar os filtros ou publique o primeiro!",
                "0 itens"
        );
    }

    private void exibirMensagemGrid(String mensagem) {
        exibirMensagemGrid(mensagem, contadorLabel.getText());
    }

    private void exibirMensagemGrid(String mensagem, String contador) {
        gridPanel.removeAll();
        gridPanel.setLayout(new BorderLayout());
        JLabel label = new JLabel(mensagem, JLabel.CENTER);
        label.setFont(new Font("SansSerif", Font.PLAIN, 15));
        label.setForeground(Theme.TEXT_MUTED);
        label.setBorder(new EmptyBorder(50, 20, 50, 20));
        gridPanel.add(label, BorderLayout.CENTER);
        contadorLabel.setText(contador);
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void exibirCarregandoAnuncios() {
        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(0, obterQuantidadeColunas(), GRID_GAP, GRID_GAP));
        int skeletonCount = Math.max(1, obterQuantidadeColunas());
        for (int i = 0; i < skeletonCount; i++) {
            gridPanel.add(criarCardEsqueleto());
        }
        contadorLabel.setText("Carregando...");
        paginaLabel.setText("Carregando...");
        paginaAnteriorBtn.setEnabled(false);
        proximaPaginaBtn.setEnabled(false);
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel criarCardEsqueleto() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));

        JPanel imagem = new JPanel();
        imagem.setBackground(Theme.PALE);
        imagem.setPreferredSize(new Dimension(300, 140));
        card.add(imagem, BorderLayout.NORTH);

        JPanel conteudo = new JPanel();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBackground(Color.WHITE);
        conteudo.setBorder(new EmptyBorder(18, 18, 18, 18));
        conteudo.add(blocoEsqueleto(120, 15));
        conteudo.add(Box.createVerticalStrut(14));
        conteudo.add(blocoEsqueleto(190, 22));
        conteudo.add(Box.createVerticalStrut(12));
        conteudo.add(blocoEsqueleto(250, 12));
        conteudo.add(Box.createVerticalStrut(7));
        conteudo.add(blocoEsqueleto(210, 12));
        card.add(conteudo, BorderLayout.CENTER);
        card.getAccessibleContext().setAccessibleName("Anúncio carregando");
        return card;
    }

    private JPanel blocoEsqueleto(int largura, int altura) {
        JPanel bloco = new JPanel();
        bloco.setBackground(Theme.PALE);
        bloco.setMaximumSize(new Dimension(largura, altura));
        bloco.setPreferredSize(new Dimension(largura, altura));
        bloco.setAlignmentX(Component.LEFT_ALIGNMENT);
        return bloco;
    }

    private void abrirOpcoesBanco() {
        JOptionPane.showMessageDialog(
                this,
                "Os dados são compartilhados pelo Supabase. Backups e restaurações do banco remoto "
                        + "devem ser feitos pela configuração do projeto Supabase.",
                "Banco de dados",
                JOptionPane.QUESTION_MESSAGE
        );
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
        exibirMensagemGrid("Não foi possível carregar os anúncios.", "Erro");
        paginaLabel.setText("Não foi possível carregar");
        paginaAnteriorBtn.setEnabled(false);
        proximaPaginaBtn.setEnabled(false);
        JOptionPane.showMessageDialog(this,
                "Erro ao carregar anúncios: " + erro.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void agendarAtualizacaoGrid() {
        paginaAtual = 0;
        buscaTimer.restart();
    }
}