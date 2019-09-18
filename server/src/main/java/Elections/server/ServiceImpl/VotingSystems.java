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
        Returns true/false whether the vote was transferred or not
    */
    private boolean doTransferVoteAV(Map<PoliticalParty, List<Vote>> masterMap, List<PoliticalParty> eliminatedParties, Vote vote) {
        List<PoliticalParty> parties = vote.getPreferredParties();
        int i = 0;
        while (i < parties.size() && eliminatedParties.contains(parties.get(i))) {
            // Option: vote already transferred => false, shifting the list
            // Option: vote about to be transferred, but the next candidate was already eliminated => false, shifting the list
            // Option: vote to the eliminated candidate => vote about to be transferred
            // false, shifting the list
            i++;
        }
        if (i != parties.size()) {
            // there is a candidate that is still running => he gets transferred a vote
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
        Returns the amount of transferred votes
     */
    private int transferVotesAV(Map<PoliticalParty, List<Vote>> masterMap, List<PoliticalParty> eliminatedParties, List<Vote> transferableVotes) {
        int count = 0;
        for (Vote vote : transferableVotes) {
            if (doTransferVoteAV(masterMap, eliminatedParties, vote)) {
                count++;
            }
        }
        return count;
    }

    /*
        returns the Pair (percentage of votes, political party) from the winner
     */
    private List<Pair<BigDecimal, PoliticalParty>> alternativeVoteNationalLevelREC(Map<PoliticalParty, List<Vote>> masterMap, List<PoliticalParty> eliminatedParties, int total) {
        // map gets sorted
        // could use the stream's max/min but it is better to sort it only once
        List<Map.Entry<PoliticalParty, List<Vote>>> sortedEntries = masterMap.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .collect(Collectors.toList());
        if (sortedEntries.isEmpty() || (sortedEntries.get(0).getValue().size() / (double) total) > 0.5) {
            // there is a winner
            return sortedEntries.stream().map((e) -> new Pair<>(
                    new BigDecimal((e.getValue().size() / (double) total) * 100).setScale(2, BigDecimal.ROUND_DOWN),
                    e.getKey())).collect(Collectors.toList());
        }
        Map.Entry<PoliticalParty, List<Vote>> loser = sortedEntries.get(sortedEntries.size() - 1);
        List<Map.Entry<PoliticalParty, List<Vote>>> losers = sortedEntries.stream().filter(e -> e.getValue().size() == loser.getValue().size()).collect(Collectors.toList());

        losers.forEach(l -> masterMap.remove(l.getKey()));
        losers.forEach(l -> eliminatedParties.add(l.getKey()));

        int losersTrasnferredVotes = 0;
        int losersTotalVotes = 0;
        for (Map.Entry<PoliticalParty, List<Vote>> e : losers) {
            losersTotalVotes += e.getValue().size();
            losersTrasnferredVotes += transferVotesAV(masterMap, eliminatedParties, e.getValue());
        }
        int votesLost = losersTotalVotes - losersTrasnferredVotes;
        return alternativeVoteNationalLevelREC(masterMap, eliminatedParties, total - votesLost);
    }

    /*
        returns the Pair (percentage of votes, political party) from the winner
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
    // We assume that there will never be an election where #candidates <= WINNERS_PER_PROVINCE

    List<Pair<BigDecimal, PoliticalParty>> stVoteProvicialLevel(Province prov) {
        Supplier<Stream<Vote>> supplier = () -> votes.stream().filter(x -> x.getProvince() == prov);
        long provinceCount = supplier.get().count();
        if (provinceCount <= 0) {
            System.out.println("There are no votes for this province: " + prov.toString());
            return null;
        }
        // We need a cleaner structures
        // In this set we keep all candidates who are still participating
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
                .thenComparing(Collections.reverseOrder(Comparator.comparing(o -> o.getKey().name())));

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
        stVoteProvincialLevelREC(set, stillCompeting, WINNERS_PER_PROVINCE, threshold, winnersList);

        // We return the winners with with the FRACTION of votes they won over the total amount of votes
        return winnersList.stream()
                .sorted(comp.reversed())
                .map((Pair<PoliticalParty, VoteList> p) -> new Pair<>(new BigDecimal((p.getValue().votes / provinceCount) * 100).setScale(2, BigDecimal.ROUND_DOWN), p.getKey()))
                .collect(Collectors.toList());
    }


    private void stVoteProvincialLevelREC(Set<Pair<PoliticalParty, VoteList>> masterSet, Set<PoliticalParty> stillCompeting, int availableSeats, double threshold, List<Pair<PoliticalParty, VoteList>> winnersSet) {
        // stillCompeting list should always contain candidates that did not won nor where eliminated
        availableSeats -= checkForWinners(masterSet, threshold, stillCompeting, winnersSet);

        // We check that there still are candidates competing
        Supplier<Stream<Pair<PoliticalParty, VoteList>>> supplier = () -> masterSet.stream()
                .filter(c -> stillCompeting.contains(c.getKey()) && c.getValue().size() > 0); // c.getValue().size() > 0 makes the difference
        long stillCompetingCount = supplier.get().count();
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
            // We did not find enough winners, we have to eliminate losers and distribute their votes
            List<Pair<PoliticalParty, VoteList>> eliminated = eliminateLooser(masterSet, stillCompeting, threshold);
            List<Pair<PoliticalParty, VoteList>> stillCompetingFiltered = supplier.get().collect(Collectors.toList());
            if (stillCompetingFiltered.size() < availableSeats) {
                // We just deleted more candidates than needed to determine a winner
                // so we need to undo a bit to determine who will win
                // First we add all candidates who are still competing to the winners list
                for (Pair<PoliticalParty, VoteList> candidate : masterSet) {
                    if (stillCompetingFiltered.contains(candidate)) {
                        winnersSet.add(candidate);
                        stillCompeting.remove(candidate.getKey());
                        availableSeats--;
                    }
                }
                // Then we order the list of eliminated candidates alphabetically and add them
                // to the winners list until all seats are filled
                // they  have already been added removed from the stillCompeting set
                eliminated.sort(Comparator.comparing(o -> o.getKey().name()));
                winnersSet.addAll(eliminated.subList(0, availableSeats));
                return;
            }
        }
        stVoteProvincialLevelREC(masterSet, stillCompeting, availableSeats, threshold, winnersSet);
    }

    private List<Pair<PoliticalParty, VoteList>> eliminateLooser(Set<Pair<PoliticalParty, VoteList>> masterSet, Set<PoliticalParty> stillCompeting, double threshold) {
        // buffer to keep track of eliminated candidates
        List<Pair<PoliticalParty, VoteList>> eliminated = new ArrayList<>();
        // buffer to transfer left overs at the end of this method
        Map<PoliticalParty, VoteList> buffer = new HashMap<>();
        // init buffer
        for (PoliticalParty pp : PoliticalParty.values()) {
            buffer.put(pp, new VoteList());
        }
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
                // if this happens, it means that we have already eliminated all the losers
                // we distribute left overs and leave
                redistributeVotes(buffer, masterSet);
                return eliminated;
            }
            // we eliminate the looser
            stillCompeting.remove(candidate.getKey());
            eliminated.add(candidate);
            // we will give this votes a weight of 1
            bufferLeftoverVotes(candidate, buffer, 1.0, stillCompeting);
            smallestVotes = newSmallestVotes;
        }
        // we do not redistributeVotes(buffer, masterSet) because if we reach here
        // it means that smallestVotes was always == newSmallestVotes
        // which means that parties had the same amount of votes
        return eliminated;
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
                    // transferred to other candidates
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
        // transfer votes, we assume that all candidates in the buffer are competing
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


    /**
     * Test for the state results
     */
    public static void main(String[] args) {
        int VOTES_COUNT = 4000000;
        int DESK = 1;
        List<Vote> votes = new ArrayList<>();
        Random rand = new Random();
        List<PoliticalParty> parties = Arrays.asList(PoliticalParty.values());
        int num;
        for (int i = 0; i < VOTES_COUNT; i++) {
            //Collections.shuffle(parties);
            num = rand.nextInt(7); // this changes how biased the elections are
            votes.add(new Vote(DESK, new ArrayList<>(parties.subList(num, num + 3)), Province.values()[rand.nextInt(3)]));
        }
        VotingSystems vs = new VotingSystems(votes);
        for (Province prov : Province.values()) {
            System.out.println(vs.stVoteProvicialLevel(prov));
        }
        votes = new ArrayList<>();
        votes.add(new Vote(1, Collections.singletonList(PoliticalParty.BUFFALO), Province.JUNGLE));
        votes.add(new Vote(1, Collections.singletonList(PoliticalParty.BUFFALO), Province.JUNGLE));
        votes.add(new Vote(1, Collections.singletonList(PoliticalParty.GORILLA), Province.JUNGLE));
        votes.add(new Vote(1, Collections.singletonList(PoliticalParty.GORILLA), Province.JUNGLE));
        votes.add(new Vote(1, Collections.singletonList(PoliticalParty.WHITE_GORILLA), Province.JUNGLE));
        votes.add(new Vote(1, Collections.singletonList(PoliticalParty.JACKALOPE), Province.JUNGLE));
        votes.add(new Vote(1, Collections.singletonList(PoliticalParty.LEOPARD), Province.JUNGLE));
        votes.add(new Vote(1, Collections.singletonList(PoliticalParty.MONKEY), Province.JUNGLE));

        Collections.shuffle(votes);
        vs = new VotingSystems(votes);
        List<Pair<BigDecimal, PoliticalParty>> resp = vs.stVoteProvicialLevel(Province.JUNGLE);
        resp.forEach( c -> System.out.println(c.getKey() + ";" + c.getValue()));
    }
}
