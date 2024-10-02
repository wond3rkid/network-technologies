package network_technologies;

import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class DownloadInfo {
    private static final Logger LOGGER = Logger.getLogger(DownloadInfo.class.getName());
    private final AtomicLong totalBytesRead;
    private final AtomicLong lastBytesRead;
    private final AtomicLong lastUpdateTime;
    private final SocketChannel socketChannel;

    public DownloadInfo(SocketChannel channel) {
        socketChannel = channel;
        totalBytesRead = new AtomicLong();
        lastBytesRead = new AtomicLong();
        lastUpdateTime = new AtomicLong(System.currentTimeMillis());
    }

    public void addBytesRead(long bytesRead) {
        totalBytesRead.addAndGet(bytesRead);
        lastBytesRead.addAndGet(bytesRead);
    }

    public long getSpeed() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastUpdateTime.get();
        if (elapsedTime <= 0) {
            return 0;
        }
        long speed = lastBytesRead.get() * 1000 / elapsedTime;
        lastBytesRead.set(0);
        lastUpdateTime.set(currentTime);
        return speed;
    }

    public long getTotalBytesRead() {
        return totalBytesRead.get();
    }

    public void printStatus() {
        long speed = getSpeed();
        // todo
        LOGGER.info("Client id: {}, Speed: {} bytes/s, Total bytes read: {}, File size: {}");
    }
}
