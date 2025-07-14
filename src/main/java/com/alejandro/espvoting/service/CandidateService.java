package com.alejandro.espvoting.service;

import com.alejandro.espvoting.dto.CandidateDTO;
import com.alejandro.espvoting.mapper.CandidateMapper;
import com.alejandro.espvoting.model.Candidate;
import com.alejandro.espvoting.model.Election;
import com.alejandro.espvoting.model.QCandidate;
import com.alejandro.espvoting.repository.CandidateRepository;
import com.alejandro.espvoting.repository.ElectionRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service for managing Candidate entities with QueryDSL for complex queries and Redis for caching.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final ElectionRepository electionRepository;
    private final CandidateMapper candidateMapper;

    /**
     * Find all candidates.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "candidates")
    @Transactional(readOnly = true)
    public List<CandidateDTO> findAll() {
        return candidateMapper.toDto(candidateRepository.findAll());
    }

    /**
     * Find a candidate by ID.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "candidates", key = "#id")
    @Transactional(readOnly = true)
    public Optional<CandidateDTO> findById(Long id) {
        return candidateRepository.findById(id)
                .map(candidateMapper::toDto);
    }

    /**
     * Save a candidate.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "candidates", allEntries = true)
    public CandidateDTO save(CandidateDTO candidateDTO) {
        Candidate candidate = candidateMapper.toEntity(candidateDTO);
        
        // Set elections if electionIds are provided
        if (candidateDTO.getElectionIds() != null && !candidateDTO.getElectionIds().isEmpty()) {
            List<Election> elections = new ArrayList<>();
            candidateDTO.getElectionIds().forEach(electionId -> 
                electionRepository.findById(electionId).ifPresent(elections::add)
            );
            candidate.setElections(elections);
        }
        
        return candidateMapper.toDto(candidateRepository.save(candidate));
    }

    /**
     * Update a candidate.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "candidates", allEntries = true)
    public Optional<CandidateDTO> update(Long id, CandidateDTO candidateDTO) {
        return candidateRepository.findById(id)
                .map(existingCandidate -> {
                    candidateMapper.partialUpdate(existingCandidate, candidateDTO);
                    
                    // Update elections if electionIds are provided
                    if (candidateDTO.getElectionIds() != null && !candidateDTO.getElectionIds().isEmpty()) {
                        List<Election> elections = new ArrayList<>();
                        candidateDTO.getElectionIds().forEach(electionId -> 
                            electionRepository.findById(electionId).ifPresent(elections::add)
                        );
                        existingCandidate.setElections(elections);
                    }
                    
                    return candidateMapper.toDto(candidateRepository.save(existingCandidate));
                });
    }

    /**
     * Delete a candidate by ID.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "candidates", allEntries = true)
    public void delete(Long id) {
        candidateRepository.deleteById(id);
    }

    /**
     * Find candidates by party.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "candidates", key = "'party:' + #party")
    @Transactional(readOnly = true)
    public List<CandidateDTO> findByParty(String party) {
        return candidateMapper.toDto(candidateRepository.findByParty(party));
    }

    /**
     * Find candidates by election ID.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "candidates", key = "'election:' + #electionId")
    @Transactional(readOnly = true)
    public List<CandidateDTO> findByElectionId(Long electionId) {
        return candidateMapper.toDto(candidateRepository.findByElectionId(electionId));
    }

    /**
     * Find candidates with complex filtering.
     * This method demonstrates advanced filtering using QueryDSL.
     */
    @Transactional(readOnly = true)
    public List<CandidateDTO> findCandidatesWithComplexFiltering(
            String nameFilter, 
            List<String> parties, 
            List<Long> electionIds, 
            Integer minVotes) {
        
        QCandidate candidate = QCandidate.candidate;
        BooleanBuilder builder = new BooleanBuilder();

        // Filter by name (first name or last name)
        if (nameFilter != null && !nameFilter.isEmpty()) {
            builder.and(candidate.firstName.containsIgnoreCase(nameFilter)
                    .or(candidate.lastName.containsIgnoreCase(nameFilter)));
        }

        // Filter by parties
        if (parties != null && !parties.isEmpty()) {
            builder.and(candidate.party.in(parties));
        }

        // Filter by elections
        if (electionIds != null && !electionIds.isEmpty()) {
            // This is a more complex query that requires joining with the elections collection
            // We'll use a subquery approach
            BooleanExpression electionPredicate = null;
            for (Long electionId : electionIds) {
                BooleanExpression singleElectionPredicate = candidate.elections.any().id.eq(electionId);
                if (electionPredicate == null) {
                    electionPredicate = singleElectionPredicate;
                } else {
                    electionPredicate = electionPredicate.or(singleElectionPredicate);
                }
            }
            if (electionPredicate != null) {
                builder.and(electionPredicate);
            }
        }

        // Execute the query and convert to DTOs
        Iterable<Candidate> candidates = candidateRepository.findAll(builder);
        
        // If minVotes is specified, filter the results in memory
        // This is a simplification; in a real application, you might want to use a more efficient approach
        List<CandidateDTO> candidateDTOs = StreamSupport.stream(candidates.spliterator(), false)
                .map(candidateMapper::toDto)
                .collect(Collectors.toList());
        
        if (minVotes != null && minVotes > 0) {
            return candidateDTOs.stream()
                    .filter(dto -> dto.getVoteCount() >= minVotes)
                    .collect(Collectors.toList());
        }
        
        return candidateDTOs;
    }

    /**
     * Find top candidates by vote count.
     * This method demonstrates using repository methods with custom queries.
     */
    @Transactional(readOnly = true)
    public List<CandidateDTO> findTopCandidatesByVoteCount(int limit) {
        // Use the custom repository method to find candidates with vote count
        List<Object[]> results = candidateRepository.findCandidatesWithVoteCountGreaterThan(0L);
        
        // Convert the results to DTOs and sort by vote count
        return results.stream()
                .map(result -> {
                    Candidate candidate = (Candidate) result[0];
                    Long voteCount = (Long) result[1];
                    
                    CandidateDTO dto = candidateMapper.toDto(candidate);
                    dto.setVoteCount(voteCount.intValue());
                    return dto;
                })
                .sorted((c1, c2) -> c2.getVoteCount().compareTo(c1.getVoteCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}