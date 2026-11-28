package br.com.unimart;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.util.DatabaseConfig;
import br.com.unimart.view.MainFrame;
import br.com.unimart.view.dialogs.LoginCadastroDialog;

/**
 * Ponto de entrada da aplicação desktop Trocaê.
 * Inicializa o banco SQLite local e exibe a View principal.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        configurarAparenciaNativa();

        FeiraController controller;
        try {
            DatabaseConfig.initDatabase();
            controller = new FeiraController();
        } catch (SQLException | IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "Não foi possível inicializar o banco SQLite local.", e);
            JOptionPane.showMessageDialog(
                    null,
                    "Não foi possível inicializar o banco de dados local: " + e.getMessage(),
                    "Erro no banco de dados",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        SwingUtilities.invokeLater(() -> {
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