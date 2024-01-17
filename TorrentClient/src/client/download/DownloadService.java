package client.download;

import client.LogHandler;
import client.connection.ServerConnection;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DownloadService {
    private static final Logger LOGGER = Logger.getLogger(DownloadService.class.getName());

    private final ExecutorService executorService;

    private final ServerConnection serverConnection;
    private final String registerCommandPrefix;

    public DownloadService(ServerConnection serverConnection, String registerCommandPrefix) {
        this.serverConnection = serverConnection;
        this.registerCommandPrefix = registerCommandPrefix;
        executorService = Executors.newFixedThreadPool(5);
        LogHandler.registerLogger(LOGGER, "logs/downloads.log");

    }

    public void executeCommand(String command) throws DownloadException, IOException {
        DownloadData downloadData;
        try {
            downloadData = DownloadData.parseDownloadCommand(command);
        } catch (DownloadException | IOException e) {
            LOGGER.log(Level.SEVERE, "Parsing of the command failed: " + e.getMessage(), e);
            throw e;
        }

        DownloadProcess downloadProcess =
                new DownloadProcess(downloadData, serverConnection, registerCommandPrefix);
        executorService.submit(downloadProcess);
        LOGGER.info("Download command executed.");
    }

    public void awaitAllSubmittedAndShutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                forceShutdown();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Download service shut down.");
    }

    private void forceShutdown() {
        executorService.shutdownNow();
    }
}
