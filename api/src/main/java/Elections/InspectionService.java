package Elections;


public interface InspectionService {
    /**
     * If already open
     * e@throws ElectionsAlreadyStartedException or
     * e@throws AlreadyFinishedElectionException (o un nombre mas piolin)
     *
     * hay que ver como implementar el servicio remoto pavisar que hubo un voto
     */
    void addInspector(/*Party*/int party, int desk);


}
