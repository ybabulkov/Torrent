package client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogHandler {
    public static void registerLogger(Logger logger, String logPath) {
        try {
            Path path = Paths.get(logPath);
            Files.createDirectories(path.getParent());

            FileHandler handler = new FileHandler(logPath);
            handler.setLevel(Level.ALL);
            logger.addHandler(handler);
            logger.setUseParentHandlers(false);
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "Creating log handler failed: "
                    + exception.getMessage(), exception);
        }
    }
}
