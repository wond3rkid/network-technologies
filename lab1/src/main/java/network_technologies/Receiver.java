package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

import static network_technologies.Config.*;

public class Receiver implements Runnable {
    private final InetSocketAddress address;
    private final AddressMap addressMap;
    private final Logger LOGGER = LogManager.getLogger("RECEIVER");

    /**
     * Constructor to initialize Receiver with address and AddressMap.
     *
     * @param address    the multicast address to join
     * @param addressMap the AddressMap to store received addresses
     */

    public Receiver(InetSocketAddress address, AddressMap addressMap) {
        this.address = address;
        this.addressMap = addressMap;
    }

    /**
     * The method to run when the thread starts.
     */
    @Override
    public void run() {
        LOGGER.info("Starting receiver");
        try (MulticastSocket multicastReceiver = new MulticastSocket(PORT)) {
            DatagramPacket receivePacket = new DatagramPacket(new byte[256], 256);
            multicastReceiver.joinGroup(address.getAddress());
            multicastReceiver.setSoTimeout(DELETE_TIMEOUT);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    multicastReceiver.receive(receivePacket);
                    LOGGER.info("{}", receivePacket.getData());
                    addressMap.add(receivePacket.getSocketAddress(), System.currentTimeMillis());
                } catch (SocketTimeoutException e) {
                    LOGGER.error("Socket timed out");
                }
                addressMap.delete();
                addressMap.printAddresses();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
