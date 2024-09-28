package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            // TODO нормальный цикл
            while (true) {
                selector.select();
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

    private void handleAcceptable() throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        LOGGER.info("Accepted connection from {}", socketChannel.getRemoteAddress().toString());
    }

    private void handleReadable(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();

        try {
            ByteBuffer buffer = ByteBuffer.allocate(20);
            int bytesRead = channel.read(buffer);

            if (bytesRead == -1) {
                LOGGER.info("Client disconnected from: {}", channel.getRemoteAddress());
                channel.close();
                return;
            }

            buffer.flip();

            byte[] fileNameBytes = new byte[16];
            buffer.get(fileNameBytes);
            String fileName = new String(fileNameBytes, StandardCharsets.UTF_8).trim();
            int fileSize = buffer.getInt();

            LOGGER.info("Received file: {} (size: {} bytes)", fileName, fileSize);

            ByteBuffer fileBuffer = ByteBuffer.allocate(fileSize);
            int totalBytesRead = 0;

            while (totalBytesRead < fileSize) {
                int bytesReadFromFile = channel.read(fileBuffer);
                if (bytesReadFromFile == -1) {
                    LOGGER.info("Client disconnected while reading file data: {}", channel.getRemoteAddress());
                    channel.close();
                    return;
                }
                totalBytesRead += bytesReadFromFile;
                LOGGER.info("Read {} bytes from channel; Total bytes read: {}", bytesReadFromFile, totalBytesRead);
            }

            fileBuffer.flip();
            Path outputPath = Paths.get("received_" + fileName);
            Files.write(outputPath, fileBuffer.array());
            LOGGER.info("File {} has been saved successfully", outputPath.toString());

        } catch (IOException e) {
            LOGGER.error("Error handling channel: {}", e.getMessage());
            try {
                channel.close();
            } catch (IOException ex) {
                LOGGER.error("Error closing channel: {}", ex.getMessage());
            }
        }
    }

}
