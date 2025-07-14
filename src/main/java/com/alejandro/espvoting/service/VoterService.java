package com.alejandro.espvoting.service;

import com.alejandro.espvoting.dto.VoterDTO;
import com.alejandro.espvoting.mapper.VoterMapper;
import com.alejandro.espvoting.model.QVoter;
import com.alejandro.espvoting.model.Voter;
import com.alejandro.espvoting.repository.DistrictRepository;
import com.alejandro.espvoting.repository.RegionRepository;
import com.alejandro.espvoting.repository.VoterRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service for managing Voter entities with QueryDSL for complex queries and Redis for caching.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VoterService {

    private final VoterRepository voterRepository;
    private final RegionRepository regionRepository;
    private final DistrictRepository districtRepository;
    private final VoterMapper voterMapper;

    /**
     * Find all voters.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "voters")
    @Transactional(readOnly = true)
    public List<VoterDTO> findAll() {
        return voterMapper.toDto(voterRepository.findAll());
    }

    /**
     * Find a voter by ID.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "voters", key = "#id")
    @Transactional(readOnly = true)
    public Optional<VoterDTO> findById(Long id) {
        return voterRepository.findById(id)
                .map(voterMapper::toDto);
    }

    /**
     * Save a voter.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "voters", allEntries = true)
    public VoterDTO save(VoterDTO voterDTO) {
        Voter voter = voterMapper.toEntity(voterDTO);

        // Set region if regionId is provided
        if (voterDTO.getRegionId() != null) {
            regionRepository.findById(voterDTO.getRegionId())
                    .ifPresent(voter::setRegion);
        }

        // Set district if districtId is provided
        if (voterDTO.getDistrictId() != null) {
            districtRepository.findById(voterDTO.getDistrictId())
                    .ifPresent(voter::setDistrict);
        }

        return voterMapper.toDto(voterRepository.save(voter));
    }

    /**
     * Update a voter.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "voters", allEntries = true)
    public Optional<VoterDTO> update(Long id, VoterDTO voterDTO) {
        return voterRepository.findById(id)
                .map(existingVoter -> {
                    voterMapper.partialUpdate(existingVoter, voterDTO);

                    // Update region if regionId is provided
                    if (voterDTO.getRegionId() != null) {
                        regionRepository.findById(voterDTO.getRegionId())
                                .ifPresent(existingVoter::setRegion);
                    }

                    // Update district if districtId is provided
                    if (voterDTO.getDistrictId() != null) {
                        districtRepository.findById(voterDTO.getDistrictId())
                                .ifPresent(existingVoter::setDistrict);
                    }

                    return voterMapper.toDto(voterRepository.save(existingVoter));
                });
    }

    /**
     * Delete a voter by ID.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "voters", allEntries = true)
    public void delete(Long id) {
        voterRepository.deleteById(id);
    }

    /**
     * Find voters by region, age range, and sex.
     * This method demonstrates complex queries using QueryDSL.
     */
    @Transactional(readOnly = true)
    public List<VoterDTO> findVotersByRegionAgeAndSex(Long regionId, Integer minAge, Integer maxAge, String sex) {
        QVoter voter = QVoter.voter;
        BooleanBuilder builder = new BooleanBuilder();

        // Add conditions based on parameters
        if (regionId != null) {
            builder.and(voter.region().id.eq(regionId));
        }

        if (minAge != null) {
            LocalDate maxBirthDate = LocalDate.now().minusYears(minAge);
            builder.and(voter.dateOfBirth.before(maxBirthDate).or(voter.dateOfBirth.eq(maxBirthDate)));
        }

        if (maxAge != null) {
            LocalDate minBirthDate = LocalDate.now().minusYears(maxAge + 1);
            builder.and(voter.dateOfBirth.after(minBirthDate));
        }

        if (sex != null && !sex.isEmpty()) {
            builder.and(voter.sex.eq(sex));
        }

        // Only include active voters
        builder.and(voter.isActive.isTrue());

        // Execute the query and convert to DTOs
        Iterable<Voter> voters = voterRepository.findAll(builder);
        return StreamSupport.stream(voters.spliterator(), false)
                .map(voterMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Find voters by name (first name or last name).
     * This method demonstrates text search using QueryDSL.
     */
    @Transactional(readOnly = true)
    public List<VoterDTO> findVotersByName(String name) {
        if (name == null || name.isEmpty()) {
            return List.of();
        }

        QVoter voter = QVoter.voter;
        BooleanExpression predicate = voter.firstName.containsIgnoreCase(name)
                .or(voter.lastName.containsIgnoreCase(name));

        // Execute the query and convert to DTOs
        Iterable<Voter> voters = voterRepository.findAll(predicate);
        return StreamSupport.stream(voters.spliterator(), false)
                .map(voterMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Find voters with complex filtering.
     * This method demonstrates advanced filtering using QueryDSL.
     */
    @Transactional(readOnly = true)
    public List<VoterDTO> findVotersWithComplexFiltering(
            String nameFilter, 
            List<Long> regionIds, 
            List<Long> districtIds, 
            Boolean hasVoted, 
            LocalDate registeredBefore) {

        QVoter voter = QVoter.voter;
        BooleanBuilder builder = new BooleanBuilder();

        // Filter by name (first name or last name)
        if (nameFilter != null && !nameFilter.isEmpty()) {
            builder.and(voter.firstName.containsIgnoreCase(nameFilter)
                    .or(voter.lastName.containsIgnoreCase(nameFilter)));
        }

        // Filter by regions
        if (regionIds != null && !regionIds.isEmpty()) {
            builder.and(voter.region().id.in(regionIds));
        }

        // Filter by districts
        if (districtIds != null && !districtIds.isEmpty()) {
            builder.and(voter.district().id.in(districtIds));
        }

        // Filter by voting status
        if (hasVoted != null) {
            if (hasVoted) {
                builder.and(voter.votes.isNotEmpty());
            } else {
                builder.and(voter.votes.isEmpty());
            }
        }

        // Only include active voters
        builder.and(voter.isActive.isTrue());

        // Execute the query and convert to DTOs
        Iterable<Voter> voters = voterRepository.findAll(builder);
        return StreamSupport.stream(voters.spliterator(), false)
                .map(voterMapper::toDto)
                .collect(Collectors.toList());
    }
}
