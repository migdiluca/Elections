package Elections.Exceptions;


import Elections.Models.ElectionState;

public class ElectionStateException extends Exception {


    public ElectionStateException() {
        super();
    }

    public ElectionStateException(String msg) {
        super(msg);
    }
}
