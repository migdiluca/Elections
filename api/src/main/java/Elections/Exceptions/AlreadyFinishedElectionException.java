package Elections.Exceptions;

public class AlreadyFinishedElectionException extends ElectionStateException {

    public AlreadyFinishedElectionException() {
        super("Elections have already finished");
    }
}
