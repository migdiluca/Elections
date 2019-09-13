package Elections.server.ServiceImpl;

import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import Elections.Models.Vote;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VotingSystems {

    private List<Vote> votes;

    public VotingSystems(List<Vote> votes) {
        this.votes = votes;
    }

    /*
        Retorna true/false según si el voto fue transferido o no
    */
    private boolean doTransferVoteAV(Map<PoliticalParty, List<Vote>> masterMap, List<PoliticalParty> eliminatedParties, Vote vote) {
        List<PoliticalParty> parties = vote.getPreferredParties();
        int i = 0;
        while (i < parties.size() && eliminatedParties.contains(parties.get(i))) {
            // opcion: voto ya transferido => falso shift de la lista
            // opcion: voto a transferir, pero el candidato que le sigue ya fue eliminado => falso shift de la lista
            // opcion: voto al candidato eliminado => voto a transferir
            // falso shift de la lista
            i++;
        }
        if (i != parties.size()) {
            // hay un candidato que sigue compitiendo => se le transfiere un voto
            PoliticalParty party = parties.get(i);
            masterMap.get(party).add(vote);
            return true;
        }
        return false;
    }

    /*
        Retorna la cantidad de votos que fueron transferidos
     */
    private int transferVotesAV(Map<PoliticalParty, List<Vote>> masterMap, List<PoliticalParty> eliminatedParties, List<Vote> transferableVotes) {
//        transferableVotes.forEach(vote -> doTransferVoteAV(masterMap, eliminatedParties, vote));
        // no uso java 8 ya que no deja incluir variables no final en sus bloques de codigo {}
        int count = 0;
        for (Vote vote : transferableVotes) {
            if (doTransferVoteAV(masterMap, eliminatedParties, vote)) {
                count++;
            }
        }
        return count;
    }

    /*
        Retorna Pair (porcentaje de votos, partido politico) del ganador de la eleccion
     */
    private Pair<BigDecimal, PoliticalParty> alternativeVoteNationalLevelREC(Map<PoliticalParty, List<Vote>> masterMap, List<PoliticalParty> eliminatedParties, int total) {
        // ordenamos el mapa
        // podria haber usado los metodos max/min de streams pero seria mas conveniente ordenarlo de una y no 2 veces
        List<Map.Entry<PoliticalParty, List<Vote>>> sortedEntries = masterMap.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .collect(Collectors.toList());
        if ((sortedEntries.get(0).getValue().size() / (double) total) > 0.5) {
            // hay un ganador
            return new Pair<>(
                    new BigDecimal(sortedEntries.get(0).getValue().size() / (double) total),
                    sortedEntries.get(0).getKey()
            );
        }
        Map.Entry<PoliticalParty, List<Vote>> loser = sortedEntries.get(sortedEntries.size() - 1);
        /* todo: el perdedor podria haber empatado con otro candidato -> alternativas:
        1- random sacar a uno
        2- sacar a los 2
        3- algun tipo de decision sobre estadistica en las rondas anteriores
        */
        masterMap.remove(loser.getKey());
        eliminatedParties.add(loser.getKey());
        int trasnferredVotes = transferVotesAV(masterMap, eliminatedParties, loser.getValue());
        int votesLost = loser.getValue().size() - trasnferredVotes;
        return alternativeVoteNationalLevelREC(masterMap, eliminatedParties, total - votesLost);
    }

    /*
       Retorna Pair(porcentaje de votos, partido politico) del ganador de la eleccion
    */
    public Pair<BigDecimal, PoliticalParty> alternativeVoteNationalLevel() {
        Map<PoliticalParty, List<Vote>> masterMap = votes.stream()
                .collect(Collectors.groupingBy(vote -> vote.getPreferredParties().get(0)));
        return alternativeVoteNationalLevelREC(masterMap, new ArrayList<>(), votes.size());
    }

    public void calculateDeskResults(Map<Integer, List<Pair<BigDecimal, PoliticalParty>>> entry) {
        Map<Integer, List<Vote>> votesPerDesk =
                votes.stream().collect(Collectors.groupingBy(Vote::getTable));

        votesPerDesk.forEach((k, v) -> {
            Map<PoliticalParty, List<Vote>> collect = v.stream().collect(Collectors.groupingBy((u) -> u.getPreferredParties().get(0)));
            List<Pair<BigDecimal, PoliticalParty>> list = new ArrayList<>();
            collect.forEach((x, y) -> {
                list.add(new Pair<>(new BigDecimal(y.size() / v.size()), x));
            });
            list.sort((a, b) -> a.getKey().subtract(b.getKey()).intValue());
            entry.put(k, list);
        });
    }

    private final int WINNERS_PER_PROVINCE = 5;

    public List<Pair<BigDecimal, PoliticalParty>> stVoteProvicialLevel(Province prov) {
         Stream<Vote> provinceStream = votes.stream()
                .filter(x -> x.getProvince() == prov);
        long provinceCount = provinceStream.count();
        Map<PoliticalParty, List<Vote>> masterMap = provinceStream.collect(Collectors.groupingBy(vote -> vote.getPreferredParties().get(0)));
        return stVoteProvicialLevelREC(masterMap, new ArrayList<>(), provinceCount, WINNERS_PER_PROVINCE);
    }

    /**
     * Al inicio de cada iteración se recalcula el thresh hold y al final se recalculan los asientos disponibles
     * */
    private List<Pair<BigDecimal, PoliticalParty>> stVoteProvicialLevelREC(Map<PoliticalParty, List<Vote>> masterMap, List<PoliticalParty> notCountingParties, long totalVotes, int availableSeats) {
        // caso base, no hay que iterar más
        if (availableSeats == 0) {
            return new ArrayList<>();
        }
        // array para los ganadores de esta iteración;
        List<Pair<BigDecimal, PoliticalParty>> winnerPPs = new ArrayList<>();

        // Calculamos el mínimo de votos requerido para ser elegido,
        // esto es igual al máximo de votos que se pueden adquirir por candidato.
        // El porcentaje de votos que sobrepasan este threshhold es redistribuido entre las segundas y terceras opciones
        double threshhold = totalVotes / availableSeats; // redondeo??

        // Mapa para hacer un buffer de los votos de seunga/tercera opción a ditribuir.
        // esperamos para distribuir cosa de que recién al final de la iteración se consideren los cambios
        Map<PoliticalParty, LongAdder> secondChoiceBuffer = new HashMap<>();
        for (PoliticalParty pp : PoliticalParty.values()) { secondChoiceBuffer.put(pp, new LongAdder()); }

        // Para cada candidato, si sobre pasa el límite, contamos las segundas/terceras opciones de sus votos
        // para luego distribuirlos
        availableSeats = threshholdCounting(masterMap, threshhold, totalVotes, winnerPPs, notCountingParties, availableSeats, secondChoiceBuffer);
        if (availableSeats == 0) {
            // terminamos
            // todo ??
        } else if (availableSeats < 0 ) {
            // no se si es posible este caso
            // todo ??
            
        }

        // retornamos la lista de winners concatenada con los winners de otras iteraciones
        winnerPPs.addAll(stVoteProvicialLevelREC());
        return winnerPPs;
    }

    private int threshholdCounting(Map<PoliticalParty, List<Vote>> masterMap, double threshhold, long totalVotes, List<Pair<BigDecimal, PoliticalParty>> winnerPPs, List<PoliticalParty> notCountingParties, int availableSeats, Map<PoliticalParty, LongAdder> secondChoiceBuffer) {
        // Por cada PoliticalParty nos fijamos si alguno sobre pasa el límite
        for (PoliticalParty pp: masterMap.keySet()) {
            long votesForThisPP = masterMap.get(pp).size();
            if ( votesForThisPP > threshhold) {
                // si sobre pasa
                winnerPPs.add(new Pair<>(new BigDecimal(votesForThisPP / totalVotes), pp)); // agregamos a la lista de los que ya ganaron
                notCountingParties.add(pp); // agregamos a la lista de los que no van a contar para la siguiente iteración
                availableSeats--;           // hay 1 asiento menos
                // agregamos los votos al buffer
                bufferExtraVotes(masterMap.get(pp), notCountingParties, secondChoiceBuffer);
            } else if (votesForThisPP == threshhold) {
                // si es igual
                winnerPPs.add(new Pair<>(new BigDecimal(votesForThisPP / totalVotes), pp)); // agregamos a la lista de los que ya ganaron
                notCountingParties.add(pp); // agregamos a la lista de los que no van a contar para la siguiente iteración
                availableSeats--;           // hay 1 asiento menos
            }
            // else: votesForThisPP < threshhold => después decidimos qué hacer
        }
        return availableSeats;
    }

    /**
     * Itero por todos los votos de este partido.
     * Considero la segunda o tercera opción en base a los partidos que no fueron eliminados y no ganaron
     * Si no hay candidato disponible estos votos no aportan a otros candidatos
     * y tampoco modifican el promedio que será agregado a los otros partidos
     */
    private void bufferExtraVotes(List<Vote> ppVotes, List<PoliticalParty> notCountingParties, Map<PoliticalParty, LongAdder> extrasBuffer) {
        // por cada voto de este candidato
        for (Vote vote: ppVotes) {
            // buscamos la segunda o tercera opción
            List<PoliticalParty> preferredParties = vote.getPreferredParties();
            if (preferredParties.size() < 2) {
                break; // votó uno solo y entonces su voto no es transferible
            }
            // si llegué hasta acá, por lo menos votó a 2 candidatos
            if (!notCountingParties.contains(preferredParties.get(1))) {
                // entonces este voto si cuenta!
                extrasBuffer.get(preferredParties.get(1)).increment();
                break;
            }
            // si llegúe hasta acá, tiene por lo menos 2 votos, pero el segundo no sirve
            // vemos si tiene tercer voto
            if (preferredParties.size() < 3) {
                break; // no tuvo tercer voto y entonces su voto no es transferible
            }
            //si llegúe hasta acá, tiene 3 votos
            if (notCountingParties.contains(preferredParties.get(2))) {
                break; // entonces el tercer voto no sirve
            }
            // si llegúe hasta acá, el tercer voto sirve
            extrasBuffer.get(preferredParties.get(2)).increment();
        }
    }
}
