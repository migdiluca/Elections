package Elections.server.ServiceImpl;


import Elections.Models.PoliticalParty;

public class PartyResults {

    private PoliticalParty politicalParty;
    private int results;

    public PartyResults(PoliticalParty politicalParty, int results) {
        this.politicalParty = politicalParty;
        this.results = results;
    }

    public PartyResults(PoliticalParty politicalParty) {
        this.politicalParty = politicalParty;
        this.results = 0;
    }

    public PoliticalParty getPoliticalParty() {
        return politicalParty;
    }

    public void setPoliticalParty(PoliticalParty politicalParty) {
        this.politicalParty = politicalParty;
    }

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
