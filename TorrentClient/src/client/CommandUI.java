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

    private String extractCommandPrefix(String command) {
        int spaceIndex = command.trim().indexOf(' ');
        return (spaceIndex != -1) ? command.substring(0, spaceIndex) : command;
    }

    private void printHelp() {
        System.out.println("\nAvailable Commands:");
        System.out.println("1. 'register <file1, file2, file3, ..., fileN>' - announce files for download.");
        System.out.println("2. 'unregister <file1, file2, file3, ..., fileN>' - declare files that can't be downloaded.");
        System.out.println("3. 'list-files' - view available files and the users from which they can be downloaded.");
        System.out.println("4. 'download <user> <path to file on user> [<path to save>]' - download " +
                "<path to file on user> from <user> in <path to save>. If <path to save> is omitted, " +
                "store it in the current directory.");
        System.out.println("5. 'disconnect' - disconnect from the server.");
    }

    private boolean consoleReader(Scanner scanner, ClientActions clientActions) {
        printHelp();
        String command = scanner.nextLine();

        boolean keepActive = true;
        String response;

        String commandPrefix = extractCommandPrefix(command);

        switch (commandPrefix) {
            case "disconnect" -> {
                response = clientActions.stop();
                keepActive = false;
            }
            case "download" -> response = clientActions.download(command);
            case "register" -> response = clientActions.register(command);
            case "unregister" -> response = clientActions.unregister(command);
            default -> response = clientActions.serverCommand(command);
        }

        out.println(response);
        return keepActive;
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
        System.out.println("Welcome to the Kol-Yo Torrent Application!");

        ServerConnection serverConnection = new ServerConnection(SERVER_IP, SERVER_PORT);
        CommandUI commandUI = new CommandUI(System.in, System.out);
        String username = commandUI.connectToServer(serverConnection);
        Tuple<MiniServer, Integer> miniServerInfo = commandUI.initializeMiniServerWithPort();

        commandUI.run(commandUI.setUpClient(serverConnection, username,
                miniServerInfo.getFirst(), miniServerInfo.getSecond()));
    }
}
