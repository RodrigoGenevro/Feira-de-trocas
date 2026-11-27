package br.com.unimart.view.components;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import br.com.unimart.model.Anuncio;
import br.com.unimart.util.ImageThumbnailLoader;

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

    @Test
    void proprioAnuncioNaoPermiteEnviarInteresse() throws Exception {
        Anuncio anuncio = new Anuncio();

        SwingUtilities.invokeAndWait(() -> {
            AnuncioCard card = new AnuncioCard(anuncio, ignored -> {
            }, false);
            JButton botao = localizarBotao(card, "Seu anúncio");

            assertNotNull(botao);
            assertFalse(botao.isEnabled());
        });
    }

    @Test
    void previaRejeitaEsquemasDeUrlNaoPermitidosAntesDeAcessarAOrigem() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ImageThumbnailLoader.load("file:///C:/private/image.png", 100, 100)
        );
    }

    @Test
    void previaCarregaImagemHttpESeAjustaAoTamanhoSolicitado() throws Exception {
        BufferedImage image = new BufferedImage(10, 5, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", imageBytes));

        try (ServerSocket server = new ServerSocket(0)) {
            Thread responder = new Thread(() -> responderImagem(server, imageBytes.toByteArray()));
            responder.start();

            ImageIcon preview = ImageThumbnailLoader.load(
                    "http://127.0.0.1:" + server.getLocalPort() + "/image.png",
                    80,
                    40
            );

            responder.join(5000);
            assertFalse(responder.isAlive());
            assertEquals(80, preview.getIconWidth());
            assertEquals(40, preview.getIconHeight());
        }
    }

    private void responderImagem(ServerSocket server, byte[] imageBytes) {
        try (Socket client = server.accept();
             BufferedReader request = new BufferedReader(
                     new InputStreamReader(client.getInputStream(), StandardCharsets.US_ASCII)
             );
             OutputStream response = client.getOutputStream()) {
            while (true) {
                String line = request.readLine();
                if (line == null || line.isEmpty()) {
                    break;
                }
            }
            String headers = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: image/png\r\n"
                    + "Content-Length: " + imageBytes.length + "\r\n"
                    + "Connection: close\r\n\r\n";
            response.write(headers.getBytes(StandardCharsets.US_ASCII));
            response.write(imageBytes);
            response.flush();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
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
