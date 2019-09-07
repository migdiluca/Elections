package Elections.Exceptions;

public class ElectionsNotStartedException extends ElectionStateException {

    public ElectionsNotStartedException() {
        super("Elections have not yet started");
    }
}
