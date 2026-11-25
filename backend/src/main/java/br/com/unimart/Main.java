package br.com.unimart;

import br.com.unimart.controller.FeiraController;
import br.com.unimart.util.DatabaseConfig;
import br.com.unimart.view.MainFrame;

import javax.swing.*;
import java.sql.SQLException;

/**
 * Ponto de entrada da aplicação desktop UniMart.
 * Inicializa o banco de dados, instancia o Controlador e exibe a View principal.
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
            mainFrame.setVisible(true);
        });
    }
}
