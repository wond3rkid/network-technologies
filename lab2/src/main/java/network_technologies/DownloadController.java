package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadController implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger("DownloadController");
    private final Map<SocketChannel, DownloadInfo> downloadInfos = new ConcurrentHashMap<>();
    private final long INTERVAL = 3000;

    public DownloadController() {    }

    public void addDownloadInfo(SocketChannel channel) {
        downloadInfos.put(channel, new DownloadInfo(channel));
    }

    public void deleteDownloadInfo(SocketChannel channel) {
        downloadInfos.remove(channel);
    }

    public void addBytesRead(SocketChannel channel, long bytesRead) {
        DownloadInfo downloadInfo = downloadInfos.get(channel);
        if (downloadInfo != null) {
            downloadInfo.addBytesRead(bytesRead);
        }
    }

    @Override
    public void run() {
        while (true) {
            for (DownloadInfo downloadInfo : downloadInfos.values()) {
                try {
                    downloadInfo.printStatus();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                Thread.sleep(INTERVAL);
            } catch (InterruptedException e) {
                LOGGER.error("Error in download monitor thread: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }
}
