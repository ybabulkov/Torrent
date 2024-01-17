package client;

import client.connection.ConnectionException;
import client.download.DownloadException;
import client.lib.CommandExtractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    public String download(String command) {
        String response = serverCommand(command);
        String responseCommand = CommandExtractor.extractCommandPrefix(response);
        if(!responseCommand.equals("download")) {
            return response;
        }

        try {
            properties.downloadService().executeCommand(response);
        } catch (DownloadException | IOException e) {
            LOGGER.log(Level.SEVERE, "Downloading failed: " + e.getMessage(), e);
            return e.getMessage();
        }
        return "Download request sent!";
    }

    public String register(String command) {
        String[] words = command.split(" ");
        List<String> files = Arrays.asList(words).subList(1, words.length);

        List<String> existingFiles = files.stream()
                .peek(fileName -> {
                    if (!Files.exists(Paths.get(fileName))) {
                        System.out.println(fileName + " does not exist.");
                    }
                })
                .filter(fileName -> Files.exists(Paths.get(fileName)))
                .collect(Collectors.toList());

        String newCommand = String.format("register %d %s ", userPort, properties.username()) + String.join(" ", existingFiles);

        return serverCommand(newCommand);
    }

    public String unregister(String command) {
        return serverCommand(command.replaceFirst(" ", String.format(" %s ", properties.username())));
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
