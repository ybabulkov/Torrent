package client;

import client.file.FileData;
import client.address.AddressHandler;
import client.address.AddressUpdaterThread;
import client.connection.ServerConnection;
import client.download.DownloadService;
import client.miniserver.MiniServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public record ClientProperties(DownloadService downloadService,
                               MiniServer miniServer,
                               AddressUpdaterThread addressUpdaterThread,
                               ServerConnection serverConnection,
                               String username) {

    private static String getRegisterCommandPrefix(int userPort, String username) {
        return "register " + userPort + " " + username + " ";
    }

    public static Path createAddressFile(String username) {
        String addressDirectory = "address";
        String fileName = String.format("%s.txt", username);
        Path addressPath = Paths.get(String.format("%s\\%s", addressDirectory, fileName));

        if(Files.exists((addressPath))) {
            try {
                Files.delete(addressPath);
            } catch (IOException e) {
                //TODO: Exception + logger maybe
                System.out.println("Failed to delete the existing file: " + e.getMessage());
                return null;
            }
        }

        try {
            Files.createDirectories(addressPath.getParent());
            Files.createFile(addressPath);

            return addressPath;
        } catch (IOException e) {
            //TODO: Log error
            System.out.println("Failed to create address file: " + e.getMessage());
            return null;
        }
    }

    public static ClientProperties setUpDependencies(ServerConnection serverConnection, String username,
                                                     MiniServer miniServer, int port) {

        Path addressPath = createAddressFile(username);

        AddressHandler addressHandler = new AddressHandler(new FileData(addressPath), serverConnection);
        AddressUpdaterThread addressUpdaterThread = new AddressUpdaterThread(addressHandler);
        addressUpdaterThread.setDaemon(true);

        DownloadService downloadService =
                new DownloadService(addressHandler, serverConnection, getRegisterCommandPrefix(port, username));

        return new ClientProperties(downloadService, miniServer, addressUpdaterThread, serverConnection, username);
    }
}
