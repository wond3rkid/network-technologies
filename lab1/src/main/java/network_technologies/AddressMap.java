package network_technologies;

import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.LogManager;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import static network_technologies.Config.DELETE_TIMEOUT;

public class AddressMap {
    private final Map<SocketAddress, Long> addresses = new HashMap<>();
    private final Logger LOGGER = LogManager.getLogger("ADDRESS_MAP");

    public AddressMap() {
    }

    /**
     * Print all addresses and their respective timestamps.
     */
    public void printAddresses() {
        LOGGER.info("Printing addresses");
        for (Map.Entry<SocketAddress, Long> entry : addresses.entrySet()) {
            LOGGER.info("SocketAddress: {} | Time: {}", entry.getKey(), entry.getValue());
        }
    }

    /**
     * Add a socket address to the map with the current time.
     *
     * @param socketAddress the address to add
     * @param l             the current time (timestamp)
     */
    public void add(SocketAddress socketAddress, long l) {
        LOGGER.info("We adding: {} - {}", socketAddress, l);
        addresses.put(socketAddress, l);
    }

    /**
     * Remove socket addresses that have been inactive for longer than DELETE_TIMEOUT.
     */
    public void delete() {
        LOGGER.info("We trying to delete smth");
        addresses.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > DELETE_TIMEOUT);
    }
}
