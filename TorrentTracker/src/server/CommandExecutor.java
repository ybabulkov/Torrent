package server;

import server.exceptions.InvalidUserException;
import server.exceptions.UserNotFoundException;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandExecutor {
    private static final int REGISTER_FILE_OFFSET = 3;
    private static final int UNREGISTER_FILE_OFFSET = 2;
    private static final String SINGLE_LINE_PREFIX = "1" + System.lineSeparator();

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final Map<SocketChannel, String> channelUserMap;
    private final ServerData serverData;

    CommandExecutor(ServerData serverData) {
        this.serverData = serverData;
        channelUserMap = new HashMap<>();
    }

    private Set<String> getFilesFromCommand(int offset, String[] words) {
        return new HashSet<>(Arrays.asList(words).subList(offset, words.length));
    }

    private boolean checkInvalidUser(SocketChannel channel, String username) {
        if (channelUserMap.containsKey(channel)) {
            return !channelUserMap.get(channel).equals(username);
        }
        channelUserMap.put(channel, username);
        return false;
    }

    private boolean isValid(String command) {
        String[] words = command.split(" ");
        return words.length > 1;
    }

    private String register(SocketChannel channel, String command, InetAddress ip) {
        String[] words = command.split(" ");
        String username = words[2];

        int port;
        try {
            port = Integer.parseInt(words[1]);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Parsing of the port failed: " + e.getMessage(), e);
            return "Could not retrieve port.";
        }

        if (checkInvalidUser(channel, username)) {
            return "This session is associated with another user.";
        }

        UserData userData = new UserData(ip.getHostAddress() + ":" + port,
                getFilesFromCommand(REGISTER_FILE_OFFSET, words));
        try {
            serverData.register(username, userData);
        } catch (InvalidUserException e) {
            LOGGER.log(Level.SEVERE, "Register failed: " + e.getMessage(), e);
            return e.getMessage();
        }

        return "File(s) successfully registered!";
    }

    private String unregister(SocketChannel channel, String command) {
        String[] words = command.split(" ");
        String username = words[1];

        if (checkInvalidUser(channel, username)) {
            return "This session is associated with another user.";
        }

        try {
            serverData.unregister(username, getFilesFromCommand(UNREGISTER_FILE_OFFSET, words));
        } catch (UserNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Unregister failed: " + e.getMessage(), e);
            return e.getMessage();
        }

        return "File(s) successfully unregistered!";
    }

    private String download(String command) {
        String[] words = command.split("\\s+");

        String address;
        try {
            address = serverData.getAddressOfFile(words[1]);
        } catch (FileNotFoundException e) {
            return e.getMessage();
        }

        return command.replaceFirst("\\s+", String.format(" %s ", address));
    }

    private String buildStringFrom(Set<String> lines) {
        StringBuilder builder = new StringBuilder();
        builder.append(lines.size()).append(System.lineSeparator());

        for (String line : lines) {
            builder.append(line).append(System.lineSeparator());
        }
        return builder.toString();
    }

    private String parseLines( String messageIfEmpty, Set<String> lines) {
        if(lines.isEmpty())
            return messageIfEmpty;

        return buildStringFrom(lines);
    }


    public String connect(String command) {
        String[] words = command.split(" ");
        String username = words[1];
        return !serverData.checkIfUsernameExists(username) ? "Successful" : "Unsuccessful";
    }

    private String extractCommandPrefix(String command) {
        int spaceIndex = command.indexOf(' ');
        return (spaceIndex != -1) ? command.substring(0, spaceIndex) : command;
    }

    public String execute(SocketChannel channel, String command, InetAddress ip) {
        String response = null;

        switch (extractCommandPrefix(command)) {
                case "register" -> {
                    if(isValid(command))
                        response = SINGLE_LINE_PREFIX + register(channel, command, ip);
                }
                case "unregister" -> {
                    if(isValid(command))
                        response = SINGLE_LINE_PREFIX + unregister(channel, command);
                }
                case "list-files" -> response =
                        parseLines(String.format("%sThere are no files registered!", SINGLE_LINE_PREFIX),
                                serverData.listFiles());
                case "list-addresses" -> response =
                        parseLines(String.format("%sThere are no addresses available!", SINGLE_LINE_PREFIX),
                                serverData.listAddresses());
                case "connect" -> {
                    if(isValid(command))
                        response = SINGLE_LINE_PREFIX + connect(command);
                }
                case "download" -> {
                    if(isValid(command))
                        response = SINGLE_LINE_PREFIX + download(command);
                }
                default -> response = SINGLE_LINE_PREFIX + "Unknown command!";
            }

        return response == null ? SINGLE_LINE_PREFIX + "Unknown command!" : response;
    }

    public void disconnect(SocketChannel channel) {
        if (channelUserMap.containsKey(channel)) {
            try {
                serverData.disconnect(channelUserMap.get(channel));
            } catch (UserNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Disconnect failed: " + e.getMessage()
                        + "SocketChannel hashcode: " + channel.hashCode(), e);
            }
            channelUserMap.remove(channel);
        }
    }
}
