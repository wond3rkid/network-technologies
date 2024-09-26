package network_technologies;

import java.net.InetSocketAddress;

public class Receiver implements Runnable {
    private final InetSocketAddress address;
    private final AddressMap addressMap;

    public Receiver(InetSocketAddress address, AddressMap addressMap) {
        this.address = address;
        this.addressMap = addressMap;
    }

    @Override
    public void run() {

    }
}
