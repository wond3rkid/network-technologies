package network_technologies;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class Main {
    public static final int PORT = 8088;

    public void main(String[] args) {
        InetSocketAddress address;
        try {
            address = new InetSocketAddress(InetAddress.getByName("224.0.0.1"), PORT);
        } catch (UnknownHostException e) {
            // logger
            return;
        }
    }
}