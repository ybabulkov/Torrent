package client.address;

import client.LogHandler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AddressUpdaterThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(AddressUpdaterThread.class.getName());
    private final AddressHandler addressHandler;

    public AddressUpdaterThread(AddressHandler addressHandler) {
        this.addressHandler = addressHandler;
        LogHandler.registerLogger(LOGGER, "logs/addresses.log");
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                addressHandler.update();
                Thread.sleep(30_000);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Writing to the address file failed: " + e.getMessage(), e);
        } catch (AddressException e) {
            LOGGER.log(Level.SEVERE, "Connecting to the server failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Sleeping interrupted: " + e.getMessage(), e);
            interrupt();
        }
    }
}
