package Elections.server.ServiceImpl;

import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import Elections.Models.Vote;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
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
    // We asume that there will never be an election where #candidates <= WINNERS_PER_PROVINCE

    public List<Pair<PoliticalParty, Double>> stVoteProvicialLevel(Province prov) {
        Supplier<Stream<Vote>> supplier = () -> votes.stream().filter(x -> x.getProvince() == prov);
        long provinceCount = supplier.get().count();
        if (provinceCount <= 0) { System.out.println("There are no votes for this province: " + prov.toString()); return null;}
        // We need a cleaner structures
        // In this set we keep all candidates who are still particiapting
        Set<PoliticalParty> stillCompeting = new HashSet<>(Arrays.asList(PoliticalParty.values()));
        // In this map we have votes grouped by their first choice of candidates
        Map<PoliticalParty, List<WVote>> masterMap = supplier.get()
                .map(WVote::new)
                .collect(Collectors.groupingBy((WVote wv) -> wv.getPreferredParties().get(0)));
        // We need a sorted set based on amount of votes of a given candidate,
        // so we create a comparator to order and distinguish the lists
        // We compare first by amount of votes, then by party name(because they need to be distinguishable)
        Comparator<Pair<PoliticalParty, VoteList>> comp = Comparator
                .comparing((Function<Pair<PoliticalParty, VoteList>, VoteList>) Pair::getValue, Comparator.comparingDouble(VoteList::getVotes))
                .thenComparing(Pair::getKey);

        SortedSet<Pair<PoliticalParty, VoteList>> set = new TreeSet<>(comp);
        // we fill the set with VoteList for all candidate
        // why? it may occur that a candidate have 0 votes on initial conditions but
        // thorough out the polling he earns some votes.
        for (PoliticalParty pp : PoliticalParty.values()) {
            if (masterMap.containsKey(pp)) // if there is an existing list of votes, we use it
                set.add(new Pair<>(pp, new VoteList(masterMap.get(pp))));
            else // if he has no votes we need to create a list
                set.add(new Pair<>(pp, new VoteList()));
        }
        // In this buffer we place the winners with the TOTAL amount of votes
        List<Pair<PoliticalParty, VoteList>> winnersSet = new ArrayList<>();
        // threshold is the TOTAL amount of votes a candidate need to sit at the table
        double threshold = provinceCount/(double)WINNERS_PER_PROVINCE;
        stVoteProvicialLevelREC(set, stillCompeting, WINNERS_PER_PROVINCE, threshold, winnersSet);
        // We return the winners with with the FRACTION of votes they won over the total amount of votes
        return winnersSet.stream()
                .map((Pair<PoliticalParty, VoteList> p) -> new Pair<>(p.getKey(), p.getValue().votes/provinceCount))
                .collect(Collectors.toList());
    }


    private void stVoteProvicialLevelREC(Set<Pair<PoliticalParty, VoteList>> masterSet, Set<PoliticalParty> stillCompeting, int availableSeats, double threshold, List<Pair<PoliticalParty, VoteList>> winnersSet) {
        // stillCompeting list should always contain candidates that did not won nor where eliminated
        availableSeats -= checkForWinners(masterSet, threshold, stillCompeting, winnersSet);
        // We ckeck that there still are candidates competing
        Supplier<Stream<Pair<PoliticalParty, VoteList>>> supplier = () -> masterSet.stream()
                .filter(c -> stillCompeting.contains(c.getKey()) && c.getValue().size() > 0); // c.getValue().size() > 0 makes the difference
        long stillCompetingCount  = supplier.get().count();
        if (stillCompetingCount < availableSeats) {
            // There are less candidates than seats.
            System.out.println("There is no way to determine a winner between the lasts, they all are in the exact same situation");
        }
        if (stillCompetingCount <= availableSeats) {
            // There are less or equal candidates compared to seats.
            // We force this lasts candidates to be winners even thought they do not meet the threshold
            winnersSet.addAll(supplier.get().collect(Collectors.toList()));
            return;
        }
        if (availableSeats < 0) {
            System.out.println("There were more winners than seats!");
            return;
        } else if (availableSeats == 0) {
            // we have finished!
            return;
        } else {
            // We did not find enough winners, we have to eliminate loosers and distribute their votes
            eliminateLooser(masterSet, stillCompeting, threshold);
        }
        stVoteProvicialLevelREC(masterSet, stillCompeting, availableSeats, threshold, winnersSet);
    }

    private void eliminateLooser(Set<Pair<PoliticalParty, VoteList>> masterSet, Set<PoliticalParty> stillCompeting, double threshold) {
        Map<PoliticalParty, VoteList> buffer = new HashMap<>(); // buffer to transfer left overs at the end of this method
        for (PoliticalParty pp :PoliticalParty.values()) { buffer.put(pp, new VoteList()); } // init buffer
        // we sort the list to attack the lowest candidates first
        List<Pair<PoliticalParty, VoteList>> sorted = masterSet.stream()
                .sorted(Comparator.comparingDouble(o -> o.getValue().votes))
                .filter(c -> stillCompeting.contains(c.getKey()) && c.getValue().size() > 0 && c.getValue().votes < threshold) // we need only those who have votes
                .collect(Collectors.toList());
        double smallestVotes = sorted.get(0).getValue().votes;
        double newSmallestVotes;
        for (Pair<PoliticalParty, VoteList> candidate : sorted) {
            newSmallestVotes = candidate.getValue().votes;
            if (smallestVotes != newSmallestVotes ) {
                // if this happens, it means that we have already eliminated all the loosers
                // we distribute left overs and leave
                redistributeVotes(buffer, masterSet);
                return;
            }
            // we elominate the looser
            stillCompeting.remove(candidate.getKey());
            // we will give this votes a weight of 1
            bufferLeftoverVotes(candidate, buffer, 1.0, stillCompeting);
            smallestVotes = newSmallestVotes;
        }
        // we do not redistributeVotes(buffer, masterSet) because if we reach here
        // it means that smallestVotes was always == newSmallestVotes
        // which means that parties had the same amount of votes
    }

    private int checkForWinners(Set<Pair<PoliticalParty, VoteList>> masterSet, double threshold, Set<PoliticalParty> stillCompeting, List<Pair<PoliticalParty, VoteList>> winnersSet) {
        int winners = 0; // counting the amount of winners
        // buffer to transfer left overs at the end of this method
        Map<PoliticalParty, VoteList> buffer = new HashMap<>();
        // init buffer
        for (PoliticalParty pp :PoliticalParty.values()) { buffer.put(pp, new VoteList()); }

        boolean distribute = false; // flag to distribute or not
        for (Pair<PoliticalParty, VoteList> candidate : masterSet) {
            if (stillCompeting.contains(candidate.getKey())) {
                if (candidate.getValue().votes >= threshold) {
                    // he won!
                    winners++;
                    winnersSet.add(candidate);
                    stillCompeting.remove(candidate.getKey());
                }
                if (candidate.getValue().votes > threshold) {
                    // above winning, his left over votes are buffered to be then
                    // transfered to other candidates
                    double extraVoteWeight = (candidate.getValue().votes - threshold) /  candidate.getValue().votes;
                    bufferLeftoverVotes(candidate, buffer, extraVoteWeight, stillCompeting);
                    distribute = true;
                }
            }
        }
        // redistribute votes
        if (distribute) {
            redistributeVotes(buffer, masterSet);
        }
        return winners;
    }

    private void redistributeVotes(Map<PoliticalParty, VoteList> buffer, Set<Pair<PoliticalParty, VoteList>> masterSet) {
        // transfer votes, we asume that all candidates in the buffer are competing
        for (Pair<PoliticalParty, VoteList> candidate : masterSet) {
            candidate.getValue().addAll(buffer.get(candidate.getKey()));
        }
    }

    private void bufferLeftoverVotes(Pair<PoliticalParty, VoteList> candidate, Map<PoliticalParty, VoteList> buffer, double extraVoteWeight, Set<PoliticalParty> stillCompeting) {
        // For each vote paper of this candidate, we distribute the other choice, if the apply
        for (WVote vote : candidate.getValue()) {
            // We look for the second or third option
            List<PoliticalParty> preferredParties = vote.getPreferredParties();
            // second vote is present and it counts
            if (preferredParties.size() > 1 && stillCompeting.contains(preferredParties.get(1))) {
                // his vote counts
                // the new vote will have less options than before and a weight
                List<PoliticalParty> newParties = preferredParties.subList(1, preferredParties.size());
                // add the vote with its weight to the list
                WVote newVote = new WVote(vote.getTable(), newParties, vote.getProvince(), extraVoteWeight);
                buffer.get(preferredParties.get(1)).add(newVote);
            } else if (preferredParties.size() == 3 && stillCompeting.contains(preferredParties.get(2)) ) {
                // third is present and it counts
                // his vote counts
                // the new vote will have less options than before and a weight
                List<PoliticalParty> newParties = preferredParties.subList(2, preferredParties.size());
                // add the vote with its weight to the list
                buffer.get(preferredParties.get(2)).add(new WVote(vote.getTable(), newParties, vote.getProvince(), extraVoteWeight));
            }
        }
    }
