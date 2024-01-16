package server.exceptions;

public class InvalidUserException extends Exception{
    public InvalidUserException(String message) {
        super(message);
    }
}
