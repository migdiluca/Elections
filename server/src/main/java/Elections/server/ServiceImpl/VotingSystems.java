package Elections.server.ServiceImpl;

import Elections.Models.PoliticalParty;
import Elections.Models.Vote;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VotingSystems {

    List<Vote> votes;

    public VotingSystems(List<Vote> votes) {
        this.votes = votes;
    }

    private void doTransferVoteAV(Map<PoliticalParty, List<Vote>> masterMap, List<PoliticalParty> eliminatedParties, Vote vote) {
        List<PoliticalParty> parties = vote.getPreferredParties();
        int i = 0;
        for (; i < parties.size(); i++) {
            if (eliminatedParties.contains(parties.get(i))) {
                // opcion1: voto ya transferido => falso shift de la lista
                // opcion2: voto a transferir, pero el candidato que le sigue ya fue eliminado => falso shift de la lista
                // opcion3: voto al candidato eliminado => voto a transferir
                // falso shift de la lista
            } else {
                // hay un candidato que sigue compitiendo => se le transfiere un voto
                break;
            }
        }
        if (i != parties.size()) {
            // tenemos un candidato a quien transferirle votos
            PoliticalParty party = parties.get(i);
            masterMap.get(party).add(vote);
        }
    }

    private void transferVotesAV(Map<PoliticalParty, List<Vote>> masterMap, List<PoliticalParty> eliminatedParties, List<Vote> transferableVotes) {
        transferableVotes.forEach(vote -> doTransferVoteAV(masterMap, eliminatedParties, vote));
    }

    private void alternativeVoteNationalLevelREC(Map<PoliticalParty, List<Vote>> masterMap, List<PoliticalParty> eliminatedParties) {
        // ordenamos el mapa
        // podria haber usado los metodos max/min de streams pero seria mas conveniente ordenarlo de una y no 2 veces
        List<Map.Entry<PoliticalParty, List<Vote>>> sortedEntries = masterMap.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .collect(Collectors.toList());
        if ((sortedEntries.get(0).getValue().size() / (double) votes.size()) > 0.5 || sortedEntries.size() == 2) {
            // hay un ganador, quitamos al segundo del mapa
            masterMap.remove(sortedEntries.get(1).getKey());
            return;
        }
        Map.Entry<PoliticalParty, List<Vote>> loser = sortedEntries.get(sortedEntries.size() - 1);
        /* todo: el perdedor podria haber empatado con otro candidato -> alternativas:
        1- random sacar a uno
        2- sacar a los 2
        3- algun tipo de decision sobre estadistica en las rondas anteriores
        */
        masterMap.remove(loser.getKey());
        eliminatedParties.add(loser.getKey());
        transferVotesAV(masterMap, eliminatedParties, loser.getValue());
        alternativeVoteNationalLevelREC(masterMap, eliminatedParties);
    }

    public List<Pair<BigDecimal, PoliticalParty>> alternativeVoteNationalLevel() {
        Map<PoliticalParty, List<Vote>> masterMap = votes.stream()
                .collect(Collectors.groupingBy(vote -> vote.getPreferredParties().get(0)));
        alternativeVoteNationalLevelREC(masterMap, new ArrayList<>());
        return masterMap.entrySet().stream()
                .map(entry -> new Pair<>(new BigDecimal(entry.getValue().size() * 100 / (double) votes.size()), entry.getKey()))
                .collect(Collectors.toList());
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
}
