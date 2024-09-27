package network_technologies;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private final static Logger LOGGER = LogManager.getLogger("MAIN");

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption("ip", true, "IP address");
        options.addOption("port", true, "Port number");
        options.addOption("path", true, "File path");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            String ip = cmd.getOptionValue("ip");
            int port = Integer.parseInt(cmd.getOptionValue("port"));
            String path = cmd.getOptionValue("path");

            LOGGER.info("ip:port {}:{}", ip, port);
            LOGGER.info("path:{}", path);
        } catch (ParseException e) {
            LOGGER.error("Error parsing command line arguments: {}",e.getMessage());
            System.exit(1);
        } catch (NumberFormatException e) {
            LOGGER.error("Port must be an integer: {}", e.getMessage());
            System.exit(1);
        }
    }
}