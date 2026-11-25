package br.com.unimart;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class Main extends JFrame {

    private static final String DB_URL = "jdbc:sqlite:database/feira_trocas.db";
    private static final String DB_FALLBACK_URL = "jdbc:sqlite:../database/feira_trocas.db";

    // Paleta de cores baseada no style.css original
    private static final Color GREEN = new Color(0x16, 0x7D, 0x70);
    private static final Color DARK = new Color(0x16, 0x3D, 0x3B);
    private static final Color PALE = new Color(0xE8, 0xF5, 0xF1);
    private static final Color INTRO_BG = new Color(0xDF, 0xF1, 0xEB);
    private static final Color BG_MAIN = new Color(0xF6, 0xF8, 0xF7);
    private static final Color TEXT_MAIN = new Color(0x20, 0x33, 0x31);
    private static final Color TEXT_MUTED = new Color(0x71, 0x81, 0x7E);
    private static final Color BORDER_COLOR = new Color(0xD9, 0xE2, 0xDF);

    private JTextField buscaField;
    private JComboBox<String> categoriaCombo;
    private JComboBox<String> modalidadeCombo;
    private JLabel contadorLabel;
    private JPanel gridPanel;
    private String activeDbUrl;

    public Main() {
        resolveDbPath();
        initDatabase();

        setTitle("UniMart | Feira de trocas");
        setSize(1180, 820);
        setMinimumSize(new Dimension(900, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_MAIN);
        setLayout(new BorderLayout());

        // 1. Header
        add(createHeader(), BorderLayout.NORTH);

        // 2. Main Scrollable Area (Intro + Tools + Grid)
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(BG_MAIN);
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
        scrollPane.getViewport().setBackground(BG_MAIN);
        add(scrollPane, BorderLayout.CENTER);

        carregarAnuncios();
    }

    private void resolveDbPath() {
        try {
            Path cwd = Paths.get("").toAbsolutePath();
            if (cwd.getFileName() != null && cwd.getFileName().toString().equals("backend")) {
                Files.createDirectories(Paths.get("../database"));
                activeDbUrl = DB_FALLBACK_URL;
            } else {
                Files.createDirectories(Paths.get("database"));
                activeDbUrl = DB_URL;
            }
        } catch (Exception e) {
            activeDbUrl = DB_URL;
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(activeDbUrl);
    }

    private void initDatabase() {
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS anuncios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "titulo TEXT NOT NULL, " +
                    "descricao TEXT NOT NULL, " +
                    "categoria TEXT NOT NULL, " +
                    "modalidade TEXT NOT NULL, " +
                    "nome TEXT NOT NULL, " +
                    "turma TEXT, " +
                    "foto TEXT, " +
                    "status TEXT NOT NULL DEFAULT 'DISPONIVEL', " +
                    "criado_em TEXT DEFAULT CURRENT_TIMESTAMP)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS interesses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "anuncio_id INTEGER NOT NULL, " +
                    "nome TEXT NOT NULL, " +
                    "mensagem TEXT NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'PENDENTE', " +
                    "criado_em TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(anuncio_id) REFERENCES anuncios(id))");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao inicializar banco de dados: " + e.getMessage(),
                    "Erro de Banco", JOptionPane.ERROR_MESSAGE);
        }
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
        brand.setForeground(GREEN);

        JLabel subtitle = new JLabel("Feira de trocas e doações da comunidade Senac");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);

        leftPanel.add(brand);
        leftPanel.add(subtitle);

        JButton novoBtn = createStyledButton("+ Publicar anúncio", GREEN, Color.WHITE);
        novoBtn.addActionListener(e -> abrirDialogNovoAnuncio());

        header.add(leftPanel, BorderLayout.WEST);
        header.add(novoBtn, BorderLayout.EAST);
        return header;
    }

    private JPanel createIntroBanner() {
        JPanel intro = new JPanel();
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        intro.setBackground(INTRO_BG);
        intro.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xCE, 0xE6, 0xDE), 1, true),
                new EmptyBorder(32, 36, 32, 36)
        ));
        intro.setMaximumSize(new Dimension(Integer.MAX_VALUE, 210));
        intro.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tag = new JLabel("COMUNIDADE SENAC");
        tag.setFont(new Font("SansSerif", Font.BOLD, 11));
        tag.setForeground(GREEN);

        JLabel title = new JLabel("<html>Dê uma nova vida<br>ao que você não usa.</html>");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(DARK);

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
        tools.setBackground(BG_MAIN);
        tools.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        tools.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        buscaField = new JTextField();
        buscaField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        buscaField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        buscaField.setToolTipText("Buscar livros, materiais, equipamentos...");
        buscaField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                carregarAnuncios();
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
        styleComboBox(categoriaCombo);
        categoriaCombo.addActionListener(e -> carregarAnuncios());

        modalidadeCombo = new JComboBox<>(new String[]{
                "Trocas e doações",
                "Troca",
                "Doação"
        });
        styleComboBox(modalidadeCombo);
        modalidadeCombo.addActionListener(e -> carregarAnuncios());

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
        sectionTitle.setBackground(BG_MAIN);
        sectionTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Anúncios disponíveis");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT_MAIN);

        contadorLabel = new JLabel("0 itens");
        contadorLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        contadorLabel.setForeground(TEXT_MUTED);

        sectionTitle.add(title, BorderLayout.WEST);
        sectionTitle.add(contadorLabel, BorderLayout.EAST);
        return sectionTitle;
    }

    private JPanel createGridContainer() {
        gridPanel = new JPanel(new GridLayout(0, 3, 18, 18));
        gridPanel.setBackground(BG_MAIN);
        gridPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return gridPanel;
    }

    private void carregarAnuncios() {
        gridPanel.removeAll();

        String q = buscaField.getText() != null ? buscaField.getText().trim() : "";
        String catSelected = (String) categoriaCombo.getSelectedItem();
        String cat = (catSelected == null || catSelected.equals("Todas as categorias")) ? "" : catSelected;

        String modSelected = (String) modalidadeCombo.getSelectedItem();
        String modFiltro = "";
        if ("Troca".equals(modSelected)) modFiltro = "TROCA";
        else if ("Doação".equals(modSelected)) modFiltro = "DOACAO";

        List<Map<String, Object>> itens = new ArrayList<>();

        String sql = "SELECT * FROM anuncios WHERE (? = '' OR lower(titulo || ' ' || descricao) LIKE ?) " +
                     "AND (? = '' OR categoria = ?) AND status != 'CONCLUIDO' ORDER BY id DESC";

        try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, q);
            p.setString(2, "%" + q.toLowerCase() + "%");
            p.setString(3, cat);
            p.setString(4, cat);

            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    String mod = r.getString("modalidade");
                    if (!modFiltro.isEmpty() && !modFiltro.equals(mod)) {
                        continue;
                    }
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", r.getInt("id"));
                    m.put("titulo", r.getString("titulo"));
                    m.put("descricao", r.getString("descricao"));
                    m.put("categoria", r.getString("categoria"));
                    m.put("modalidade", mod);
                    m.put("nome", r.getString("nome"));
                    m.put("turma", r.getString("turma"));
                    m.put("foto", r.getString("foto"));
                    m.put("status", r.getString("status"));
                    itens.add(m);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar anúncios: " + e.getMessage());
        }

        contadorLabel.setText(itens.size() + (itens.size() == 1 ? " item" : " itens"));

        if (itens.isEmpty()) {
            gridPanel.setLayout(new BorderLayout());
            JLabel empty = new JLabel("Nenhum anúncio encontrado. Tente mudar os filtros ou publique o primeiro!", JLabel.CENTER);
            empty.setFont(new Font("SansSerif", Font.PLAIN, 15));
            empty.setForeground(TEXT_MUTED);
            empty.setBorder(new EmptyBorder(50, 20, 50, 20));
            gridPanel.add(empty, BorderLayout.CENTER);
        } else {
            gridPanel.setLayout(new GridLayout(0, 3, 18, 18));
            for (Map<String, Object> item : itens) {
                gridPanel.add(createItemCard(item));
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JPanel createItemCard(Map<String, Object> item) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new LineBorder(new Color(0xE5, 0xEB, 0xE8), 1, true));

        // Topo (Imagem ou placeholder)
        JPanel imgContainer = new JPanel(new BorderLayout());
        imgContainer.setBackground(PALE);
        imgContainer.setPreferredSize(new Dimension(300, 150));

        JLabel imgLabel = new JLabel("[ Item ]", JLabel.CENTER);
        imgLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        imgLabel.setForeground(GREEN);
        imgContainer.add(imgLabel, BorderLayout.CENTER);

        String fotoUrl = (String) item.get("foto");
        if (fotoUrl != null && !fotoUrl.isBlank()) {
            carregarImagemAssincrona(fotoUrl, imgLabel);
        }

        // Corpo do Card
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(16, 16, 16, 16));

        String modText = "DOACAO".equals(item.get("modalidade")) ? "DOAÇÃO" : "TROCA";
        String catText = String.valueOf(item.get("categoria"));

        JLabel pill = new JLabel("  " + modText + " · " + catText + "  ");
        pill.setOpaque(true);
        pill.setBackground(PALE);
        pill.setForeground(GREEN);
        pill.setFont(new Font("SansSerif", Font.BOLD, 11));
        pill.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titulo = new JLabel(String.valueOf(item.get("titulo")));
        titulo.setFont(new Font("SansSerif", Font.BOLD, 17));
        titulo.setForeground(TEXT_MAIN);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea desc = new JTextArea(String.valueOf(item.get("descricao")));
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        desc.setForeground(TEXT_MUTED);
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

        String autor = "Por " + item.get("nome");
        String turma = (String) item.get("turma");
        if (turma != null && !turma.isBlank()) {
            autor += " · " + turma;
        }

        JLabel autorLabel = new JLabel(autor);
        autorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        autorLabel.setForeground(TEXT_MUTED);

        int idAnuncio = (int) item.get("id");
        String tituloAnuncio = String.valueOf(item.get("titulo"));

        JButton btnInteresse = createStyledButton("Tenho interesse", DARK, Color.WHITE);
        btnInteresse.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnInteresse.setBorder(new EmptyBorder(8, 12, 8, 12));
        btnInteresse.addActionListener(e -> abrirDialogInteresse(idAnuncio, tituloAnuncio));

        foot.add(autorLabel, BorderLayout.CENTER);
        foot.add(btnInteresse, BorderLayout.EAST);

        body.add(pill);
        body.add(Box.createVerticalStrut(8));
        body.add(titulo);
        body.add(Box.createVerticalStrut(6));
        body.add(desc);
        body.add(Box.createVerticalGlue());
        body.add(foot);

        card.add(imgContainer, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private void carregarImagemAssincrona(String urlStr, JLabel targetLabel) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    URL url = URI.create(urlStr).toURL();
                    ImageIcon raw = new ImageIcon(url);
                    if (raw.getIconWidth() > 0) {
                        Image scaled = raw.getImage().getScaledInstance(340, 150, Image.SCALE_SMOOTH);
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

    private void abrirDialogNovoAnuncio() {
        JDialog dialog = new JDialog(this, "Novo anúncio", true);
        dialog.setSize(480, 560);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel head = new JLabel("Novo anúncio");
        head.setFont(new Font("SansSerif", Font.BOLD, 20));
        head.setForeground(DARK);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nomeField = createInputField();
        JTextField turmaField = createInputField();
        JTextField tituloField = createInputField();
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> catBox = new JComboBox<>(new String[]{
                "Livros", "Materiais escolares", "Eletrônicos", "Equipamentos", "Outros"
        });
        styleComboBox(catBox);
        catBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> modBox = new JComboBox<>(new String[]{"TROCA", "DOACAO"});
        styleComboBox(modBox);
        modBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField fotoField = createInputField();

        form.add(head);
        form.add(Box.createVerticalStrut(14));
        form.add(createFieldLabel("Seu nome *"));
        form.add(nomeField);
        form.add(Box.createVerticalStrut(10));
        form.add(createFieldLabel("Turma (opcional)"));
        form.add(turmaField);
        form.add(Box.createVerticalStrut(10));
        form.add(createFieldLabel("Título do item *"));
        form.add(tituloField);
        form.add(Box.createVerticalStrut(10));
        form.add(createFieldLabel("Descrição *"));
        form.add(descScroll);
        form.add(Box.createVerticalStrut(10));

        JPanel twoCols = new JPanel(new GridLayout(1, 2, 12, 0));
        twoCols.setBackground(Color.WHITE);
        twoCols.setAlignmentX(Component.LEFT_ALIGNMENT);
        twoCols.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JPanel col1 = new JPanel();
        col1.setLayout(new BoxLayout(col1, BoxLayout.Y_AXIS));
        col1.setBackground(Color.WHITE);
        col1.add(createFieldLabel("Categoria"));
        col1.add(catBox);

        JPanel col2 = new JPanel();
        col2.setLayout(new BoxLayout(col2, BoxLayout.Y_AXIS));
        col2.setBackground(Color.WHITE);
        col2.add(createFieldLabel("Modalidade"));
        col2.add(modBox);

        twoCols.add(col1);
        twoCols.add(col2);
        form.add(twoCols);

        form.add(Box.createVerticalStrut(10));
        form.add(createFieldLabel("Link da foto (opcional)"));
        form.add(fotoField);
        form.add(Box.createVerticalStrut(18));

        JButton submitBtn = createStyledButton("Publicar anúncio", GREEN, Color.WHITE);
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        submitBtn.addActionListener(e -> {
            String nome = nomeField.getText().trim();
            String titulo = tituloField.getText().trim();
            String desc = descArea.getText().trim();

            if (nome.isEmpty() || titulo.isEmpty() || desc.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Preencha os campos obrigatórios (*).", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = "INSERT INTO anuncios(titulo, descricao, categoria, modalidade, nome, turma, foto) VALUES(?,?,?,?,?,?,?)";
            try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, titulo);
                p.setString(2, desc);
                p.setString(3, (String) catBox.getSelectedItem());
                p.setString(4, (String) modBox.getSelectedItem());
                p.setString(5, nome);
                p.setString(6, turmaField.getText().trim());
                p.setString(7, fotoField.getText().trim());
                p.executeUpdate();

                dialog.dispose();
                carregarAnuncios();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Não foi possível publicar o anúncio: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        form.add(submitBtn);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void abrirDialogInteresse(int anuncioId, String tituloAnuncio) {
        JDialog dialog = new JDialog(this, "Tenho interesse", true);
        dialog.setSize(440, 360);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel head = new JLabel("Tenho interesse");
        head.setFont(new Font("SansSerif", Font.BOLD, 20));
        head.setForeground(DARK);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Item: " + tituloAnuncio);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nomeField = createInputField();
        JTextArea msgArea = new JTextArea(4, 20);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane msgScroll = new JScrollPane(msgArea);
        msgScroll.setBorder(new LineBorder(BORDER_COLOR, 1, true));
        msgScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(head);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(14));
        form.add(createFieldLabel("Seu nome *"));
        form.add(nomeField);
        form.add(Box.createVerticalStrut(10));
        form.add(createFieldLabel("Mensagem (como gostaria de combinar) *"));
        form.add(msgScroll);
        form.add(Box.createVerticalStrut(18));

        JButton enviarBtn = createStyledButton("Enviar interesse", GREEN, Color.WHITE);
        enviarBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        enviarBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        enviarBtn.addActionListener(e -> {
            String nome = nomeField.getText().trim();
            String msg = msgArea.getText().trim();

            if (nome.isEmpty() || msg.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Preencha seu nome e mensagem.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String sql = "INSERT INTO interesses(anuncio_id, nome, mensagem) VALUES(?,?,?)";
            try (Connection c = connect(); PreparedStatement p = c.prepareStatement(sql)) {
                p.setInt(1, anuncioId);
                p.setString(2, nome);
                p.setString(3, msg);
                p.executeUpdate();

                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Interesse enviado com sucesso!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Não foi possível enviar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        form.add(enviarBtn);
        dialog.add(form);
        dialog.setVisible(true);
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(11, 18, 11, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        return lbl;
    }

    private JTextField createInputField() {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT_MAIN);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
