package network_technologies;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class AddressMap {
    private final Map<InetSocketAddress, Long> addresses = new HashMap<>();

    public AddressMap() {
    }

    public void addAddress(InetSocketAddress address) {
        addresses.put(address, System.currentTimeMillis());
    }

    public void printAddresses() {
        for (Map.Entry<InetSocketAddress, Long> entry : addresses.entrySet()) {
            System.out.println(STR."\{entry.getKey()}: \{entry.getValue()}");
        }
    }

    // TODO deleter
}