/*
    private Set<Pair<BigDecimal, PoliticalParty>> stVoteProvicialLevelREC(Map<PoliticalParty, List<Vote>> masterMap, List<PoliticalParty> notCountingParties, long totalVotes, int availableSeats, List<Pair<BigDecimal, PoliticalParty>> stillCompeting) {
        // caso base, no hay que iterar más
        if (availableSeats == 0) {
            return new HashSet<>();
        }
        // array para los ganadores de esta iteración;
        Set<Pair<BigDecimal, PoliticalParty>> winnerPPs = new HashSet<>();

        // Calculamos el mínimo de votos requerido para ser elegido,
        // esto es igual al máximo de votos que se pueden adquirir por candidato.
        // El fracción de votos que sobrepasan este threshold es redistribuido entre las segundas y terceras opciones
        BigDecimal threshold = new BigDecimal(totalVotes / (double)availableSeats); // redondeo??
        BigDecimal thresholdFraction = new BigDecimal(1 / (double)availableSeats); // redondeo??

        // Mapa para hacer un buffer de los votos de seunga/tercera opción a ditribuir.
        // esperamos para distribuir cosa de que recién al final de la iteración se consideren los cambios
        Map<PoliticalParty, BigDecimal> secondChoiceBuffer = new HashMap<>();
        for (PoliticalParty pp : PoliticalParty.values()) { secondChoiceBuffer.put(pp, new BigDecimal(0)); }

        // Para cada candidato, si sobre pasa el límite, contamos las segundas/terceras opciones de sus votos
        // para luego distribuirlos
        int newAvailableSeats = thresholdCounting(masterMap, threshold, totalVotes, winnerPPs, notCountingParties, availableSeats, secondChoiceBuffer);
        if (newAvailableSeats == 0) {
            // terminamos
            printWinners(winnerPPs);
            return winnerPPs;
        } else if (newAvailableSeats < 0){
            // no se si es posible este caso
            throw new IllegalStateException("Ganaron más personas que sillas habilitadas");
        }

        // En el caso que ningún candidato haya llegado al threshold, eliminamos a alguno que esté
        // último en la lista y guardamos sus votos para luego distribuirlos.
        if (newAvailableSeats == availableSeats) {
            eliminateLoosers(masterMap, notCountingParties, threshold);
        }
        availableSeats = newAvailableSeats;

        // TODO redistribuir los votos del buffer y generar nuevo masterMap
        masterMap = redistributeExtraVotes();
        // retornamos la lista de winners concatenada con los winners de otras iteraciones
        winnerPPs.addAll(stVoteProvicialLevelREC(masterMap, notCountingParties, totalVotes, availableSeats));
        return winnerPPs;
    }

    private void eliminateLoosers(Map<PoliticalParty, List<Vote>> masterMap, List<PoliticalParty> notCountingParties, long threshold) {
        // buffer para meter votos y después respartirlos al final
        Map<PoliticalParty, List<Vote>> buffer = new HashMap<>();
        for (PoliticalParty pp : PoliticalParty.values()) { buffer.put(pp, new ArrayList<>()); }

        // iteramos por cada candidato en orden descendiente
        List<Map.Entry<PoliticalParty, List<Vote>>> sortedEntries = new ArrayList<>(masterMap.entrySet());
        sortedEntries.sort(Comparator.comparingInt(o -> o.getValue().size()));
        long votesCount;
        long oldVotesCount = sortedEntries.get(0).getValue().size();
        for (Map.Entry<PoliticalParty, List<Vote>> e: sortedEntries) {
            // si ya ganó o ya perdió, no lo contamos
            if (!notCountingParties.contains(e.getKey())) {
                break;
            }
            // solo sacamos el/los más perdedores, es decir sacamos de a 1 grupo de perdedores en base a la cantida de votos
            votesCount = e.getValue().size();
            if (votesCount != oldVotesCount) {
                return;
            }
            if (votesCount < threshold) {
                // lo eliminamos
                notCountingParties.add(e.getKey());
                // metemos sus votos en un buffer, al final de la función los repartimos
                bufferExtraVotes(buffer, masterMap.get(e.getKey()), notCountingParties);
            } else {
                throw new IllegalStateException("Para este punto no debería suceder que algún candiato sobrepasa el threshold");
            }
            oldVotesCount = votesCount;
        }
    }

    private void bufferExtraVotes(Map<PoliticalParty, List<Vote>> buffer, List<Vote> votes, List<PoliticalParty> notCountingParties) {

    }

    private void printWinners(Set<Pair<BigDecimal, PoliticalParty>> winnerPPs) {
        winnerPPs.stream()
                .sorted(Comparator.comparing(Pair::getKey))
                .forEach(x -> {
                    // ej : 20,00%;GORILLA
                    // TODO format output
                    System.out.println(x.getKey() + "%;" + x.getValue().toString());
                });
    }

    private int thresholdCounting(Map<PoliticalParty, List<Vote>> masterMap, BigDecimal threshhold, long totalVotes, Set<Pair<BigDecimal, PoliticalParty>> winnerPPs, List<PoliticalParty> notCountingParties, int availableSeats, Map<PoliticalParty, BigDecimal> secondChoiceBuffer) {
        // Por cada PoliticalParty nos fijamos si alguno sobre pasa el límite
        for (PoliticalParty pp: masterMap.keySet()) {
            BigDecimal fractionVotesPP = new BigDecimal(masterMap.get(pp).size() / (double) totalVotes);
            if (fractionVotesPP.compareTo(threshhold) >= 0) {
                // si es igual
                winnerPPs.add(new Pair<>(fractionVotesPP, pp)); // agregamos a la lista de los que ya ganaron
                notCountingParties.add(pp); // agregamos a la lista de los que no van a contar para la siguiente iteración
                availableSeats--;           // hay 1 asiento menos
            }
            if (fractionVotesPP.compareTo(threshhold) > 0) {
                // si tiene más votos que el threshold agregamos los votos extras al buffer
                bufferExtraVotes(masterMap.get(pp), notCountingParties, secondChoiceBuffer, fractionVotesPP.subtract(threshhold).multiply(fractionVotesPP));
            }
            // else: fractionVotesPP < threshhold => después decidimos qué hacer
        }
        return availableSeats;
    }

    private void bufferExtraVotes(List<Vote> ppVotes, List<PoliticalParty> notCountingParties, Map<PoliticalParty, BigDecimal> secondChoiceBuffer, BigDecimal extraVotesWeight) {
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
                // agregamos la franción de la fración de votos
                BigDecimal newFraction = secondChoiceBuffer.get(preferredParties.get(1)).add(extraVotesWeight);
                secondChoiceBuffer.put(preferredParties.get(1), newFraction);
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
            BigDecimal newFraction = secondChoiceBuffer.get(preferredParties.get(2)).add(extraVotesWeight);
            secondChoiceBuffer.put(preferredParties.get(2), newFraction);
        }
    }*/

    public static void main(String[] args) {
        List<Vote> votes = new ArrayList<>();
        votes.add(new Vote(1, Arrays.asList(PoliticalParty.LYNX                                 ), Province.JUNGLE));
        votes.add(new Vote(1, Arrays.asList(PoliticalParty.BUFFALO, PoliticalParty.LYNX         ), Province.JUNGLE));
        votes.add(new Vote(1, Arrays.asList(PoliticalParty.BUFFALO, PoliticalParty.LYNX         ), Province.JUNGLE));
        votes.add(new Vote(1, Arrays.asList(PoliticalParty.BUFFALO, PoliticalParty.LYNX         ), Province.JUNGLE));
        votes.add(new Vote(1, Arrays.asList(PoliticalParty.GORILLA                              ), Province.JUNGLE));
        votes.add(new Vote(1, Arrays.asList(PoliticalParty.GORILLA, PoliticalParty.WHITE_GORILLA), Province.JUNGLE));
        votes.add(new Vote(1, Arrays.asList(PoliticalParty.GORILLA, PoliticalParty.JACKALOPE    ), Province.JUNGLE));
        votes.add(new Vote(1, Arrays.asList(PoliticalParty.WHITE_GORILLA                        ), Province.JUNGLE));
        votes.add(new Vote(1, Arrays.asList(PoliticalParty.JACKALOPE                            ), Province.JUNGLE));

        votes.add(new Vote(1, Arrays.asList(PoliticalParty.LEOPARD, PoliticalParty.WHITE_GORILLA), Province.JUNGLE));

        Collections.shuffle(votes);
        VotingSystems vs = new VotingSystems(votes);
        List<Pair<PoliticalParty, Double>> resp = vs.stVoteProvicialLevel(Province.JUNGLE);
        resp.forEach( c -> System.out.println(c.getKey() + ";" + c.getValue()*100));
    }

    private class WVote extends Vote {
        double weight;

        WVote(Vote v) {
            this(v, 1);
        }

        WVote(Vote v, double w) {
            super(v);
            this.weight = w;
        }

        WVote(int table, List<PoliticalParty> parties, Province province, double w) {
            super(table, parties, province);
            this.weight = w;
        }

        @Override
        public String toString() {
            return "WVote{" +
                    "weight=" + weight +
                    "} " + super.toString();
        }
    }

    private class VoteList extends ArrayList<WVote> {
        double votes;

        VoteList(List<WVote> wVotes) {
            super(wVotes);
            votes = wVotes.stream()
                    .map(wv -> wv.weight)
                    .reduce(0.0, Double::sum);
        }

        VoteList() {
            super();
        }

        @Override
        public boolean add(VotingSystems.WVote wv) {
            votes += wv.weight;
            return super.add(wv);
        }

        @Override
        public WVote remove(int index) {
            this.votes -= super.get(index).weight;
            return super.remove(index);
        }

        @Override
        public boolean addAll(Collection<? extends WVote> list) {
            votes += list.stream()
                    .map(wv -> wv.weight)
                    .reduce(0.0, Double::sum);
            return super.addAll(list);
        }

        public double getVotes() {
            return votes;
        }

        @Override
        public String toString() {
            return "VoteList{" +
                    "votes=" + votes +
                    '}';
        }
    }
}
