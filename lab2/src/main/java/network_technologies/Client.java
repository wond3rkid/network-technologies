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
    private int PIECE_COUNT;
    private final int PIECE_SIZE = 64 * 1024;
    private final int LAST_PIECE_SIZE;
    private PieceManager pieceManager;
    private ClientStatus clientStatus;
    private int INIT_BUFFER = 20;

    public Client(String serverIp, int serverPort, String path, CountDownLatch latch) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.filePath = path;
        this.latch = latch;
        clientStatus = ClientStatus.NO_CONNECTION;
        Path fpath = Paths.get(filePath);

        try {
            this.fileName = fpath.getFileName().toString();
            this.fileSize = Files.size(fpath);
        } catch (IOException e) {
            LOGGER.error("Error file handling: {}", e.getMessage());
        }

        LAST_PIECE_SIZE = (int) (fileSize % PIECE_SIZE);
        PIECE_COUNT = (int) (fileSize / PIECE_SIZE) + (LAST_PIECE_SIZE != 0 ? 1 : 0);
        LOGGER.info("ip: {} | port: {} | filename: {} | absolute path: {}",
                serverIp, serverPort, fileName, fpath.toAbsolutePath());
        LOGGER.info("file size: {} | PIECE_COUNT count : {} | piece size: {} | last piece size: {}", fileSize, PIECE_COUNT, PIECE_SIZE, LAST_PIECE_SIZE);
        pieceManager = new PieceManager(PIECE_COUNT);
    }

    @Override
    public void run() {
        try {
            latch.await();
            LOGGER.info("Client started to run");
            while (!pieceManager.isAllPiecesSent()) {
                switch (clientStatus) {
                    case NO_CONNECTION -> connectToServer();
                    case CONNECTED -> sendInitMessage();
                    case SENT_INIT_MESSAGE, SENT_PIECE -> sendPiece();
                }
            }
        } catch (IOException | InterruptedException e) {
            clientStatus = ClientStatus.NO_CONNECTION;
            LOGGER.error(e);
        }
    }

    private void connectToServer() throws IOException {
        LOGGER.info("Connecting to {}:{}", serverIp, serverPort);
        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(serverIp, serverPort));
        socketChannel.configureBlocking(false);
        socketChannel.finishConnect();
        clientStatus = ClientStatus.CONNECTED;
        LOGGER.info("Client connected to server");
    }

    private void sendInitMessage() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(INIT_BUFFER);
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
        int bytesWrite = socketChannel.write(buffer);
        LOGGER.info("Message sent successfully | bytes write : {}", bytesWrite);
        clientStatus = ClientStatus.SENT_INIT_MESSAGE;

    }


    private void sendPiece() {
        int piece = pieceManager.getPieceToSent();
        long bufferSize = 4 + (piece == PIECE_COUNT - 1 ? LAST_PIECE_SIZE : PIECE_SIZE);
        ByteBuffer buffer = ByteBuffer.allocate((int) bufferSize);
        buffer.putInt((int) (bufferSize - 4));
        // todo open file and get piece
    }

}
