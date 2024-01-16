package client.download;

public class DownloadException extends Exception {
    public DownloadException(Exception e) {
        super(e);
    }

    public DownloadException(String s, Exception e) {
        super(s, e);
    }

    public DownloadException(String s) {
        super(s);
    }
}
