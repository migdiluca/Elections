package Elections.server.ServiceImpl;


import Elections.Models.ElectionState;

public class ElectionPOJO {

    private ElectionState electionState;

    public ElectionPOJO() {
        electionState = ElectionState.NOT_STARTED;
    }

    public ElectionState getElectionState() {
        return electionState;
    }

    public void setElectionState(ElectionState electionState) {
        this.electionState = electionState;
    }
}
