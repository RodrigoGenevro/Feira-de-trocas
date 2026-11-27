package br.com.unimart.view.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.model.Anuncio;
import br.com.unimart.model.Usuario;
import br.com.unimart.util.ImageThumbnailLoader;
import br.com.unimart.util.Theme;

/**
 * Modal de cadastro de novo anúncio. Herda de BaseDialog.
 */
public class NovoAnuncioDialog extends BaseDialog {

    private static final Color VALIDATION_ERROR = new Color(0xB0, 0x2A, 0x2A);
    private final FeiraController controller;
    private final Runnable onSuccessCallback;
    private final Anuncio anuncioParaEditar;
    private Timer fotoPreviewTimer;
    private SwingWorker<javax.swing.ImageIcon, Void> fotoPreviewWorker;
    private JLabel fotoPreviewLabel;
    private String enderecoPreviaFoto = "";

    public NovoAnuncioDialog(Frame owner, FeiraController controller, Runnable onSuccessCallback) {
        this(owner, controller, onSuccessCallback, null);
    }

    public NovoAnuncioDialog(
            Frame owner, FeiraController controller, Runnable onSuccessCallback, Anuncio anuncioParaEditar
    ) {
        super(owner, anuncioParaEditar == null ? "Novo anúncio" : "Editar anúncio", 480, 560);
        this.controller = controller;
        this.onSuccessCallback = onSuccessCallback;
        this.anuncioParaEditar = anuncioParaEditar;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        construirFormulario();
    }

