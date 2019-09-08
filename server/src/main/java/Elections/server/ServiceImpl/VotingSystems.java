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

    private void doTransferVoteAV(Map<PoliticalParty, Long> masterMap, List<PoliticalParty> eliminatedParties, PoliticalParty roundLoser, Vote vote) {
        List<PoliticalParty> parties = vote.getPreferredParties();
        boolean flag = false;
        int i = 0;
        for (; i < parties.size(); i++) {
            if (eliminatedParties.contains(parties.get(i))) {
                // opcion1: voto ya transferido => falso shift de la lista
                // opcion2: voto a transferir, pero el candidato que le sigue ya fue eliminado => falso shift de la lista
            } else if (roundLoser.equals(parties.get(i))) {
                // voto al candidato eliminado => voto a transferir
                flag = true;
                // falso shift de la lista
            } else {
                // hay un candidato que sigue compitiendo => vemos si el voto ya fue contabilizado o se le transfiere uno
                break;
            }
        }
        if (i == parties.size()) {
            // todos los votos del votante ya fueron transferidos
            // no hay a candidato en competencia a transferir voto
        } else if (flag) {
            // tenemos un candidato a quien transferirle votos
            PoliticalParty party = parties.get(i);
            masterMap.put(party, masterMap.get(party) + 1);
        }
    }

    private void transferVotesAV(Map<PoliticalParty, Long> masterMap, List<PoliticalParty> eliminatedParties, PoliticalParty roundLoser) {
        votes.forEach(vote -> doTransferVoteAV(masterMap, eliminatedParties, roundLoser, vote));
    }

    private void alternativeVoteNationalLevelREC(Map<PoliticalParty, Long> masterMap, List<PoliticalParty> eliminatedParties) {
        // ordenamos el mapa
        // podria haber usado los metodos max/min de streams pero seria mas conveniente ordenarlo de una y no 2 veces
        List<Map.Entry<PoliticalParty, Long>> sortedEntries = masterMap.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .collect(Collectors.toList());
        if (sortedEntries.get(0).getValue() / Double.valueOf(votes.size()) > 0.5) {
            // hay un ganador
            return;
        }
        Map.Entry<PoliticalParty, Long> loser = sortedEntries.get(sortedEntries.size() - 1);
        /* todo: el perdedor podria haber empatado con otro candidato -> alternativas:
        1- random sacar a uno
        2- sacar a los 2
        3- algun tipo de decision sobre estadistica en las rondas anteriores
        */
        masterMap.remove(loser.getKey());
        transferVotesAV(masterMap, eliminatedParties, loser.getKey());
        eliminatedParties.add(loser.getKey());
        alternativeVoteNationalLevelREC(masterMap, eliminatedParties);
    }

    public List<Pair<BigDecimal, PoliticalParty>> alternativeVoteNationalLevel() {
        // todo: en vez de mapa de <PoliticalParty, Long> podria tener un <PoliticalParty, List<Vote>> de esta forma ya tengo una lista con los votos a transferir y no hay que recontar los votos
        Map<PoliticalParty, Long> masterMap = votes.stream()
                .collect(Collectors.groupingBy(
                        vote -> vote.getPreferredParties().get(0),
                        Collectors.counting())
                );
        alternativeVoteNationalLevelREC(masterMap, new ArrayList<>());
        return masterMap.entrySet().stream()
                .map(entry -> new Pair<>(new BigDecimal(entry.getValue() * 100 / Double.valueOf(votes.size())), entry.getKey()))
                .collect(Collectors.toList());
    }
}
