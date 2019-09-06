package Elections;


import java.util.Map;

public interface ConsultingService {

    /**
     * If elections did not start @throws ElectionsNotStartedException (o un nombre mas piolin)
     * If elections are running, answers must be FPTP else respect each voting.
     */
    Map</*Party*/Integer, Integer> checkResult(/*Spectrum*/Integer spectrum);


}
