package client.download;

import client.address.AddressException;
import client.address.AddressHandler;


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

    public static DownloadData parseDownloadCommand(AddressHandler addressHandler, String command)
            throws DownloadException {
        String[] words = command.split(" ");

        if (words.length != 4) {
            throw new DownloadException("Unknown command!");
        }

        String address;
        try {
            address = addressHandler.addressOf(words[1]);
        } catch (AddressException e) {
            throw new DownloadException(e.getMessage(), e);
        }

        AddressPair pair = parseAddress(address);

        return new DownloadData(pair.ip(), pair.port(), words[2], words[3]);
    }
}
