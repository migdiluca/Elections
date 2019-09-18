package Elections.server.ServiceImpl;

import Elections.Models.PoliticalParty;
import Elections.Models.Province;
import Elections.Models.Vote;
import Elections.Models.Pair;;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VotingSystems {

    private List<Vote> votes;
    private final static Comparator<Pair<BigDecimal, PoliticalParty>> cmpByPercentage = (a1, a2) -> a2.getKey().compareTo(a1.getKey());
    private final static Comparator<Pair<BigDecimal, PoliticalParty>> cmpByName = Comparator.comparing(p -> p.getValue().name());
    public final static Comparator<Pair<BigDecimal, PoliticalParty>> cmp = cmpByPercentage.thenComparing(cmpByName);

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
            if (masterMap.containsKey(party)) {
                masterMap.get(party).add(vote);
            } else {
                List<Vote> votes = new ArrayList<>();
                votes.add(vote);
                masterMap.put(party, votes);
            }
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
    private List<Pair<BigDecimal, PoliticalParty>> alternativeVoteNationalLevelREC(Map<PoliticalParty, List<Vote>> masterMap, List<PoliticalParty> eliminatedParties, int total) {
        // ordenamos el mapa
        // podria haber usado los metodos max/min de streams pero seria mas conveniente ordenarlo de una y no 2 veces
        List<Map.Entry<PoliticalParty, List<Vote>>> sortedEntries = masterMap.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .collect(Collectors.toList());
        if (sortedEntries.isEmpty() || (sortedEntries.get(0).getValue().size() / (double) total) > 0.5) {
            // hay un ganador
            return sortedEntries.stream().map((e) -> new Pair<>(
                    new BigDecimal((e.getValue().size() / (double) total) * 100).setScale(2, BigDecimal.ROUND_DOWN),
                    e.getKey())).collect(Collectors.toList());
        }
        Map.Entry<PoliticalParty, List<Vote>> loser = sortedEntries.get(sortedEntries.size() - 1);
        List<Map.Entry<PoliticalParty, List<Vote>>> losers = sortedEntries.stream().filter(e -> e.getValue().size() == loser.getValue().size()).collect(Collectors.toList());

        losers.forEach(l -> masterMap.remove(l.getKey()));
        losers.forEach(l -> eliminatedParties.add(l.getKey()));

        int trasnferredVotes = 0;
        for (Map.Entry<PoliticalParty, List<Vote>> e : losers) {
            trasnferredVotes += transferVotesAV(masterMap, eliminatedParties, e.getValue());
        }
        int votesLost = loser.getValue().size() - trasnferredVotes;
        return alternativeVoteNationalLevelREC(masterMap, eliminatedParties, total - votesLost);
    }

    /*
       Retorna Pair(porcentaje de votos, partido politico) del ganador de la eleccion
    */
    List<Pair<BigDecimal, PoliticalParty>> alternativeVoteNationalLevel() {
        Map<PoliticalParty, List<Vote>> masterMap = votes.stream()
                .collect(Collectors.groupingBy(vote -> vote.getPreferredParties().get(0)));
        List<Pair<BigDecimal, PoliticalParty>> result = alternativeVoteNationalLevelREC(masterMap, new ArrayList<>(), votes.size());
        result.sort(cmp);
        return result;
    }

    Map<Integer, List<Pair<BigDecimal, PoliticalParty>>> calculateDeskResults() {
        Map<Integer, List<Pair<BigDecimal, PoliticalParty>>> map = new HashMap<>();
        Map<Integer, List<Vote>> votesPerDesk = votes.stream()
                .collect(Collectors.groupingBy(Vote::getDesk));
        votesPerDesk.forEach((k, v) -> {
            Map<PoliticalParty, List<Vote>> collect = v.stream().collect(Collectors.groupingBy((u) -> u.getPreferredParties().get(0)));
            List<Pair<BigDecimal, PoliticalParty>> list = new ArrayList<>();
            collect.forEach((x, y) -> {
                list.add(new Pair<>(new BigDecimal(100 * y.size() / (double) v.size()).setScale(2, BigDecimal.ROUND_DOWN), x));
            });
            list.sort(cmp);
            map.put(k, list);
        });
        return map;
    }

    private final int WINNERS_PER_PROVINCE = 5;
    // We asume that there will never be an election where #candidates <= WINNERS_PER_PROVINCE

    List<Pair<BigDecimal, PoliticalParty>> stVoteProvicialLevel(Province prov) {
        Supplier<Stream<Vote>> supplier = () -> votes.stream().filter(x -> x.getProvince() == prov);
        long provinceCount = supplier.get().count();
        if (provinceCount <= 0) {
            System.out.println("There are no votes for this province: " + prov.toString());
            return null;
        }
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
        List<Pair<PoliticalParty, VoteList>> winnersList = new ArrayList<>();
        // threshold is the TOTAL amount of votes a candidate need to sit at the table
        double threshold = provinceCount / (double) WINNERS_PER_PROVINCE;
        stVoteProvicialLevelREC(set, stillCompeting, WINNERS_PER_PROVINCE, threshold, winnersList);

        // We return the winners with with the FRACTION of votes they won over the total amount of votes
        return winnersList.stream()
                .sorted(comp.reversed())
                .map((Pair<PoliticalParty, VoteList> p) -> new Pair<>(new BigDecimal((p.getValue().votes / provinceCount) * 100).setScale(2, BigDecimal.ROUND_DOWN), p.getKey()))
                .collect(Collectors.toList());
    }


    private void stVoteProvicialLevelREC(Set<Pair<PoliticalParty, VoteList>> masterSet, Set<PoliticalParty> stillCompeting, int availableSeats, double threshold, List<Pair<PoliticalParty, VoteList>> winnersSet) {
        // stillCompeting list should always contain candidates that did not won nor where eliminated
        availableSeats -= checkForWinners(masterSet, threshold, stillCompeting, winnersSet);
        // We ckeck that there still are candidates competing
        Supplier<Stream<Pair<PoliticalParty, VoteList>>> supplier = () -> masterSet.stream()
                .filter(c -> stillCompeting.contains(c.getKey()) && c.getValue().size() > 0); // c.getValue().size() > 0 makes the difference
        long stillCompetingCount = supplier.get().count();
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
        for (PoliticalParty pp : PoliticalParty.values()) {
            buffer.put(pp, new VoteList());
        } // init buffer
        // we sort the list to attack the lowest candidates first
        List<Pair<PoliticalParty, VoteList>> sorted = masterSet.stream()
                .sorted(Comparator.comparingDouble(o -> o.getValue().votes))
                .filter(c -> stillCompeting.contains(c.getKey()) && c.getValue().size() > 0 && c.getValue().votes < threshold) // we need only those who have votes
                .collect(Collectors.toList());
        double smallestVotes = sorted.get(0).getValue().votes;
        double newSmallestVotes;
        for (Pair<PoliticalParty, VoteList> candidate : sorted) {
            newSmallestVotes = candidate.getValue().votes;
            if (smallestVotes != newSmallestVotes) {
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
        for (PoliticalParty pp : PoliticalParty.values()) {
            buffer.put(pp, new VoteList());
        }

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
                    double extraVoteWeight = (candidate.getValue().votes - threshold) / candidate.getValue().votes;
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
                WVote newVote = new WVote(vote.getDesk(), newParties, vote.getProvince(), extraVoteWeight);
                buffer.get(preferredParties.get(1)).add(newVote);
            } else if (preferredParties.size() == 3 && stillCompeting.contains(preferredParties.get(2))) {
                // third is present and it counts
                // his vote counts
                // the new vote will have less options than before and a weight
                List<PoliticalParty> newParties = preferredParties.subList(2, preferredParties.size());
                // add the vote with its weight to the list
                buffer.get(preferredParties.get(2)).add(new WVote(vote.getDesk(), newParties, vote.getProvince(), extraVoteWeight));
            }
        }
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

    public static void main(String[] args) {
        int VOTES_COUNT = 4000000;
        int DESK = 1;
        List<Vote> votes = new ArrayList<>();
        Random rand = new Random();
        List<PoliticalParty> parties = Arrays.asList(PoliticalParty.values());
        int num;
        for (int i = 0; i < VOTES_COUNT; i++) {
            //Collections.shuffle(parties);
            num = rand.nextInt(7); // tocando esto podemos hacer la votación más o menos parcial/random
            votes.add(new Vote(DESK, new ArrayList<>(parties.subList(num, num + 3)), Province.values()[rand.nextInt(3)]));
        }
        VotingSystems vs = new VotingSystems(votes);
        for (Province prov : Province.values()) {
            System.out.println(vs.stVoteProvicialLevel(prov));
        }
    }
}
