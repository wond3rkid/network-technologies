package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static network_technologies.Config.*;

/**
 * The main class that initializes and starts the multicast sender and receiver.
 */

public class Main {
    private static final Logger LOGGER = LogManager.getLogger("MAIN");

    public static void main() {
        InetSocketAddress address;
        try {
            address = new InetSocketAddress(InetAddress.getByName("224.0.0.1"), PORT);
            LOGGER.info("Address: {}", address);
        } catch (UnknownHostException e) {
            LOGGER.error("Unknown host: {}", e.getMessage());
            return;
        }
        AddressMap addressMap = new AddressMap();
        LOGGER.info("Starting threads");
        Thread receiver = new Thread(new Receiver(address, addressMap));
        Thread sender = new Thread(new Sender(address));
        receiver.start();
        sender.start();
    }
}