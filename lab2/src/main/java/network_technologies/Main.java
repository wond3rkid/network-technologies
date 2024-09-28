package network_technologies;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private final static Logger LOGGER = LogManager.getLogger("MAIN");

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption("ip", true, "IP address");
        options.addOption("port", true, "Port number");
        options.addOption("path", true, "File path");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        String ip, path;
        int port;
        try {
            cmd = parser.parse(options, args);
            ip = cmd.getOptionValue("ip");
            port = Integer.parseInt(cmd.getOptionValue("port"));
            path = cmd.getOptionValue("path");

            LOGGER.info("ip:port {}:{}", ip, port);
            LOGGER.info("path:{}", path);
        } catch (ParseException e) {
            LOGGER.error("Error parsing command line arguments: {}", e.getMessage());
            return;
        } catch (NumberFormatException e) {
            LOGGER.error("Port must be an integer: {}", e.getMessage());
            return;
        }
        LOGGER.info("Starting threads for client and server");
        CountDownLatch latch = new CountDownLatch(1);
        Client client = new Client(ip, port, path, latch);
        Server server = new Server(port, latch);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(server);
        executor.execute(client);
        executor.shutdown();
    }
}