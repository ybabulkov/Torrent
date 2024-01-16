package client.address;

public class AddressException extends Exception {
    public AddressException(String s, Exception e) {
        super(s, e);
    }

    public AddressException(String s) {
        super(s);
    }
}
