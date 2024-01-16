package client.connection;

import client.LogHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerConnection {
    private static final Logger LOGGER = Logger.getLogger(ServerConnection.class.getName());

    private final String address;
    private final int port;

    private SocketChannel socketChannel;
    private BufferedReader reader;
    private PrintWriter writer;

    public ServerConnection(String address, int port) {
        this.address = address;
        this.port = port;
        LogHandler.registerLogger(LOGGER, "logs/connection.log");
    }

    public boolean isConnected() {
        return socketChannel != null && socketChannel.isConnected();
    }

    public void connect() throws ConnectionException {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(address, port));
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Connection failed: " + exception.getMessage(), exception);
            throw new ConnectionException(exception.getMessage(), exception);
        }

        reader = new BufferedReader(Channels.newReader(socketChannel, StandardCharsets.UTF_8));
        writer = new PrintWriter(Channels.newWriter(socketChannel, StandardCharsets.UTF_8), true);
        LOGGER.info("Connected to the server");
    }

    private void loadLines(StringBuilder stringBuilder, int linesCount) throws IOException {
        for (int i = 0; i < linesCount - 1; ++i) {
            stringBuilder
                    .append(reader.readLine())
                    .append(System.lineSeparator());
        }
        stringBuilder.append(reader.readLine());
    }

    private String serverReply() throws ConnectionException {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            int linesCount = Integer.parseInt(reader.readLine());
            if (linesCount > 0) {
                loadLines(stringBuilder, linesCount);
            }
        } catch (IOException | NumberFormatException exception) {
            LOGGER.log(Level.SEVERE, "Retrieving server reply failed: " + exception.getMessage(), exception);
            throw new ConnectionException(exception.getMessage(), exception);
        }
        return stringBuilder.toString();
    }

    public synchronized String sendMessage(String message) throws ConnectionException {
        if (isConnected()) {
            writer.println(message);
            return serverReply();
        }
        throw new ConnectionException("Not yet connected!");
    }

    public void close() throws ConnectionException {
        writer.close();
        try {
            reader.close();
            socketChannel.close();
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Connection closing failed: " + exception.getMessage(), exception);
            throw new ConnectionException(exception.getMessage(), exception);
        }
        LOGGER.info("Connection closed.");
    }
}
