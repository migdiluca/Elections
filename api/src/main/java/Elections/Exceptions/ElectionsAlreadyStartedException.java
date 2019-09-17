package Elections.Exceptions;

public class ElectionsAlreadyStartedException extends ElectionStateException {

    public ElectionsAlreadyStartedException(){
        super("Elections have already started");
    }

}