    private void construirFormulario() {
        Usuario logado = controller.getUsuarioLogado();

        JTextField nomeField = Theme.createInputField();
        JTextField turmaField = Theme.createInputField();
        nomeField.getAccessibleContext().setAccessibleName("Seu nome");
        turmaField.getAccessibleContext().setAccessibleName("Sua turma, opcional");

        if (logado != null) {
            nomeField.setText(logado.getNome());
            nomeField.setEditable(false);
            nomeField.setBackground(new Color(0xF4, 0xF6, 0xF5));

            if (logado.getTurma() != null && !logado.getTurma().isBlank()) {
                turmaField.setText(logado.getTurma());
                turmaField.setEditable(false);
                turmaField.setBackground(new Color(0xF4, 0xF6, 0xF5));
            }
        }

        JTextField tituloField = Theme.createInputField();
        tituloField.getAccessibleContext().setAccessibleName("Título do item");

        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descArea.getAccessibleContext().setAccessibleName("Descrição do item");
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton sugestaoDesc = Theme.createButton(
                "Sugerir descrição",
                new Color(0xEE, 0xF2, 0xF0),
                Theme.TEXT_MAIN
        );
        sugestaoDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        sugestaoDesc.setMaximumSize(new Dimension(170, 32));

        JComboBox<String> catBox = new JComboBox<>(new String[]{
                "Livros", "Materiais escolares", "Eletrônicos", "Equipamentos", "Outros"
        });
        Theme.styleComboBox(catBox);
        catBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        catBox.getAccessibleContext().setAccessibleName("Categoria do anúncio");

        JComboBox<String> modBox = new JComboBox<>(new String[]{"TROCA", "DOACAO"});
        Theme.styleComboBox(modBox);
        modBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        modBox.getAccessibleContext().setAccessibleName("Modalidade do anúncio");

        JTextField fotoField = Theme.createInputField();

        if (anuncioParaEditar != null) {
            tituloField.setText(anuncioParaEditar.getTitulo());
            descArea.setText(anuncioParaEditar.getDescricao());
            catBox.setSelectedItem(anuncioParaEditar.getCategoria());
            modBox.setSelectedItem(anuncioParaEditar.getModalidade());
            fotoField.setText(anuncioParaEditar.getFotoUrl());
        }

        String tituloFormulario = anuncioParaEditar == null ? "Novo anúncio" : "Editar anúncio";
        String textoBotao = anuncioParaEditar == null ? "Publicar anúncio" : "Salvar alterações";
        formPanel.add(createDialogHeader(tituloFormulario));
        formPanel.add(Box.createVerticalStrut(14));
        formPanel.add(criarRotuloCampo("Seu nome *", nomeField));
        formPanel.add(nomeField);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(criarRotuloCampo("Turma (opcional)", turmaField));
        formPanel.add(turmaField);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(criarRotuloCampo("Título do item *", tituloField));
        formPanel.add(tituloField);
        JLabel tituloCount = new JLabel("0/" + Anuncio.MAX_TITULO_LENGTH);
        tituloCount.setAlignmentX(Component.LEFT_ALIGNMENT);
        tituloCount.getAccessibleContext().setAccessibleName("Contagem de caracteres do título");
        formPanel.add(tituloCount);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(criarRotuloCampo("Descrição *", descArea));
        formPanel.add(descScroll);
        formPanel.add(Box.createVerticalStrut(6));
        formPanel.add(sugestaoDesc);
        JLabel descricaoCount = new JLabel("0/" + Anuncio.MAX_DESCRICAO_LENGTH);
        descricaoCount.setAlignmentX(Component.LEFT_ALIGNMENT);
        descricaoCount.getAccessibleContext().setAccessibleName("Contagem de caracteres da descrição");
        formPanel.add(descricaoCount);
        formPanel.add(Box.createVerticalStrut(10));

        JPanel twoCols = new JPanel(new GridLayout(1, 2, 12, 0));
        twoCols.setBackground(Color.WHITE);
        twoCols.setAlignmentX(Component.LEFT_ALIGNMENT);
        twoCols.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JPanel col1 = new JPanel();
        col1.setLayout(new BoxLayout(col1, BoxLayout.Y_AXIS));
        col1.setBackground(Color.WHITE);
        col1.add(criarRotuloCampo("Categoria", catBox));
        col1.add(catBox);

        JPanel col2 = new JPanel();
        col2.setLayout(new BoxLayout(col2, BoxLayout.Y_AXIS));
        col2.setBackground(Color.WHITE);
        col2.add(criarRotuloCampo("Modalidade", modBox));
        col2.add(modBox);

        twoCols.add(col1);
        twoCols.add(col2);
        formPanel.add(twoCols);

        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(criarRotuloCampo("Link da foto (opcional)", fotoField));
        fotoField.setToolTipText("Se informar uma foto, use um link público iniciado por http:// ou https://.");
        fotoField.getAccessibleContext().setAccessibleName("Link HTTP ou HTTPS da foto, opcional");
        formPanel.add(fotoField);
        JLabel fotoFeedback = new JLabel("A prévia é opcional; use um link público HTTP ou HTTPS.");
        fotoFeedback.setAlignmentX(Component.LEFT_ALIGNMENT);
        fotoFeedback.setForeground(Theme.TEXT_MUTED);
        formPanel.add(fotoFeedback);
        fotoPreviewLabel = new JLabel("A prévia da foto aparecerá aqui.", SwingConstants.CENTER);
        fotoPreviewLabel.setPreferredSize(new Dimension(320, 140));
        fotoPreviewLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        fotoPreviewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        fotoPreviewLabel.setOpaque(true);
        fotoPreviewLabel.setBackground(Theme.PALE);
        fotoPreviewLabel.setForeground(Theme.TEXT_MUTED);
        fotoPreviewLabel.setBorder(new LineBorder(Theme.BORDER_COLOR, 1, true));
        fotoPreviewLabel.getAccessibleContext().setAccessibleName("Prévia da foto do anúncio");
        formPanel.add(fotoPreviewLabel);
        formPanel.add(Box.createVerticalStrut(18));

        sugestaoDesc.addActionListener(event -> {
            String titulo = tituloField.getText().trim();
            if (titulo.isEmpty()) {
                showWarning("Informe o título do item antes de sugerir uma descrição.");
                return;
            }
            String categoria = String.valueOf(catBox.getSelectedItem());
            String modalidade = "DOACAO".equals(modBox.getSelectedItem())
                    ? "doação" : "troca";
            String sugestao = "Este item está disponível para " + modalidade
                    + " na comunidade do campus. Categoria: " + categoria
                    + ". " + titulo + " encontra-se em bom estado de uso; "
                    + "combine diretamente com o anunciante o encontro no campus.";
            int resposta = JOptionPane.YES_OPTION;
            if (!descArea.getText().trim().isEmpty()) {
                resposta = JOptionPane.showConfirmDialog(
                        this,
                        "Substituir a descrição atual pela sugestão local?",
                        "Sugerir descrição",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
            }
            if (resposta == JOptionPane.YES_OPTION) {
                descArea.setText(sugestao);
            }
        });

        JButton submitBtn = Theme.createButton(textoBotao, Theme.GREEN, Color.WHITE);
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        Runnable validarFormulario = () -> {
            String titulo = tituloField.getText().trim();
            String descricao = descArea.getText().trim();
            String foto = fotoField.getText().trim();
            boolean tituloValido = !titulo.isEmpty() && titulo.length() <= Anuncio.MAX_TITULO_LENGTH;
            boolean descricaoValida = !descricao.isEmpty()
                    && descricao.length() <= Anuncio.MAX_DESCRICAO_LENGTH;
            boolean fotoValida = true;
            String feedbackFoto = "A prévia é opcional; use um link público HTTP ou HTTPS.";
            try {
                Anuncio.validarUrlFoto(foto);
            } catch (IllegalArgumentException e) {
                fotoValida = false;
                feedbackFoto = e.getMessage();
            }

            tituloCount.setText(titulo.length() + "/" + Anuncio.MAX_TITULO_LENGTH);
            tituloCount.setForeground(tituloValido ? Theme.TEXT_MUTED : VALIDATION_ERROR);
            descricaoCount.setText(descricao.length() + "/" + Anuncio.MAX_DESCRICAO_LENGTH);
            descricaoCount.setForeground(descricaoValida ? Theme.TEXT_MUTED : VALIDATION_ERROR);
            fotoFeedback.setText(feedbackFoto);
            fotoFeedback.setForeground(fotoValida ? Theme.TEXT_MUTED : VALIDATION_ERROR);
            submitBtn.setEnabled(tituloValido && descricaoValida && fotoValida);
        };
        DocumentListener formListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                validarFormulario.run();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                validarFormulario.run();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                validarFormulario.run();
            }
        };
        tituloField.getDocument().addDocumentListener(formListener);
        descArea.getDocument().addDocumentListener(formListener);
        fotoField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                validarFormulario.run();
                agendarPreviaFoto(fotoField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                validarFormulario.run();
                agendarPreviaFoto(fotoField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                validarFormulario.run();
                agendarPreviaFoto(fotoField.getText());
            }
        });
        validarFormulario.run();
        agendarPreviaFoto(fotoField.getText());
        submitBtn.addActionListener(e -> {
            if (!submitBtn.isEnabled()) {
                return;
            }
            submitBtn.setEnabled(false);
            submitBtn.setText(anuncioParaEditar == null ? "Publicando..." : "Salvando...");
            String nome = nomeField.getText();
            String turma = turmaField.getText();
            String titulo = tituloField.getText();
            String descricao = descArea.getText();
            String categoria = (String) catBox.getSelectedItem();
            String modalidade = (String) modBox.getSelectedItem();
            String foto = fotoField.getText();

            new SwingWorker<Anuncio, Void>() {
                @Override
                protected Anuncio doInBackground() throws Exception {
                    if (anuncioParaEditar != null) {
                        controller.atualizarAnuncio(
                                anuncioParaEditar.getId(), titulo, descricao, categoria, modalidade, foto
                        );
                        return anuncioParaEditar;
                    }
                    return controller.criarAnuncio(nome, turma, titulo, descricao,
                            categoria, modalidade, foto);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        dispose();
                        if (onSuccessCallback != null) {
                            onSuccessCallback.run();
                        }
                    } catch (java.util.concurrent.ExecutionException ex) {
                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                        if (causa instanceof IllegalArgumentException) {
                            showWarning(causa.getMessage());
                        } else {
                            String acao = anuncioParaEditar == null ? "publicar" : "salvar as alterações do";
                            showError("Não foi possível " + acao + " anúncio: " + causa.getMessage());
                        }
                        submitBtn.setEnabled(true);
                        submitBtn.setText(textoBotao);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        submitBtn.setEnabled(true);
                        submitBtn.setText(textoBotao);
                        String acao = anuncioParaEditar == null ? "publicação" : "edição";
                        showError("A " + acao + " do anúncio foi interrompida.");
                    }
                }
            }.execute();
        });

        formPanel.add(submitBtn);
    }

    private JLabel criarRotuloCampo(String texto, Component campo) {
        JLabel label = Theme.createFieldLabel(texto);
        label.setLabelFor(campo);
        return label;
    }

    private void agendarPreviaFoto(String url) {
        if (fotoPreviewTimer == null) {
            fotoPreviewTimer = new Timer(500, e -> carregarPreviaFoto(enderecoPreviaFoto));
            fotoPreviewTimer.setRepeats(false);
        }

        String endereco = url.trim();
        enderecoPreviaFoto = endereco;
        if (endereco.isEmpty()) {
            if (fotoPreviewWorker != null) {
                fotoPreviewWorker.cancel(true);
            }
            fotoPreviewLabel.setIcon(null);
            fotoPreviewLabel.setToolTipText(null);
            atualizarTextoPreviaFoto("A prévia da foto aparecerá aqui.");
            return;
        }
        try {
            Anuncio.validarUrlFoto(endereco);
            fotoPreviewLabel.setIcon(null);
            atualizarTextoPreviaFoto("Prévia pendente...");
            fotoPreviewTimer.restart();
        } catch (IllegalArgumentException e) {
            fotoPreviewTimer.stop();
            if (fotoPreviewWorker != null) {
                fotoPreviewWorker.cancel(true);
            }
            fotoPreviewLabel.setIcon(null);
            fotoPreviewLabel.setToolTipText(null);
            atualizarTextoPreviaFoto("Corrija o link para carregar a prévia.");
        }
    }

    private void carregarPreviaFoto(String url) {
        String endereco = url.trim();
        if (fotoPreviewWorker != null) {
            fotoPreviewWorker.cancel(true);
        }
        fotoPreviewWorker = new SwingWorker<>() {
            @Override
            protected javax.swing.ImageIcon doInBackground() throws Exception {
                return ImageThumbnailLoader.load(endereco, 320, 140);
            }

            @Override
            protected void done() {
                if (isCancelled() || !endereco.equals(enderecoPreviaFoto)) {
                    return;
                }
                try {
                    javax.swing.ImageIcon icon = get();
                    fotoPreviewLabel.setIcon(icon);
                    atualizarTextoPreviaFoto("");
                    fotoPreviewLabel.getAccessibleContext().setAccessibleDescription("Prévia da foto carregada.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fotoPreviewLabel.setIcon(null);
                    atualizarTextoPreviaFoto("Prévia interrompida.");
                } catch (java.util.concurrent.ExecutionException e) {
                    fotoPreviewLabel.setIcon(null);
                    atualizarTextoPreviaFoto("Prévia indisponível. Verifique o link.");
                }
            }
        };
        fotoPreviewLabel.setToolTipText(endereco);
        fotoPreviewWorker.execute();
    }

    private void atualizarTextoPreviaFoto(String texto) {
        fotoPreviewLabel.setText(texto);
        fotoPreviewLabel.getAccessibleContext().setAccessibleDescription(texto);
    }

    @Override
    public void dispose() {
        if (fotoPreviewTimer != null) {
            fotoPreviewTimer.stop();
        }
        if (fotoPreviewWorker != null) {
            fotoPreviewWorker.cancel(true);
        }
        super.dispose();
    }
}