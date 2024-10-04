package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class FileHandler implements Runnable {
    private final Logger LOGGER = LogManager.getLogger("FileHandler");
    private final SocketChannel socketChannel;
    private long fileSize;
    private long readSize = 0;
    private long lastReadSize = 0;
    private long startTime;

    public FileHandler(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void run() {
        try {
            handleConnection();
        } catch (IOException e) {
            LOGGER.error("Error handling connection: {}", e.getMessage());
        } finally {
            try {
                socketChannel.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close socket channel", e);
            }
        }
    }

    private void handleConnection() throws IOException {
        ByteBuffer metaBuffer = ByteBuffer.allocate(20);
        int bytesRead = socketChannel.read(metaBuffer);
        if (bytesRead == -1) {
            LOGGER.info("Client disconnected.");
            return;
        }
        metaBuffer.flip();

        byte[] nameBytes = new byte[16];
        metaBuffer.get(nameBytes);
        String fileName = new String(nameBytes, StandardCharsets.UTF_8).trim();

        fileSize = metaBuffer.getInt();
        LOGGER.info("Receiving file: {} of size: {}", fileName, fileSize);

        if (fileSize <= 0) {
            LOGGER.error("Invalid file size: {}", fileSize);
            return;
        }

        String destination = "resources/uploads/" + fileName;
        File directory = new File("resources/uploads");
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                LOGGER.info("Created directory: {}", directory.getAbsolutePath());
            } else {
                LOGGER.error("Failed to create directory: {}", directory.getAbsolutePath());
                return;
            }
        }

        File file = new File(destination);
        LOGGER.info("File path: {}", file.getAbsolutePath());
        startTimer();
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            ByteBuffer responseBuffer = ByteBuffer.wrap("ok".getBytes(StandardCharsets.UTF_8));
            socketChannel.write(responseBuffer);
            readDataFromSocket(fileOutputStream);

            LOGGER.info("File {} has been read: {} bytes", fileName, readSize);
        }
        LOGGER.info("Total time: {}", (System.currentTimeMillis() - startTime));
        LOGGER.info("Finished receiving file: {}", fileName);
        ByteBuffer doneResponseBuffer = ByteBuffer.wrap("done".getBytes(StandardCharsets.UTF_8));
        socketChannel.write(doneResponseBuffer);
    }

    private void readDataFromSocket(FileOutputStream fileOutputStream) throws IOException {
        ByteBuffer dataBuffer = ByteBuffer.allocate(8192);
        while (readSize < fileSize) {
            dataBuffer.clear();

            int bytesRead = socketChannel.read(dataBuffer);
            if (bytesRead < 0) {
                LOGGER.warn("End of stream reached - client may have disconnected.");
                break;
            }
            if (bytesRead == 0) {
                continue;
            }

            dataBuffer.flip();
            int bytesToWrite = dataBuffer.remaining();
            fileOutputStream.write(dataBuffer.array(), 0, bytesToWrite);
            readSize += bytesToWrite;
        }
    }

    private void startTimer() {
        LOGGER.info("Starting timer now");
        startTime = System.currentTimeMillis();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                printSpeed();
            }
        }, 3000, 3000);
    }

    private void printSpeed() {
        long now = System.currentTimeMillis();
        double totalSpeed = (double) readSize / (now - startTime) * 1000 / 1024 / 1024;
        double currentSpeed = (double) (readSize - lastReadSize) / 3 / 1024 / 1024;
        lastReadSize = readSize;
        LOGGER.info("Read: {} | Total speed: {} MB/s | Current speed: {} MB/s", readSize, totalSpeed, currentSpeed);
    }
}