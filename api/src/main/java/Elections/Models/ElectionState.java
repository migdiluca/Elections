package Elections.Models;

public enum ElectionState {

    NOT_STARTED("Election are yet to start"), RUNNING("Election started"), FINISHED("Election ended");

    private String desc;

    ElectionState(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
