package client.download;

public class DownloadException extends Exception {

    public DownloadException(String s, Exception e) {
        super(s, e);
    }

    public DownloadException(String s) {
        super(s);
    }
}
