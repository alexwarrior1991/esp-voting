package com.alejandro.espvoting.service;

import com.alejandro.espvoting.dto.VoteStatisticsDTO;
import com.alejandro.espvoting.model.QCandidate;
import com.alejandro.espvoting.model.QElection;
import com.alejandro.espvoting.model.QVote;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating vote statistics using QueryDSL.
 */
@Service
@RequiredArgsConstructor
public class VoteStatisticsService {

    private final EntityManager entityManager;

    /**
     * Gets vote statistics by candidate and election using QueryDSL with Tuple
     * and processing the results with streams and lambdas.
     * 
     * @param electionId The ID of the election to get statistics for
     * @return A list of vote statistics DTOs
     */
    @Transactional(readOnly = true)
    public List<VoteStatisticsDTO> getVoteStatisticsByCandidateAndElection(Long electionId) {
        QVote vote = QVote.vote;
        QCandidate candidate = QCandidate.candidate;
        QElection election = QElection.election;

        // Create query with QueryDSL that returns Tuple
        List<Tuple> results = new JPAQuery<Tuple>(entityManager)
                .select(
                    candidate.id,
                    candidate.firstName,
                    candidate.lastName,
                    candidate.party,
                    election.id,
                    election.name,
                    vote.count()
                )
                .from(vote)
                .join(vote.candidate(), candidate)
                .join(vote.election(), election)
                .where(election.id.eq(electionId))
                .groupBy(candidate.id, candidate.firstName, candidate.lastName, candidate.party, election.id, election.name)
                .fetch();

        // Process the results using stream and lambda to transform Tuple to DTO
        return results.stream()
                .map(tuple -> {
                    VoteStatisticsDTO dto = new VoteStatisticsDTO();
                    dto.setCandidateId(tuple.get(candidate.id));
                    dto.setCandidateFirstName(tuple.get(candidate.firstName));
                    dto.setCandidateLastName(tuple.get(candidate.lastName));
                    dto.setCandidateParty(tuple.get(candidate.party));
                    dto.setElectionId(tuple.get(election.id));
                    dto.setElectionName(tuple.get(election.name));
                    dto.setVoteCount(tuple.get(vote.count()));
                    
                    // Calculate vote percentage (example of additional processing)
                    double percentage = calculateVotePercentage(dto.getVoteCount(), electionId);
                    dto.setVotePercentage(percentage);
                    
                    return dto;
                })
                .sorted((dto1, dto2) -> Long.compare(dto2.getVoteCount(), dto1.getVoteCount())) // Sort by vote count (descending)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to calculate the percentage of votes for a candidate in an election.
     * 
     * @param candidateVotes The number of votes for the candidate
     * @param electionId The ID of the election
     * @return The percentage of votes for the candidate
     */
    private double calculateVotePercentage(Long candidateVotes, Long electionId) {
        QVote vote = QVote.vote;
        QElection election = QElection.election;
        
        Long totalVotes = new JPAQuery<Long>(entityManager)
                .select(vote.count())
                .from(vote)
                .join(vote.election(), election)
                .where(election.id.eq(electionId))
                .fetchOne();
        
        return totalVotes > 0 ? (candidateVotes * 100.0) / totalVotes : 0;
    }
}