package com.alejandro.espvoting.service;

import com.alejandro.espvoting.dto.ElectionDTO;
import com.alejandro.espvoting.mapper.ElectionMapper;
import com.alejandro.espvoting.model.Candidate;
import com.alejandro.espvoting.model.Election;
import com.alejandro.espvoting.model.QElection;
import com.alejandro.espvoting.repository.CandidateRepository;
import com.alejandro.espvoting.repository.ElectionRepository;
import com.alejandro.espvoting.repository.VoteRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service for managing Election entities with QueryDSL for complex queries and Redis for caching.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ElectionService {

    private final ElectionRepository electionRepository;
    private final CandidateRepository candidateRepository;
    private final VoteRepository voteRepository;
    private final ElectionMapper electionMapper;

    /**
     * Find all elections.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "elections")
    @Transactional(readOnly = true)
    public List<ElectionDTO> findAll() {
        List<Election> elections = electionRepository.findAll();
        List<ElectionDTO> electionDTOs = electionMapper.toDto(elections);
        
        // Calculate participation rates
        calculateParticipationRates(electionDTOs);
        
        return electionDTOs;
    }

    /**
     * Find an election by ID.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "elections", key = "#id")
    @Transactional(readOnly = true)
    public Optional<ElectionDTO> findById(Long id) {
        return electionRepository.findById(id)
                .map(election -> {
                    ElectionDTO dto = electionMapper.toDto(election);
                    calculateParticipationRate(dto);
                    return dto;
                });
    }

    /**
     * Save an election.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "elections", allEntries = true)
    public ElectionDTO save(ElectionDTO electionDTO) {
        Election election = electionMapper.toEntity(electionDTO);
        
        // Set candidates if candidateIds are provided
        if (electionDTO.getCandidateIds() != null && !electionDTO.getCandidateIds().isEmpty()) {
            List<Candidate> candidates = new ArrayList<>();
            electionDTO.getCandidateIds().forEach(candidateId -> 
                candidateRepository.findById(candidateId).ifPresent(candidates::add)
            );
            election.setCandidates(candidates);
        }
        
        Election savedElection = electionRepository.save(election);
        ElectionDTO savedDto = electionMapper.toDto(savedElection);
        calculateParticipationRate(savedDto);
        return savedDto;
    }

    /**
     * Update an election.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "elections", allEntries = true)
    public Optional<ElectionDTO> update(Long id, ElectionDTO electionDTO) {
        return electionRepository.findById(id)
                .map(existingElection -> {
                    electionMapper.partialUpdate(existingElection, electionDTO);
                    
                    // Update candidates if candidateIds are provided
                    if (electionDTO.getCandidateIds() != null && !electionDTO.getCandidateIds().isEmpty()) {
                        List<Candidate> candidates = new ArrayList<>();
                        electionDTO.getCandidateIds().forEach(candidateId -> 
                            candidateRepository.findById(candidateId).ifPresent(candidates::add)
                        );
                        existingElection.setCandidates(candidates);
                    }
                    
                    Election savedElection = electionRepository.save(existingElection);
                    ElectionDTO savedDto = electionMapper.toDto(savedElection);
                    calculateParticipationRate(savedDto);
                    return savedDto;
                });
    }

    /**
     * Delete an election by ID.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "elections", allEntries = true)
    public void delete(Long id) {
        electionRepository.deleteById(id);
    }

    /**
     * Find active elections.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "elections", key = "'active'")
    @Transactional(readOnly = true)
    public List<ElectionDTO> findActiveElections() {
        List<Election> elections = electionRepository.findByIsActiveTrue();
        List<ElectionDTO> electionDTOs = electionMapper.toDto(elections);
        calculateParticipationRates(electionDTOs);
        return electionDTOs;
    }

    /**
     * Find elections by type.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "elections", key = "'type:' + #electionType")
    @Transactional(readOnly = true)
    public List<ElectionDTO> findByElectionType(String electionType) {
        List<Election> elections = electionRepository.findByElectionType(electionType);
        List<ElectionDTO> electionDTOs = electionMapper.toDto(elections);
        calculateParticipationRates(electionDTOs);
        return electionDTOs;
    }

    /**
     * Find elections with complex filtering.
     * This method demonstrates advanced filtering using QueryDSL.
     */
    @Transactional(readOnly = true)
    public List<ElectionDTO> findElectionsWithComplexFiltering(
            String nameFilter, 
            List<String> electionTypes, 
            LocalDate startDate, 
            LocalDate endDate, 
            Boolean isActive) {
        
        QElection election = QElection.election;
        BooleanBuilder builder = new BooleanBuilder();

        // Filter by name
        if (nameFilter != null && !nameFilter.isEmpty()) {
            builder.and(election.name.containsIgnoreCase(nameFilter)
                    .or(election.description.containsIgnoreCase(nameFilter)));
        }

        // Filter by election types
        if (electionTypes != null && !electionTypes.isEmpty()) {
            builder.and(election.electionType.in(electionTypes));
        }

        // Filter by date range
        if (startDate != null) {
            builder.and(election.electionDate.goe(startDate));
        }
        if (endDate != null) {
            builder.and(election.electionDate.loe(endDate));
        }

        // Filter by active status
        if (isActive != null) {
            builder.and(election.isActive.eq(isActive));
        }

        // Execute the query and convert to DTOs
        Iterable<Election> elections = electionRepository.findAll(builder);
        List<ElectionDTO> electionDTOs = StreamSupport.stream(elections.spliterator(), false)
                .map(electionMapper::toDto)
                .collect(Collectors.toList());
        
        // Calculate participation rates
        calculateParticipationRates(electionDTOs);
        
        return electionDTOs;
    }

    /**
     * Find elections with vote statistics.
     * This method demonstrates using repository methods with custom queries.
     */
    @Transactional(readOnly = true)
    public List<ElectionDTO> findElectionsWithVoteStatistics() {
        // Use the custom repository method to find elections with vote count
        List<Object[]> results = electionRepository.findElectionsWithVoteCount();
        
        // Convert the results to DTOs
        return results.stream()
                .map(result -> {
                    Election election = (Election) result[0];
                    Long voteCount = (Long) result[1];
                    
                    ElectionDTO dto = electionMapper.toDto(election);
                    dto.setTotalVotes(voteCount.intValue());
                    calculateParticipationRate(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculate participation rates for a list of election DTOs.
     * This is a helper method used by other methods in this service.
     */
    private void calculateParticipationRates(List<ElectionDTO> electionDTOs) {
        electionDTOs.forEach(this::calculateParticipationRate);
    }

    /**
     * Calculate participation rate for a single election DTO.
     * This is a helper method used by other methods in this service.
     * 
     * The participation rate is calculated as the ratio of total votes to the total number of eligible voters.
     * For simplicity, we're using a fixed value for the total number of eligible voters.
     * In a real application, this would be calculated based on voter registration data.
     */
    private void calculateParticipationRate(ElectionDTO electionDTO) {
        // For demonstration purposes, we'll use a fixed value for the total number of eligible voters
        // In a real application, this would be calculated based on voter registration data
        final int TOTAL_ELIGIBLE_VOTERS = 1000;
        
        if (electionDTO.getTotalVotes() != null && electionDTO.getTotalVotes() > 0) {
            double participationRate = (double) electionDTO.getTotalVotes() / TOTAL_ELIGIBLE_VOTERS;
            electionDTO.setParticipationRate(participationRate);
        } else {
            electionDTO.setParticipationRate(0.0);
        }
    }
}