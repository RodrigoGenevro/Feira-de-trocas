package br.com.unimart;

import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.util.DatabaseConfig;
import br.com.unimart.view.MainFrame;
import br.com.unimart.view.dialogs.LoginCadastroDialog;

/**
 * Ponto de entrada da aplicação desktop UniMart.
 * Inicializa o banco de dados, exige autenticação e exibe a View principal.
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        try {
            DatabaseConfig.initDatabase();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Erro crítico ao inicializar o banco de dados SQLite:\n" + e.getMessage(),
                    "Erro de Inicialização", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            FeiraController controller = new FeiraController();
            MainFrame mainFrame = new MainFrame(controller);

            LoginCadastroDialog acessoDialog = new LoginCadastroDialog(
                    mainFrame,
                    controller,
                    () -> {
                        mainFrame.atualizarHeaderAuth();
                        mainFrame.setVisible(true);
                    },
                    () -> System.exit(0)
            );
            acessoDialog.setVisible(true);
        });

    }
    
}