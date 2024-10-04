package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

public class Server implements Runnable {
    private final Logger LOGGER = LogManager.getLogger("SERVER");
    private final int PORT;
    private ServerSocketChannel serverSocketChannel;
    private final CountDownLatch latch;

    public Server(int port, CountDownLatch latch) {
        this.PORT = port;
        this.latch = latch;
        LOGGER.info("Server started");
    }

    @Override
    public void run() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            LOGGER.info("Server is listening on port: {}", PORT);
            latch.countDown();

            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    LOGGER.info("Accepted connection from {}", socketChannel.getRemoteAddress());
                    new Thread(new FileHandler(socketChannel)).start();
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to start server", e);
        } finally {
            try {
                if (serverSocketChannel != null) {
                    serverSocketChannel.close();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to close server socket channel", e);
            }
        }
    }
}
