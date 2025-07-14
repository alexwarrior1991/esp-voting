package com.alejandro.espvoting.service;

import com.alejandro.espvoting.dto.RegionDTO;
import com.alejandro.espvoting.mapper.RegionMapper;
import com.alejandro.espvoting.model.QRegion;
import com.alejandro.espvoting.model.Region;
import com.alejandro.espvoting.repository.RegionRepository;
import com.alejandro.espvoting.repository.VoteRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service for managing Region entities with QueryDSL for complex queries and Redis for caching.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RegionService {

    private final RegionRepository regionRepository;
    private final VoteRepository voteRepository;
    private final RegionMapper regionMapper;

    /**
     * Find all regions.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "regions")
    @Transactional(readOnly = true)
    public List<RegionDTO> findAll() {
        List<Region> regions = regionRepository.findAll();
        List<RegionDTO> regionDTOs = regionMapper.toDto(regions);
        
        // Enhance DTOs with vote counts and participation rates
        enhanceRegionDTOs(regionDTOs);
        
        return regionDTOs;
    }

    /**
     * Find a region by ID.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "regions", key = "#id")
    @Transactional(readOnly = true)
    public Optional<RegionDTO> findById(Long id) {
        return regionRepository.findById(id)
                .map(region -> {
                    RegionDTO dto = regionMapper.toDto(region);
                    enhanceRegionDTO(dto);
                    return dto;
                });
    }

    /**
     * Save a region.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "regions", allEntries = true)
    public RegionDTO save(RegionDTO regionDTO) {
        Region region = regionMapper.toEntity(regionDTO);
        return regionMapper.toDto(regionRepository.save(region));
    }

    /**
     * Update a region.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "regions", allEntries = true)
    public Optional<RegionDTO> update(Long id, RegionDTO regionDTO) {
        return regionRepository.findById(id)
                .map(existingRegion -> {
                    regionMapper.partialUpdate(existingRegion, regionDTO);
                    return regionMapper.toDto(regionRepository.save(existingRegion));
                });
    }

    /**
     * Delete a region by ID.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "regions", allEntries = true)
    public void delete(Long id) {
        regionRepository.deleteById(id);
    }

    /**
     * Find a region by name.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "regions", key = "'name:' + #name")
    @Transactional(readOnly = true)
    public Optional<RegionDTO> findByName(String name) {
        return regionRepository.findByName(name)
                .map(region -> {
                    RegionDTO dto = regionMapper.toDto(region);
                    enhanceRegionDTO(dto);
                    return dto;
                });
    }

    /**
     * Find regions with population greater than the specified value.
     * This method demonstrates filtering using repository methods.
     */
    @Transactional(readOnly = true)
    public List<RegionDTO> findByPopulationGreaterThan(Integer population) {
        List<Region> regions = regionRepository.findByPopulationGreaterThan(population);
        List<RegionDTO> regionDTOs = regionMapper.toDto(regions);
        enhanceRegionDTOs(regionDTOs);
        return regionDTOs;
    }

    /**
     * Find regions with complex filtering.
     * This method demonstrates advanced filtering using QueryDSL.
     */
    @Transactional(readOnly = true)
    public List<RegionDTO> findRegionsWithComplexFiltering(
            String nameFilter, 
            Integer minPopulation, 
            Integer maxPopulation, 
            Integer minVoterCount) {
        
        QRegion region = QRegion.region;
        BooleanBuilder builder = new BooleanBuilder();

        // Filter by name
        if (nameFilter != null && !nameFilter.isEmpty()) {
            builder.and(region.name.containsIgnoreCase(nameFilter)
                    .or(region.description.containsIgnoreCase(nameFilter)));
        }

        // Filter by population range
        if (minPopulation != null) {
            builder.and(region.population.goe(minPopulation));
        }
        if (maxPopulation != null) {
            builder.and(region.population.loe(maxPopulation));
        }

        // Execute the query and convert to DTOs
        Iterable<Region> regions = regionRepository.findAll(builder);
        List<RegionDTO> regionDTOs = StreamSupport.stream(regions.spliterator(), false)
                .map(regionMapper::toDto)
                .collect(Collectors.toList());
        
        // Enhance DTOs with vote counts and participation rates
        enhanceRegionDTOs(regionDTOs);
        
        // Filter by voter count (in memory)
        if (minVoterCount != null && minVoterCount > 0) {
            regionDTOs = regionDTOs.stream()
                    .filter(dto -> dto.getVoterCount() >= minVoterCount)
                    .collect(Collectors.toList());
        }
        
        return regionDTOs;
    }

    /**
     * Find regions with voter statistics.
     * This method demonstrates using repository methods with custom queries.
     */
    @Transactional(readOnly = true)
    public List<RegionDTO> findRegionsWithVoterStatistics() {
        // Use the custom repository method to find regions with voter count
        List<Object[]> results = regionRepository.findRegionsWithVoterCount();
        
        // Convert the results to DTOs
        List<RegionDTO> regionDTOs = results.stream()
                .map(result -> {
                    Region region = (Region) result[0];
                    Long voterCount = (Long) result[1];
                    
                    RegionDTO dto = regionMapper.toDto(region);
                    dto.setVoterCount(voterCount.intValue());
                    return dto;
                })
                .collect(Collectors.toList());
        
        // Enhance DTOs with vote counts and participation rates
        enhanceRegionDTOs(regionDTOs);
        
        return regionDTOs;
    }

    /**
     * Find regions with vote statistics.
     * This method demonstrates using repository methods with custom queries.
     */
    @Transactional(readOnly = true)
    public List<RegionDTO> findRegionsWithVoteStatistics() {
        // Use the custom repository method to find regions with vote count
        List<Object[]> results = regionRepository.findRegionsWithVoteCount();
        
        // Convert the results to DTOs and calculate participation rates
        return results.stream()
                .map(result -> {
                    Long regionId = (Long) result[0];
                    String regionName = (String) result[1];
                    Long voteCount = (Long) result[2];
                    
                    // Find the region by ID
                    return regionRepository.findById(regionId)
                            .map(region -> {
                                RegionDTO dto = regionMapper.toDto(region);
                                dto.setVoteCount(voteCount.intValue());
                                
                                // Calculate participation rate
                                if (dto.getVoterCount() > 0) {
                                    double participationRate = (double) voteCount / dto.getVoterCount();
                                    dto.setParticipationRate(participationRate);
                                } else {
                                    dto.setParticipationRate(0.0);
                                }
                                
                                return dto;
                            })
                            .orElse(null);
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     * Enhance a list of region DTOs with vote counts and participation rates.
     * This is a helper method used by other methods in this service.
     */
    private void enhanceRegionDTOs(List<RegionDTO> regionDTOs) {
        // Get vote counts by region
        Map<String, Integer> voteCountsByRegion = voteRepository.countVotesByRegion().stream()
                .collect(Collectors.toMap(
                        result -> (String) result[1],  // Region name
                        result -> ((Long) result[2]).intValue()  // Vote count
                ));
        
        // Enhance each DTO
        regionDTOs.forEach(dto -> {
            // Set vote count
            Integer voteCount = voteCountsByRegion.getOrDefault(dto.getName(), 0);
            dto.setVoteCount(voteCount);
            
            // Calculate participation rate
            if (dto.getVoterCount() > 0) {
                double participationRate = (double) voteCount / dto.getVoterCount();
                dto.setParticipationRate(participationRate);
            } else {
                dto.setParticipationRate(0.0);
            }
        });
    }

    /**
     * Enhance a single region DTO with vote count and participation rate.
     * This is a helper method used by other methods in this service.
     */
    private void enhanceRegionDTO(RegionDTO dto) {
        // Get vote count for the region
        Map<String, Integer> voteCountsByRegion = voteRepository.countVotesByRegion().stream()
                .collect(Collectors.toMap(
                        result -> (String) result[1],  // Region name
                        result -> ((Long) result[2]).intValue()  // Vote count
                ));
        
        // Set vote count
        Integer voteCount = voteCountsByRegion.getOrDefault(dto.getName(), 0);
        dto.setVoteCount(voteCount);
        
        // Calculate participation rate
        if (dto.getVoterCount() > 0) {
            double participationRate = (double) voteCount / dto.getVoterCount();
            dto.setParticipationRate(participationRate);
        } else {
            dto.setParticipationRate(0.0);
        }
    }
}