package Elections.Exceptions;

public class ElectionsNotStartedException extends ElectionStateException {

    public ElectionsNotStartedException() {
        super("Elections are yet to start");
    }
}
