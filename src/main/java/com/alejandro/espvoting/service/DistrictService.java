package com.alejandro.espvoting.service;

import com.alejandro.espvoting.dto.DistrictDTO;
import com.alejandro.espvoting.mapper.DistrictMapper;
import com.alejandro.espvoting.model.District;
import com.alejandro.espvoting.model.PollingStation;
import com.alejandro.espvoting.model.QDistrict;
import com.alejandro.espvoting.model.Region;
import com.alejandro.espvoting.repository.DistrictRepository;
import com.alejandro.espvoting.repository.PollingStationRepository;
import com.alejandro.espvoting.repository.RegionRepository;
import com.alejandro.espvoting.repository.VoteRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service for managing District entities with QueryDSL for complex queries and Redis for caching.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DistrictService {

    private final DistrictRepository districtRepository;
    private final RegionRepository regionRepository;
    private final PollingStationRepository pollingStationRepository;
    private final VoteRepository voteRepository;
    private final DistrictMapper districtMapper;

    /**
     * Find all districts.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "districts")
    @Transactional(readOnly = true)
    public List<DistrictDTO> findAll() {
        List<District> districts = districtRepository.findAll();
        List<DistrictDTO> districtDTOs = districtMapper.toDto(districts);
        
        // Enhance DTOs with vote counts and participation rates
        enhanceDistrictDTOs(districtDTOs);
        
        return districtDTOs;
    }

    /**
     * Find a district by ID.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "districts", key = "#id")
    @Transactional(readOnly = true)
    public Optional<DistrictDTO> findById(Long id) {
        return districtRepository.findById(id)
                .map(district -> {
                    DistrictDTO dto = districtMapper.toDto(district);
                    enhanceDistrictDTO(dto);
                    return dto;
                });
    }

    /**
     * Save a district.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "districts", allEntries = true)
    public DistrictDTO save(DistrictDTO districtDTO) {
        District district = districtMapper.toEntity(districtDTO);
        
        // Set region if regionId is provided
        if (districtDTO.getRegionId() != null) {
            regionRepository.findById(districtDTO.getRegionId())
                    .ifPresent(district::setRegion);
        }
        
        // Set polling stations if pollingStationIds are provided
        if (districtDTO.getPollingStationIds() != null && !districtDTO.getPollingStationIds().isEmpty()) {
            List<PollingStation> pollingStations = new ArrayList<>();
            districtDTO.getPollingStationIds().forEach(pollingStationId -> 
                pollingStationRepository.findById(pollingStationId).ifPresent(pollingStations::add)
            );
            district.setPollingStations(pollingStations);
        }
        
        District savedDistrict = districtRepository.save(district);
        DistrictDTO savedDto = districtMapper.toDto(savedDistrict);
        enhanceDistrictDTO(savedDto);
        return savedDto;
    }

    /**
     * Update a district.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "districts", allEntries = true)
    public Optional<DistrictDTO> update(Long id, DistrictDTO districtDTO) {
        return districtRepository.findById(id)
                .map(existingDistrict -> {
                    districtMapper.partialUpdate(existingDistrict, districtDTO);
                    
                    // Update region if regionId is provided
                    if (districtDTO.getRegionId() != null) {
                        regionRepository.findById(districtDTO.getRegionId())
                                .ifPresent(existingDistrict::setRegion);
                    }
                    
                    // Update polling stations if pollingStationIds are provided
                    if (districtDTO.getPollingStationIds() != null && !districtDTO.getPollingStationIds().isEmpty()) {
                        List<PollingStation> pollingStations = new ArrayList<>();
                        districtDTO.getPollingStationIds().forEach(pollingStationId -> 
                            pollingStationRepository.findById(pollingStationId).ifPresent(pollingStations::add)
                        );
                        existingDistrict.setPollingStations(pollingStations);
                    }
                    
                    District savedDistrict = districtRepository.save(existingDistrict);
                    DistrictDTO savedDto = districtMapper.toDto(savedDistrict);
                    enhanceDistrictDTO(savedDto);
                    return savedDto;
                });
    }

    /**
     * Delete a district by ID.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "districts", allEntries = true)
    public void delete(Long id) {
        districtRepository.deleteById(id);
    }

    /**
     * Find districts by region ID.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "districts", key = "'region:' + #regionId")
    @Transactional(readOnly = true)
    public List<DistrictDTO> findByRegionId(Long regionId) {
        List<District> districts = districtRepository.findByRegionId(regionId);
        List<DistrictDTO> districtDTOs = districtMapper.toDto(districts);
        enhanceDistrictDTOs(districtDTOs);
        return districtDTOs;
    }

    /**
     * Find a district by code.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "districts", key = "'code:' + #code")
    @Transactional(readOnly = true)
    public Optional<DistrictDTO> findByCode(String code) {
        return districtRepository.findByCode(code)
                .map(district -> {
                    DistrictDTO dto = districtMapper.toDto(district);
                    enhanceDistrictDTO(dto);
                    return dto;
                });
    }

    /**
     * Find districts with complex filtering.
     * This method demonstrates advanced filtering using QueryDSL.
     */
    @Transactional(readOnly = true)
    public List<DistrictDTO> findDistrictsWithComplexFiltering(
            String nameFilter, 
            List<Long> regionIds, 
            Integer minPopulation, 
            Integer maxPopulation) {
        
        QDistrict district = QDistrict.district;
        BooleanBuilder builder = new BooleanBuilder();

        // Filter by name or code
        if (nameFilter != null && !nameFilter.isEmpty()) {
            builder.and(district.name.containsIgnoreCase(nameFilter)
                    .or(district.code.containsIgnoreCase(nameFilter)));
        }

        // Filter by regions
        if (regionIds != null && !regionIds.isEmpty()) {
            builder.and(district.region().id.in(regionIds));
        }

        // Filter by population range
        if (minPopulation != null) {
            builder.and(district.population.goe(minPopulation));
        }
        if (maxPopulation != null) {
            builder.and(district.population.loe(maxPopulation));
        }

        // Execute the query and convert to DTOs
        Iterable<District> districts = districtRepository.findAll(builder);
        List<DistrictDTO> districtDTOs = StreamSupport.stream(districts.spliterator(), false)
                .map(districtMapper::toDto)
                .collect(Collectors.toList());
        
        // Enhance DTOs with vote counts and participation rates
        enhanceDistrictDTOs(districtDTOs);
        
        return districtDTOs;
    }

    /**
     * Find districts with voter statistics.
     * This method demonstrates using repository methods with custom queries.
     */
    @Transactional(readOnly = true)
    public List<DistrictDTO> findDistrictsWithVoterStatistics() {
        // Use the custom repository method to find districts with voter count
        List<Object[]> results = districtRepository.findDistrictsWithVoterCount();
        
        // Convert the results to DTOs
        List<DistrictDTO> districtDTOs = results.stream()
                .map(result -> {
                    District district = (District) result[0];
                    Long voterCount = (Long) result[1];
                    
                    DistrictDTO dto = districtMapper.toDto(district);
                    dto.setVoterCount(voterCount.intValue());
                    return dto;
                })
                .collect(Collectors.toList());
        
        // Enhance DTOs with vote counts and participation rates
        enhanceDistrictDTOs(districtDTOs);
        
        return districtDTOs;
    }

    /**
     * Find districts with vote statistics.
     * This method demonstrates using repository methods with custom queries.
     */
    @Transactional(readOnly = true)
    public List<DistrictDTO> findDistrictsWithVoteStatistics() {
        // Use the custom repository method to find districts with vote count
        List<Object[]> results = districtRepository.findDistrictsWithVoteCount();
        
        // Convert the results to DTOs and calculate participation rates
        return results.stream()
                .map(result -> {
                    Long districtId = (Long) result[0];
                    String districtName = (String) result[1];
                    Long voteCount = (Long) result[2];
                    
                    // Find the district by ID
                    return districtRepository.findById(districtId)
                            .map(district -> {
                                DistrictDTO dto = districtMapper.toDto(district);
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
     * Enhance a list of district DTOs with vote counts and participation rates.
     * This is a helper method used by other methods in this service.
     */
    private void enhanceDistrictDTOs(List<DistrictDTO> districtDTOs) {
        // Get vote counts by district
        Map<String, Integer> voteCountsByDistrict = voteRepository.countVotesByDistrict().stream()
                .collect(Collectors.toMap(
                        result -> (String) result[1],  // District name
                        result -> ((Long) result[2]).intValue()  // Vote count
                ));
        
        // Enhance each DTO
        districtDTOs.forEach(dto -> {
            // Set vote count
            Integer voteCount = voteCountsByDistrict.getOrDefault(dto.getName(), 0);
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
     * Enhance a single district DTO with vote count and participation rate.
     * This is a helper method used by other methods in this service.
     */
    private void enhanceDistrictDTO(DistrictDTO dto) {
        // Get vote count for the district
        Map<String, Integer> voteCountsByDistrict = voteRepository.countVotesByDistrict().stream()
                .collect(Collectors.toMap(
                        result -> (String) result[1],  // District name
                        result -> ((Long) result[2]).intValue()  // Vote count
                ));
        
        // Set vote count
        Integer voteCount = voteCountsByDistrict.getOrDefault(dto.getName(), 0);
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