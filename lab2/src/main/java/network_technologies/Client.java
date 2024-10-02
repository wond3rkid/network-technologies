package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class Client implements Runnable {
    private final Logger LOGGER = LogManager.getLogger("CLIENT");
    private final CountDownLatch latch;
    private final String serverIp;
    private final int serverPort;
    private SocketChannel socketChannel;
    private final String filePath;
    private String fileName;
    private long fileSize;
    private int pieces;
    private final int SIZE = 64 * 1024;
    private final int LAST_SIZE;
    private Map<Integer, Boolean> piecesMap = new HashMap<>(); // TODO piece manager to not send same pieces twice

    public Client(String serverIp, int serverPort, String path, CountDownLatch latch) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.filePath = path;
        this.latch = latch;
        Path fpath = Paths.get(filePath);

        try {
            this.fileName = fpath.getFileName().toString();
            this.fileSize = Files.size(fpath);
        } catch (IOException e) {
            LOGGER.error("Error file handling: {}", e.getMessage());
        }

        LAST_SIZE = (int) (fileSize % SIZE);
        pieces = (int) (fileSize / SIZE) + (LAST_SIZE != 0 ? 1 : 0);
        LOGGER.info("ip: {} | port: {} | filename: {} | absolute path: {}",
                serverIp, serverPort, fileName, fpath.toAbsolutePath());
        LOGGER.info("file size: {} | pieces count : {} | piece size: {} | last piece size: {}", fileSize, pieces, SIZE, LAST_SIZE);
    }

    @Override
    public void run() {
        try {
            latch.await();
            LOGGER.info("Client started to run");
            connectToServer();
            sendMessage();
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e);
        }
    }

    private void connectToServer() throws IOException {
        LOGGER.info("Connecting to {}:{}", serverIp, serverPort);
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(serverIp, serverPort));
        socketChannel.configureBlocking(false);
        socketChannel.finishConnect();
        LOGGER.info("Client connected to server");
    }

    private void sendMessage() {
        // TODO piece manager >>>> send right pieces every time not sending all in one moment here
        long bufferSize = 16 + 4 + fileSize;
        LOGGER.info("buffer size : {} bytes", bufferSize);
        ByteBuffer buffer = ByteBuffer.allocate((int) bufferSize);

        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        if (fileNameBytes.length > 16) {
            throw new IllegalArgumentException("File name exceeds maximum length of 16 bytes");
        }
        buffer.put(fileNameBytes);
        for (int i = fileNameBytes.length; i < 16; i++) {
            buffer.put((byte) 0);
        }
        buffer.putInt((int) fileSize);

        try {
            byte[] fileData = Files.readAllBytes(Paths.get(filePath));
            buffer.put(fileData);
        } catch (IOException e) {
            LOGGER.error("Error reading file data: {}", e.getMessage());
            return;
        }
        buffer.flip();

        try {
            int bytesWrite = socketChannel.write(buffer);
            LOGGER.info("Message sent successfully | bytes write : {}", bytesWrite);
        } catch (IOException e) {
            LOGGER.error("Error sending message: {}", e.getMessage());
        } finally {
            try {
                socketChannel.close();
            } catch (IOException e) {
                LOGGER.error("Error closing socket: {}", e.getMessage());
            }
        }
    }
}
