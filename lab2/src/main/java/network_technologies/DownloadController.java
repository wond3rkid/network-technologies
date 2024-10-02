package network_technologies;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class DownloadController implements Runnable {
    private static final Logger LOGGER = Logger.getLogger("DownloadController");
    private final Map<SocketChannel, DownloadInfo> downloadInfos  = new ConcurrentHashMap<>();
    private final long INTERVAL = 3000;

    public DownloadController() {
    }

    public void addDownloadInfo(SocketChannel channel) {
        downloadInfos.put(channel, new DownloadInfo(channel));
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
                LOGGER.severe("Error in download monitor thread: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }
}
