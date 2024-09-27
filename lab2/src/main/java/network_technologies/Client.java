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

    public Client(String serverIp, int serverPort, String path) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.path = path;
    }

    @Override
    public void run() {
        try {
            // TODO infinity cycle
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    // TODO hash map ???
    private void connectToServer() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(serverIp, serverPort));
        socketChannel.configureBlocking(false);
    }

    private void finishServerConnection() throws IOException {

    }

}
