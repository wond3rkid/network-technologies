package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadInfo {
    private static final Logger LOGGER = LogManager.getLogger("DownloadInfo");
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

    public void printStatus() throws IOException {
        long speed = getSpeed();
        // todo ?????
        LOGGER.info("Сlient: {}, Speed: {} bytes/s, Total bytes read: {}, File size: {}", socketChannel.getRemoteAddress().toString(), speed, totalBytesRead.get(), totalBytesRead.get());
    }
}
