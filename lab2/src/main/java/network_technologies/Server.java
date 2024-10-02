package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class Server implements Runnable {
    private final Logger LOGGER = LogManager.getLogger("SERVER");
    private final int PORT;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private final CountDownLatch latch;
    private final DownloadController tracker = new DownloadController();
    private int fileSize;
    private String fileName;

    public Server(int port, CountDownLatch latch) {
        Thread trackerTh = new Thread(tracker);
        trackerTh.start();
        this.PORT = port;
        this.latch = latch;
        LOGGER.info("Server started");
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
                int readyChannels = selector.select(3000);
                if (readyChannels == 0) continue;
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        handleAcceptable();
                    } else if (key.isReadable()) {
                        handleReadable(key);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed from server selector", e);
        }
        //todo finally to close all resoures
    }

    private void handleAcceptable() throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ, ServerStatus.WAITING_INIT_MESSAGE);
        tracker.addDownloadInfo(socketChannel);
        LOGGER.info("Accepted connection from {}", socketChannel.getRemoteAddress().toString());
    }

    private void handleReadable(SelectionKey key) throws IOException {
        ServerStatus status = (ServerStatus) key.attachment();
        switch (status) {
            case WAITING_INIT_MESSAGE -> receiveInitMessage(key);
            case WAITING_PIECE -> receivePiece(key);
        }
    }

    private void receiveInitMessage(SelectionKey key) throws IOException {
        LOGGER.info("Handling init message from {}", ((SocketChannel) key.channel()).getRemoteAddress());
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(20);
        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            LOGGER.info("Client disconnected from: {}", channel.getRemoteAddress());
            channel.close();
            return;
        }
        buffer.flip();
        byte[] nameBytes = new byte[16];
        buffer.get(nameBytes);
        fileName = new String(nameBytes, StandardCharsets.UTF_8).trim();
        fileSize = buffer.getInt();
        key.attach(ServerStatus.WAITING_PIECE);
        LOGGER.info("Client received length: {} for file: {}", fileSize, fileName);
    }


    private void receivePiece(SelectionKey key) {
        LOGGER.info("Handling piece from the client");
    }
}
