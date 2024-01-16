package client.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
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

    public Reader newReader() throws IOException {
        return Files.newBufferedReader(addressPath);
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
