package network_technologies;

public class Client {
    private final String serverIp;
    private final int serverPort;
    private final String path;

    public Client(String serverIp, int serverPort, String path) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.path = path;
    }
}
