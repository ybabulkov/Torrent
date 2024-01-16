package client;

import client.connection.ConnectionException;
import client.download.DownloadException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientActions {
    private static final Logger LOGGER = Logger.getLogger(ClientActions.class.getName());

    private final ClientProperties properties;
    private final int userPort;

    public ClientActions(ClientProperties properties, int userPort) {
        this.properties = properties;
        this.userPort = userPort;
        LogHandler.registerLogger(LOGGER, "logs/client.log");
    }

    public String start() {
        properties.miniServer().start();
        if (!properties.miniServer().hasStarted) {
            throw new IllegalStateException("Mini server failed to start!");
        }

        try {
            properties.serverConnection().connect();
        } catch (ConnectionException e) {
            LOGGER.log(Level.SEVERE, "Connection to the server failed: " + e.getMessage(), e);
            return "Connection to the server failed!";
        }
        properties.addressUpdaterThread().start();
        return "Connected to the server!";
    }

    public String stop() {
        properties.miniServer().close();
        properties.downloadService().awaitAllSubmittedAndShutdown();
        try {
            properties.serverConnection().close();
        } catch (ConnectionException e) {
            LOGGER.log(Level.SEVERE, "Server connection closing failed: " + e.getMessage(), e);
            return "Disconnecting from the server failed!";
        }
        return "Disconnected from the server!";
    }

    private String insertPort(String command) {
        return command.replaceFirst(" ", " " + userPort + " ");
    }

    public String download(String command) {
        try {
            properties.downloadService().executeCommand(command);
        } catch (DownloadException e) {
            LOGGER.log(Level.SEVERE, "Downloading failed: " + e.getMessage(), e);
            return e.getMessage();
        }
        return "Download request sent!";
    }

    public String register(String command) {
        return serverCommand(insertPort(command));
    }

    public String serverCommand(String command) {
        try {
            return properties.serverConnection().sendMessage(command);
        } catch (ConnectionException e) {
            LOGGER.log(Level.SEVERE, "Server communication failed: " + e.getMessage(), e);
            return e.getMessage();
        }
    }
}
