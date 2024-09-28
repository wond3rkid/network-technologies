package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.CountDownLatch;

public class Server implements Runnable {
    private final Logger LOGGER = LogManager.getLogger("SERVER");
    private final int PORT;
    private final String path = "resources/";
    ServerSocketChannel serverSocketChannel;
    Selector selector;
    RandomAccessFile file;
    private final CountDownLatch latch;

    public Server(int port, CountDownLatch latch) {
        PORT = port;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            LOGGER.info("Server is listening on port: " + PORT);
            latch.countDown();
            // TODO infinity cycle
            while (true) {

            }
        } catch (IOException e) {
            LOGGER.error("Failed from server selector", e);
        }
    }

}
