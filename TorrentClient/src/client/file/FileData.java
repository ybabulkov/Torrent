package client.file;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileData {
    private final Path addressPath;

    public FileData(String addressFilePath) {
        addressPath = Paths.get(addressFilePath);
    }

    public FileData(Path addressFilePath) {
        addressPath = addressFilePath;
    }

    public Writer newWriter() throws IOException {
        return Files.newBufferedWriter(addressPath);
    }

    public long getSize() throws IOException {
        return Files.size(addressPath);
    }

    public InputStream newInputStream() throws IOException {
        return Files.newInputStream(addressPath);
    }

    public OutputStream newOutputStream() throws IOException {
        return Files.newOutputStream(addressPath);
    }
}
