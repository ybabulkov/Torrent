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
    private final Path addressFile;

    public FileData(String addressFilePath) {
        addressFile = Paths.get(addressFilePath);
    }

    public Writer newWriter() throws IOException {
        return Files.newBufferedWriter(addressFile);
    }

    public Reader newReader() throws IOException {
        return Files.newBufferedReader(addressFile);
    }

    public long getSize() throws IOException {
        return Files.size(addressFile);
    }

    public InputStream newInputStream() throws IOException {
        return Files.newInputStream(addressFile);
    }

    public OutputStream newOutputStream() throws IOException {
        return Files.newOutputStream(addressFile);
    }
}
