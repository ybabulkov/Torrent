package client;

import client.file.FileData;
import client.address.AddressHandler;
import client.address.AddressUpdaterThread;
import client.connection.ServerConnection;
import client.download.DownloadService;
import client.miniserver.MiniServer;

import java.io.IOException;
import java.net.ServerSocket;

public record ClientProperties(DownloadService downloadService,
                               MiniServer miniServer,
                               AddressUpdaterThread addressUpdaterThread,
                               ServerConnection serverConnection) {

    private static String getRegisterCommandPrefix(int userPort, String username) {
        return "register " + userPort + " " + username + " ";
    }

    public static ClientProperties setUpDependencies(String serverIp, int serverPort,
                                                     String addressFile, int userPort, String username) {
        ServerConnection serverConnection = new ServerConnection(serverIp, serverPort);


        AddressHandler addressHandler = new AddressHandler(new FileData(addressFile), serverConnection);
        AddressUpdaterThread addressUpdaterThread = new AddressUpdaterThread(addressHandler);
        addressUpdaterThread.setDaemon(true);

        DownloadService downloadService =
                new DownloadService(addressHandler, serverConnection, getRegisterCommandPrefix(userPort, username));
        try (ServerSocket serverSocket = new ServerSocket(userPort)) {
            MiniServer miniServer = new MiniServer(serverSocket, userPort);
            return new ClientProperties(downloadService, miniServer, addressUpdaterThread, serverConnection);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
