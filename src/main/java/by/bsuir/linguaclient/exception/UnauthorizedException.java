package by.bsuir.linguaclient.exception;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Token has expired or has wrong value");
    }
}
