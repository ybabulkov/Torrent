package client;

import client.connection.ConnectionException;
import client.connection.ServerConnection;
import client.lib.Tuple;
import client.miniserver.MiniServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.Scanner;

public class CommandUI {
    public static final String SERVER_IP = "localhost";
    public static final int SERVER_PORT = 5555;
    private final InputStream in;
    private final PrintStream out;

    public CommandUI(InputStream in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public String connectToServer(ServerConnection connection) {
        try {
            connection.connect();
        } catch (ConnectionException e) {
            out.println("Unable to connect to the server. Please check your connection and try again.");
        }

        String username;
        do {
            Scanner scanner = new Scanner(in);
            out.println("Enter username: ");
            username = scanner.nextLine();

            if (username.isEmpty()) {
                out.println("The username cannot be empty. Please try again.");
                continue;
            }

            try {
                String reply = connection.sendMessage(String.format("connect %s", username));
                if (reply.equals("Successful"))
                    return username;
                else {
                    out.println("The username is already taken. Please try another one.");
                }
            } catch (ConnectionException e) {
                out.println("Unable to connect to the server. Please check your connection and try again.");
                System.exit(-1);
            }
        } while(true);
    }

    public Tuple<MiniServer, Integer> initializeMiniServerWithPort() {
        int port;
        MiniServer miniServer;
        do {
            Scanner scanner = new Scanner(in);
            port = scanner.nextInt();

            try (ServerSocket serverSocket = new ServerSocket(port)) {
                miniServer = new MiniServer(serverSocket, port);
                return new Tuple<>(miniServer, port);
            } catch (IOException e) {
                out.println("This port is already in use. Please provide new port.");
            }
        } while(true);
    }

    public ClientActions setUpClient(String serverIp, int serverPort) {
        Scanner scanner = new Scanner(in);

        out.println("Enter username: ");
        String username = scanner.nextLine();

        out.println("Enter address file: ");
        String addressFile = scanner.nextLine();

        out.println("Enter port for the mini server: ");
        int port = scanner.nextInt();

        ClientProperties clientProperties =
                ClientProperties.setUpDependencies(serverIp, serverPort, addressFile, port, username);

        return new ClientActions(clientProperties, port);
    }

    private boolean consoleReader(Scanner scanner, ClientActions clientActions) {
        String command = scanner.nextLine();

        if (command.equals("disconnect")) {
            out.println(clientActions.stop());
            return false;
        }
        if (command.startsWith("download")) {
            out.println(clientActions.download(command));
            return true;
        }
        if (command.startsWith("register")) {
            out.println(clientActions.register(command));
            return true;
        }
        out.println(clientActions.serverCommand(command));
        return true;
    }

    public void run(ClientActions clientActions) {
        out.println(clientActions.start());
        Scanner scanner = new Scanner(in);

        boolean active = true;
        while (active) {
            active = consoleReader(scanner, clientActions);
        }

        scanner.close();
    }

    public static void main(String[] args) {
        ServerConnection serverConnection = new ServerConnection(SERVER_IP, SERVER_PORT);
        CommandUI commandUI = new CommandUI(System.in, System.out);
        String username = commandUI.connectToServer(serverConnection);
        Tuple<MiniServer, Integer> miniServerInfo = commandUI.initializeMiniServerWithPort();

        commandUI.run(commandUI.setUpClient("localhost", 5555));
    }
}
