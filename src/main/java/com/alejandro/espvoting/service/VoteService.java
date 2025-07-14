package com.alejandro.espvoting.service;

import com.alejandro.espvoting.dto.VoteDTO;
import com.alejandro.espvoting.mapper.VoteMapper;
import com.alejandro.espvoting.model.Candidate;
import com.alejandro.espvoting.model.Election;
import com.alejandro.espvoting.model.PollingStation;
import com.alejandro.espvoting.model.QVote;
import com.alejandro.espvoting.model.Vote;
import com.alejandro.espvoting.model.Voter;
import com.alejandro.espvoting.repository.CandidateRepository;
import com.alejandro.espvoting.repository.ElectionRepository;
import com.alejandro.espvoting.repository.PollingStationRepository;
import com.alejandro.espvoting.repository.VoteRepository;
import com.alejandro.espvoting.repository.VoterRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service for managing Vote entities with QueryDSL for complex queries and Redis for caching.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VoteService {

    private final VoteRepository voteRepository;
    private final VoterRepository voterRepository;
    private final CandidateRepository candidateRepository;
    private final ElectionRepository electionRepository;
    private final PollingStationRepository pollingStationRepository;
    private final VoteMapper voteMapper;

    /**
     * Find all votes.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "votes")
    @Transactional(readOnly = true)
    public List<VoteDTO> findAll() {
        return voteMapper.toDto(voteRepository.findAll());
    }

    /**
     * Find a vote by ID.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "votes", key = "#id")
    @Transactional(readOnly = true)
    public Optional<VoteDTO> findById(Long id) {
        return voteRepository.findById(id)
                .map(voteMapper::toDto);
    }

    /**
     * Cast a vote.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = {"votes", "elections", "candidates", "regions", "districts", "pollingStations"}, allEntries = true)
    public VoteDTO castVote(VoteDTO voteDTO) {
        // Validate that the voter, candidate, election, and polling station exist
        Voter voter = voterRepository.findById(voteDTO.getVoterId())
                .orElseThrow(() -> new IllegalArgumentException("Voter not found"));

        Candidate candidate = candidateRepository.findById(voteDTO.getCandidateId())
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

        Election election = electionRepository.findById(voteDTO.getElectionId())
                .orElseThrow(() -> new IllegalArgumentException("Election not found"));

        PollingStation pollingStation = pollingStationRepository.findById(voteDTO.getPollingStationId())
                .orElseThrow(() -> new IllegalArgumentException("Polling station not found"));

        // Validate that the candidate is participating in the election
        if (!candidate.getElections().contains(election)) {
            throw new IllegalArgumentException("Candidate is not participating in the election");
        }

        // Validate that the election is active
        if (!election.getIsActive()) {
            throw new IllegalArgumentException("Election is not active");
        }

        // Validate that the voter hasn't already voted in this election
        QVote qVote = QVote.vote;
        BooleanExpression predicate = qVote.voter().id.eq(voter.getId())
                .and(qVote.election().id.eq(election.getId()));

        if (voteRepository.count(predicate) > 0) {
            throw new IllegalArgumentException("Voter has already cast a vote in this election");
        }

        // Create and save the vote
        Vote vote = new Vote();
        vote.setVoter(voter);
        vote.setCandidate(candidate);
        vote.setElection(election);
        vote.setPollingStation(pollingStation);
        vote.setTimestamp(LocalDateTime.now());
        vote.setIsValid(true);

        return voteMapper.toDto(voteRepository.save(vote));
    }

    /**
     * Update a vote.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = {"votes", "elections", "candidates", "regions", "districts", "pollingStations"}, allEntries = true)
    public Optional<VoteDTO> update(Long id, VoteDTO voteDTO) {
        return voteRepository.findById(id)
                .map(existingVote -> {
                    // Only allow updating the isValid flag
                    if (voteDTO.getIsValid() != null) {
                        existingVote.setIsValid(voteDTO.getIsValid());
                    }

                    return voteMapper.toDto(voteRepository.save(existingVote));
                });
    }

    /**
     * Delete a vote by ID.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = {"votes", "elections", "candidates", "regions", "districts", "pollingStations"}, allEntries = true)
    public void delete(Long id) {
        voteRepository.deleteById(id);
    }

    /**
     * Find votes by voter ID.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "votes", key = "'voter:' + #voterId")
    @Transactional(readOnly = true)
    public List<VoteDTO> findByVoterId(Long voterId) {
        return voteMapper.toDto(voteRepository.findByVoterId(voterId));
    }

    /**
     * Find votes by candidate ID.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "votes", key = "'candidate:' + #candidateId")
    @Transactional(readOnly = true)
    public List<VoteDTO> findByCandidateId(Long candidateId) {
        return voteMapper.toDto(voteRepository.findByCandidateId(candidateId));
    }

    /**
     * Find votes by election ID.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "votes", key = "'election:' + #electionId")
    @Transactional(readOnly = true)
    public List<VoteDTO> findByElectionId(Long electionId) {
        return voteMapper.toDto(voteRepository.findByElectionId(electionId));
    }

    /**
     * Find votes by polling station ID.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "votes", key = "'pollingStation:' + #pollingStationId")
    @Transactional(readOnly = true)
    public List<VoteDTO> findByPollingStationId(Long pollingStationId) {
        return voteMapper.toDto(voteRepository.findByPollingStationId(pollingStationId));
    }

    /**
     * Find votes with complex filtering.
     * This method demonstrates advanced filtering using QueryDSL.
     */
    @Transactional(readOnly = true)
    public List<VoteDTO> findVotesWithComplexFiltering(
            Long voterId, 
            Long candidateId, 
            Long electionId, 
            Long pollingStationId, 
            LocalDateTime startTime, 
            LocalDateTime endTime, 
            Boolean isValid) {

        QVote vote = QVote.vote;
        BooleanBuilder builder = new BooleanBuilder();

        // Filter by voter
        if (voterId != null) {
            builder.and(vote.voter().id.eq(voterId));
        }

        // Filter by candidate
        if (candidateId != null) {
            builder.and(vote.candidate().id.eq(candidateId));
        }

        // Filter by election
        if (electionId != null) {
            builder.and(vote.election().id.eq(electionId));
        }

        // Filter by polling station
        if (pollingStationId != null) {
            builder.and(vote.pollingStation().id.eq(pollingStationId));
        }

        // Filter by time range
        if (startTime != null) {
            builder.and(vote.timestamp.goe(startTime));
        }
        if (endTime != null) {
            builder.and(vote.timestamp.loe(endTime));
        }

        // Filter by validity
        if (isValid != null) {
            builder.and(vote.isValid.eq(isValid));
        }

        // Execute the query and convert to DTOs
        Iterable<Vote> votes = voteRepository.findAll(builder);
        return StreamSupport.stream(votes.spliterator(), false)
                .map(voteMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get vote counts by region.
     * This method demonstrates using repository methods with custom queries.
     */
    @Transactional(readOnly = true)
    public Map<String, Integer> getVoteCountsByRegion() {
        List<Object[]> results = voteRepository.countVotesByRegion();

        Map<String, Integer> voteCountsByRegion = new HashMap<>();
        for (Object[] result : results) {
            Long regionId = (Long) result[0];
            String regionName = (String) result[1];
            Long voteCount = (Long) result[2];

            voteCountsByRegion.put(regionName, voteCount.intValue());
        }

        return voteCountsByRegion;
    }

    /**
     * Get vote counts by district.
     * This method demonstrates using repository methods with custom queries.
     */
    @Transactional(readOnly = true)
    public Map<String, Integer> getVoteCountsByDistrict() {
        List<Object[]> results = voteRepository.countVotesByDistrict();

        Map<String, Integer> voteCountsByDistrict = new HashMap<>();
        for (Object[] result : results) {
            Long districtId = (Long) result[0];
            String districtName = (String) result[1];
            Long voteCount = (Long) result[2];

            voteCountsByDistrict.put(districtName, voteCount.intValue());
        }

        return voteCountsByDistrict;
    }

    /**
     * Get vote counts by candidate in a specific election.
     * This method demonstrates using repository methods with custom queries.
     */
    @Transactional(readOnly = true)
    public Map<String, Integer> getVoteCountsByCandidateInElection(Long electionId) {
        List<Object[]> results = voteRepository.countVotesByCandidateInElection(electionId);

        Map<String, Integer> voteCountsByCandidate = new HashMap<>();
        for (Object[] result : results) {
            Long candidateId = (Long) result[0];
            String firstName = (String) result[1];
            String lastName = (String) result[2];
            Long voteCount = (Long) result[3];

            String candidateName = firstName + " " + lastName;
            voteCountsByCandidate.put(candidateName, voteCount.intValue());
        }

        return voteCountsByCandidate;
    }
}
