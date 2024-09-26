package network_technologies;

import java.net.InetSocketAddress;

public class Sender implements Runnable {
    private final InetSocketAddress address;

    public Sender(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public void run() {

    }
}
