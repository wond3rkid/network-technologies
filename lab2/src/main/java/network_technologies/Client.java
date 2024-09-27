package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Client implements Runnable {
    private final Logger LOGGER = LogManager.getLogger("CLIENT");
    private final String serverIp;
    private final int serverPort;
    private final String path;
    private Selector selector;

    public Client(String serverIp, int serverPort, String path) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.path = path;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            LOGGER.error("Error to open selector");
        }
    }

    @Override
    public void run() {
        try {
            // TODO infinity cycle
            while (true) {
                selector.select();
            }
        } catch (IOException e) {
            LOGGER.error("Failed with selector", e);
        }

    }
    // TODO hash map ???
    private void connectToServer() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(serverIp, serverPort));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }

}
