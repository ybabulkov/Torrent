package client.download;

import client.connection.ConnectionException;
import client.connection.ServerConnection;
import client.file.FileData;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadProcess implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(DownloadService.class.getName());
    private static final int CHUNK_SIZE = 1024;

    private final DownloadData downloadData;
    private final ServerConnection serverConnection;
    private final String registerCommandPrefix;

    public DownloadProcess(DownloadData downloadData, ServerConnection serverConnection, String registerCommandPrefix) {
        this.serverConnection = serverConnection;
        this.registerCommandPrefix = registerCommandPrefix;
        this.downloadData = downloadData;
    }

    private void registerFileOnServer(String file) {
        try {
            serverConnection.sendMessage(registerCommandPrefix + file);
        } catch (ConnectionException e) {
            LOGGER.log(Level.SEVERE, "Registration of the file on the server failed: " + e.getMessage(), e);
        }
        String result = "File " + file + " successfully downloaded and registered.";
        LOGGER.info(result);
        System.out.println(result);
    }

    private void receiveFile(DataInputStream socketInput, OutputStream fileOutput) throws IOException {
        long fileSize = socketInput.readLong();
        int bytesCount;
        byte[] buffer = new byte[CHUNK_SIZE];

        while (fileSize > 0
                && (bytesCount = socketInput.read(buffer, 0, (int) Math.min(fileSize, buffer.length))) != -1) {
            fileOutput.write(buffer, 0, bytesCount);
            fileSize -= bytesCount;
        }
    }

    private void download() {
        try (Socket socket = new Socket(downloadData.ip(), downloadData.port());
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                DataInputStream socketInput = new DataInputStream(socket.getInputStream())) {
            writer.println(downloadData.serverPath());

            FileData file = new FileData(downloadData.clientPath());
            OutputStream fileOutput = file.newOutputStream();

            receiveFile(socketInput, fileOutput);
            fileOutput.close();

            registerFileOnServer(downloadData.clientPath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Download failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        download();
    }
}
