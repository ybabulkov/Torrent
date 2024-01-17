package client.address;

import client.LogHandler;
import client.connection.ConnectionException;
import client.connection.ServerConnection;
import client.file.FileData;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AddressHandler {
    private static final Logger LOGGER = Logger.getLogger(AddressHandler.class.getName());

    private final FileData fileData;
    private final ServerConnection serverConnection;

    public AddressHandler(FileData fileData, ServerConnection serverConnection) {
        this.fileData = fileData;
        this.serverConnection = serverConnection;
        LogHandler.registerLogger(LOGGER, "logs/addresses.log");
    }

    public void update() throws IOException, AddressException {
        String addresses;
        try {
            addresses = serverConnection.sendMessage("list-addresses");
        } catch (ConnectionException e) {
            LOGGER.log(Level.SEVERE, "Getting addresses from server failed: " + e.getMessage(), e);
            throw new AddressException(e.getMessage(), e);
        }
        synchronized (fileData) {
            BufferedWriter fileWriter = new BufferedWriter(fileData.newWriter());
            fileWriter.write(addresses);
            fileWriter.flush();
            fileWriter.close();
        }
        LOGGER.info("Address file updated.");
    }
}
