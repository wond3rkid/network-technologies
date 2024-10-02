package network_technologies;

import java.nio.channels.SocketChannel;

public class DownloadInfo {
    private final SocketChannel channel;
    private long startTime;
    private long lastCheckedTime;
    private long bytesReceived;
    private long bytesLastPeriod;

    public DownloadInfo(SocketChannel channel) {
        this.channel = channel;
        this.startTime = System.currentTimeMillis();
        this.lastCheckedTime = startTime;
        this.bytesReceived = 0;
        this.bytesLastPeriod = 0;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    public long getBytesLastPeriod() {
        return bytesLastPeriod;
    }

    public void addBytes(long bytes) {
        bytesReceived += bytes;
        bytesLastPeriod += bytes;
    }

    public void resetLastPeriod() {
        bytesLastPeriod = 0;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getLastCheckedTime() {
        return lastCheckedTime;
    }

    public void updateLastCheckedTime() {
        this.lastCheckedTime = System.currentTimeMillis();
    }
}
