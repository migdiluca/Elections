package Elections.server.ServiceImpl;

import Elections.Models.PoliticalParty;
import Elections.Models.Vote;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                    new BigDecimal(sortedEntries.get(0).getValue().size() / (double) total).setScale(2, BigDecimal.ROUND_DOWN),
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

    public Map<Integer, List<Pair<BigDecimal, PoliticalParty>>> calculateDeskResults() {
        Map<Integer, List<Pair<BigDecimal, PoliticalParty>>> map = new HashMap<>();
        Map<Integer, List<Vote>> votesPerDesk = votes.stream()
                .collect(Collectors.groupingBy(Vote::getTable));
        votesPerDesk.forEach((k, v) -> {
            Map<PoliticalParty, List<Vote>> collect = v.stream().collect(Collectors.groupingBy((u) -> u.getPreferredParties().get(0)));
            List<Pair<BigDecimal, PoliticalParty>> list = new ArrayList<>();
            collect.forEach((x, y) -> {
                list.add(new Pair<>(new BigDecimal(y.size() / (double) v.size()).setScale(2, BigDecimal.ROUND_DOWN), x));
            });
            list.sort((a, b) -> a.getKey().subtract(b.getKey()).intValue());
            map.put(k, list);
        });
        return map;
    }
}
