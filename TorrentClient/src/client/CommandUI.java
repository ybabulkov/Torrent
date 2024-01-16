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
            out.print("Enter username: ");
            username = scanner.nextLine();

            if (username.isEmpty()) {
                out.println("The username cannot be empty. Please try again.");
                continue;
            }

            try {
                String reply = connection.sendMessage(String.format("connect %s", username)).trim();
                if (reply.equals("Successful"))
                    return username;
                else {
                    out.println("The username is already taken. Please try another one.");
                }
            } catch (ConnectionException e) {
                e.printStackTrace();
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
            do {
                try {
                    out.print("Enter a port number: ");
                    port = scanner.nextInt();
                } catch (java.util.InputMismatchException e) {
                    out.println("Invalid input. Please enter a valid integer for the port.");
                    scanner.nextLine();
                    continue;
                }
                if (port < 0 || port > 65535) {
                    out.println("Invalid port number. The port should be between 0 and 65535");
                    continue;
                }
                break;
            } while (true);

            try {
                ServerSocket serverSocket = new ServerSocket(port);
                miniServer = new MiniServer(serverSocket, port);
                return new Tuple<>(miniServer, port);
            } catch (IOException e) {
                out.println("This port is already in use. Please provide new port.");
            }
        } while(true);
    }

    public ClientActions setUpClient(ServerConnection serverConnection, String username,
                                     MiniServer miniServer, int port) {

        ClientProperties clientProperties =
                ClientProperties.setUpDependencies(serverConnection, username, miniServer, port);

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

        commandUI.run(commandUI.setUpClient(serverConnection, username,
                miniServerInfo.getFirst(), miniServerInfo.getSecond()));
    }
}
