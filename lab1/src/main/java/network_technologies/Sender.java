package network_technologies;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * This class is responsible for sending multicast UDP packets.
 */
public class Sender implements Runnable {
    private final InetSocketAddress address;
    private final Logger LOGGER = LogManager.getLogger("SENDER");

    /**
     * Constructor to initialize the Sender with the address.
     *
     * @param address the address to multicast packets to
     */
    public Sender(InetSocketAddress address) {
        this.address = address;
    }

    /**
     * The method to run when the thread starts.
     */
    @Override
    public void run() {
        LOGGER.info("SENDER STARTED");
        try (DatagramSocket sendingSocket = new DatagramSocket()) {
            String message = "DISCOVER";
            byte[] buffer = message.getBytes();
            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address); // Создаем пакет
                sendingSocket.send(packet);
                LOGGER.info("Sending packet to multicast address: {}", address);
                Thread.sleep(Config.TIMEOUT);
            }
        } catch (InterruptedException | IOException e) {
            LOGGER.error("We get exception: {}", e.getMessage());
        }
    }
}
