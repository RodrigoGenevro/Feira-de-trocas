package br.com.unimart;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.api.FeiraApiClient;
import br.com.unimart.view.MainFrame;
import br.com.unimart.view.dialogs.LoginCadastroDialog;

/**
 * Ponto de entrada da aplicação desktop Trocaê.
 * Configura o modo local ou o cliente da API, exige autenticação e exibe a View principal.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        configurarAparenciaNativa();

        String apiUrl = System.getenv("TROCAE_API_URL");
        boolean modoLocal = apiUrl == null || apiUrl.isBlank();
        FeiraController controller;
        try {
            controller = modoLocal
                    ? new FeiraController()
                    : new FeiraController(new FeiraApiClient(apiUrl));
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                    null, e.getMessage(), "Configuração inválida", JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        SwingUtilities.invokeLater(() -> {
            if (modoLocal) {
                JOptionPane.showMessageDialog(
                        null,
                        "TROCAE_API_URL não configurada. O aplicativo usará o banco SQLite local; "
                                + "os dados não serão compartilhados com outros dispositivos.",
                        "Modo local", JOptionPane.INFORMATION_MESSAGE
                );
            }
            MainFrame mainFrame = new MainFrame(controller);

            LoginCadastroDialog acessoDialog = new LoginCadastroDialog(
                    mainFrame,
                    controller,
                    () -> {
                        mainFrame.atualizarHeaderAuth();
                        mainFrame.atualizarGrid();
                        mainFrame.setVisible(true);
                    },
                    () -> System.exit(0)
            );
            acessoDialog.setVisible(true);
        });
    }

    private static void configurarAparenciaNativa() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            LOGGER.log(Level.FINE, "Não foi possível aplicar a aparência nativa do sistema.", e);
        }
    }
}