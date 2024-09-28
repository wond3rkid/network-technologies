package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
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
            LOGGER.info("Server is listening on port: {}", PORT);
            latch.countDown();
            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        handleAcceptable(key);
                    } else if (key.isReadable()) {
                        handleReadable(key);
                    }
                }
            }
        } catch (IOException e) {
            try {
                selector.close();
            } catch (IOException ex) {
                LOGGER.error("Failed to close selector", ex);
            }
            LOGGER.error("Failed from server selector", e);
        } finally {
            //close resources
        }
    }

    private void handleAcceptable(SelectionKey key) throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        LOGGER.info("Accepted connection from {}", socketChannel.getRemoteAddress().toString());
    }

    private void handleReadable(SelectionKey key) {
    }
}
