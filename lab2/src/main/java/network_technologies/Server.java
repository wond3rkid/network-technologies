package network_technologies;

import java.io.RandomAccessFile;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class Server implements Runnable {
    private final int PORT;
    private final String path = "resources/";
    ServerSocketChannel serverSocketChannel;
    Selector selector;
    RandomAccessFile file;

    public Server(int port) {
        PORT = port;
    }

    @Override
    public void run() {

    }

}
