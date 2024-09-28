package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

public class Client implements Runnable {
    private final Logger LOGGER = LogManager.getLogger("CLIENT");
    private final String serverIp;
    private final int serverPort;
    private final String path;
    private final CountDownLatch latch;

    public Client(String serverIp, int serverPort, String path, CountDownLatch latch) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.path = path;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            latch.await();
            LOGGER.info("Client started to run");
            connectToServer();
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    // TODO hash map ???
    private void connectToServer() throws IOException {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress(serverIp, serverPort));
            socketChannel.configureBlocking(false);
        }
    }

    private void finishServerConnection() throws IOException {

    }

}
