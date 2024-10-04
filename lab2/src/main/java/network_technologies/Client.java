package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public class Client implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger("CLIENT");
    private final String serverIp;
    private final int serverPort;
    private SocketChannel socketChannel;
    private long fileSize;
    private String fileName;
    private FileInputStream fileInputStream;
    CountDownLatch countDownLatch;

    public Client(String serverIp, int port, String filePath, CountDownLatch latch) throws IOException {
        this.serverIp = serverIp;
        this.serverPort = port;
        File file = new File(filePath);
        this.fileSize = file.length();
        this.fileName = file.getName();
        this.fileInputStream = new FileInputStream(file);
        countDownLatch = latch;
    }

    @Override
    public void run() {
        try {
            countDownLatch.await();
            LOGGER.info("Starting client");
            socketChannel = SocketChannel.open(new InetSocketAddress(serverIp, serverPort));
            handleFile();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error in client: {}", e.getMessage());
        } finally {
            closeResources();
        }
    }

    public void handleFile() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        if (fileNameBytes.length > 16) {
            throw new IllegalArgumentException("File name exceeds maximum length of 16 bytes");
        }
        buffer.put(fileNameBytes);
        for (int i = fileNameBytes.length; i < 16; i++) {
            buffer.put((byte) 0);
        }
        buffer.putInt((int) fileSize);
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();
        int bytesRead = socketChannel.read(buffer);
        if (bytesRead == -1) {
            throw new IOException("Disconnected from server");
        }
        String response = new String(buffer.array(), 0, bytesRead, StandardCharsets.UTF_8);
        if (!response.equals("ok")) {
            throw new IOException("Expected 'ok' from server, but got: " + response);
        }
        int chunkSize = 8192;
        buffer = ByteBuffer.allocate(chunkSize);
        long totalBytesSent = 0;
        while (totalBytesSent < fileSize) {
            buffer.limit(bytesRead);
            socketChannel.write(buffer);
            buffer.clear();
            totalBytesSent += bytesRead;
        }

        LOGGER.info("Total : {}", totalBytesSent);
        buffer.clear();
        buffer.put("done".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();
        bytesRead = socketChannel.read(buffer);
        response = new String(buffer.array(), 0, bytesRead, StandardCharsets.UTF_8);
        if (response.equals("done")) {
            LOGGER.info("File transferred successfully");
        } else {
            LOGGER.error("File transfer failed");
            throw new IOException("Expected 'done' from server, but got: " + response);
        }
    }

    private void closeResources() {
        try {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (socketChannel != null && socketChannel.isOpen()) {
                socketChannel.close();
            }
        } catch (IOException e) {
            LOGGER.error("Error closing resources: {}", e.getMessage());
        }
    }
}
