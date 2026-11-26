package br.com.unimart.view.components;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import br.com.unimart.model.Anuncio;

class AnuncioCardTest {

    @Test
    void botaoDeInteresseEntregaOAnuncioSelecionado() throws Exception {
        Anuncio anuncio = new Anuncio();
        anuncio.setTitulo("Livro");
        AtomicReference<Anuncio> selecionado = new AtomicReference<>();

        SwingUtilities.invokeAndWait(() -> {
            AnuncioCard card = new AnuncioCard(anuncio, selecionado::set);
            JButton botao = localizarBotao(card, "Tenho interesse");

            assertNotNull(botao);
            botao.doClick();
        });

        assertSame(anuncio, selecionado.get());
    }

    private JButton localizarBotao(Container container, String texto) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton button && texto.equals(button.getText())) {
                return button;
            }
            if (component instanceof Container child) {
                JButton button = localizarBotao(child, texto);
                if (button != null) {
                    return button;
                }
            }
        }
        return null;
    }
}
