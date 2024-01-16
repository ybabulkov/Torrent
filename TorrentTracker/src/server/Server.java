package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final String serverHost;
    private final int port;
    private static final int BUFFER_SIZE = 512;

    private final CommandExecutor commandExecutor;

    Server(String serverHost, int port) {
        this.port = port;
        this.serverHost = serverHost;

        ServerData serverData = new ServerData();
        commandExecutor = new CommandExecutor(serverData);
        createLogHandler();
    }

    private void createLogHandler() {
        try {
            FileHandler handler = new FileHandler("serverLog.log");
            handler.setLevel(Level.ALL);
            LOGGER.addHandler(handler);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Creating log handler failed: "
                    + exception.getMessage(), exception);
        }
    }

    private void acceptKey(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        System.out.println("Client connected.");
        LOGGER.info("Client connected. Socket channel hashcode: " + accept.hashCode());

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    private void endConnection(SocketChannel sc) throws IOException {
        commandExecutor.disconnect(sc);
        sc.close();
    }

    private boolean communicateThroughKey(SelectionKey key, ByteBuffer buffer) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        buffer.clear();
        try {
            int r = sc.read(buffer);
            if (r <= 0) {
                endConnection(sc);
                return false;
            }
        } catch (SocketException exception) {
            // Connection ended unexpectedly
            endConnection(sc);
            return false;
        }
        String command = new String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8);
        command = command.trim();
        InetAddress ip = sc.socket().getInetAddress();

        System.out.println("Client: " + command);
        String serverReply = commandExecutor.execute(sc, command, ip).trim() + System.lineSeparator();
        System.out.println("Server: " + serverReply);

        buffer.clear();
        buffer.put(serverReply.getBytes());
        buffer.flip();
        sc.write(buffer);
        return true;
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(serverHost, port));
            serverSocketChannel.configureBlocking(false);

            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

            while (!Thread.interrupted()) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        if (!communicateThroughKey(key, buffer)) {
                            break;
                        }
                    } else if (key.isAcceptable()) {
                        acceptKey(key, selector);
                    }
                    keyIterator.remove();
                }
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Problem with the server socket: " + e.getMessage(), e);
            System.out.println("There is a problem with the server socket");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server("localhost", 5555);
        server.start();
    }
}
