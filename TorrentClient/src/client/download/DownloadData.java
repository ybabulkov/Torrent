package client.download;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;


public record DownloadData(String ip, int port, String serverPath, String clientPath) {
    private static AddressPair parseAddress(String address) throws DownloadException {
        String[] addressSplit = address.split(":");

        if (addressSplit.length != 2) {
            throw new DownloadException("Corrupted address!");
        }
        int port;
        try {
            port = Integer.parseInt(addressSplit[1]);
        } catch (NumberFormatException e) {
            throw new DownloadException("Corrupted address! Parsing port failed!", e);
        }
        return new AddressPair(addressSplit[0], port);
    }

    private static String getDownloadDirectoryPath(String fileName, String directory) throws IOException {
        Path fileDirectory;

        try {
            fileDirectory = Paths.get(directory);
        } catch (InvalidPathException e) {
            throw new NotDirectoryException("The specified directory is invalid!");
        }

        Files.createDirectories(fileDirectory);

        if (!Files.isWritable(fileDirectory)) {
            throw new IOException("The specified directory is not writable!");
        }

        String filePathString = String.format("%s\\%s", directory, fileName);
        Path filePath = Paths.get(filePathString);

        if(Files.exists(filePath)) {
            System.out.println("A file with the same name already exists in the specified directory!\n" +
                    "Do you want to replace it? (yes/no)");
            Scanner scanner = new Scanner(System.in);
            String response = scanner.nextLine();
            if(response.equals("yes")) {
                try {
                    Files.delete(filePath);
                } catch (IOException e) {
                    throw new IOException("An error occurred while deleting the file!");
                }
            }
            else
                throw new FileAlreadyExistsException("Please, provide new directory!");
        }

        return filePathString;
    }

    public static DownloadData parseDownloadCommand(String command)
            throws DownloadException, IOException {
        String[] words = command.split("\\s+");

        if (words.length > 4 || words.length < 3) {
            throw new DownloadException("Incorrect format of 'download' command!");
        }

        AddressPair addressPair = parseAddress(words[1]);

        if(!Files.exists(Paths.get(words[2]))) {
            throw new FileNotFoundException("The file is currently unavailable!");
        }

        final String DEFAULT_DOWNLOADS_DIRECTORY = "downloads";
        Files.createDirectories(Paths.get(DEFAULT_DOWNLOADS_DIRECTORY));

        String downloadPathDirectory = words.length == 4 ? words[3] : DEFAULT_DOWNLOADS_DIRECTORY;
        String downloadFilePath = getDownloadDirectoryPath(Paths.get(words[2]).getFileName().toString(),
                downloadPathDirectory);

        return new DownloadData(addressPair.ip(), addressPair.port(), words[2], downloadFilePath);
    }
}
