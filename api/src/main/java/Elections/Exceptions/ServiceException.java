package Elections.Exceptions;

public class ServiceException extends Exception {

    public ServiceException() {
        super("Interal error on service");
    }
}
