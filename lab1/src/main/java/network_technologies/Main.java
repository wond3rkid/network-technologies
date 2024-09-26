package network_technologies;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static network_technologies.Config.*;

public class Main {


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