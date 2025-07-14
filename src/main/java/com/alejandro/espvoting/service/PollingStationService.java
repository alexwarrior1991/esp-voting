package com.alejandro.espvoting.service;

import com.alejandro.espvoting.dto.PollingStationDTO;
import com.alejandro.espvoting.mapper.PollingStationMapper;
import com.alejandro.espvoting.model.District;
import com.alejandro.espvoting.model.PollingStation;
import com.alejandro.espvoting.model.QPollingStation;
import com.alejandro.espvoting.repository.DistrictRepository;
import com.alejandro.espvoting.repository.PollingStationRepository;
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
 * Service for managing PollingStation entities with QueryDSL for complex queries and Redis for caching.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PollingStationService {

    private final PollingStationRepository pollingStationRepository;
    private final DistrictRepository districtRepository;
    private final PollingStationMapper pollingStationMapper;

    /**
     * Find all polling stations.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "pollingStations")
    @Transactional(readOnly = true)
    public List<PollingStationDTO> findAll() {
        List<PollingStation> pollingStations = pollingStationRepository.findAll();
        List<PollingStationDTO> pollingStationDTOs = pollingStationMapper.toDto(pollingStations);
        
        // Calculate utilization rates
        calculateUtilizationRates(pollingStationDTOs);
        
        return pollingStationDTOs;
    }

    /**
     * Find a polling station by ID.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "pollingStations", key = "#id")
    @Transactional(readOnly = true)
    public Optional<PollingStationDTO> findById(Long id) {
        return pollingStationRepository.findById(id)
                .map(pollingStation -> {
                    PollingStationDTO dto = pollingStationMapper.toDto(pollingStation);
                    calculateUtilizationRate(dto);
                    return dto;
                });
    }

    /**
     * Save a polling station.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "pollingStations", allEntries = true)
    public PollingStationDTO save(PollingStationDTO pollingStationDTO) {
        PollingStation pollingStation = pollingStationMapper.toEntity(pollingStationDTO);
        
        // Set districts if districtIds are provided
        if (pollingStationDTO.getDistrictIds() != null && !pollingStationDTO.getDistrictIds().isEmpty()) {
            List<District> districts = new ArrayList<>();
            pollingStationDTO.getDistrictIds().forEach(districtId -> 
                districtRepository.findById(districtId).ifPresent(districts::add)
            );
            pollingStation.setDistricts(districts);
        }
        
        PollingStation savedPollingStation = pollingStationRepository.save(pollingStation);
        PollingStationDTO savedDto = pollingStationMapper.toDto(savedPollingStation);
        calculateUtilizationRate(savedDto);
        return savedDto;
    }

    /**
     * Update a polling station.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "pollingStations", allEntries = true)
    public Optional<PollingStationDTO> update(Long id, PollingStationDTO pollingStationDTO) {
        return pollingStationRepository.findById(id)
                .map(existingPollingStation -> {
                    pollingStationMapper.partialUpdate(existingPollingStation, pollingStationDTO);
                    
                    // Update districts if districtIds are provided
                    if (pollingStationDTO.getDistrictIds() != null && !pollingStationDTO.getDistrictIds().isEmpty()) {
                        List<District> districts = new ArrayList<>();
                        pollingStationDTO.getDistrictIds().forEach(districtId -> 
                            districtRepository.findById(districtId).ifPresent(districts::add)
                        );
                        existingPollingStation.setDistricts(districts);
                    }
                    
                    PollingStation savedPollingStation = pollingStationRepository.save(existingPollingStation);
                    PollingStationDTO savedDto = pollingStationMapper.toDto(savedPollingStation);
                    calculateUtilizationRate(savedDto);
                    return savedDto;
                });
    }

    /**
     * Delete a polling station by ID.
     * This method evicts the cache to ensure data consistency.
     */
    @CacheEvict(value = "pollingStations", allEntries = true)
    public void delete(Long id) {
        pollingStationRepository.deleteById(id);
    }

    /**
     * Find active polling stations.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "pollingStations", key = "'active'")
    @Transactional(readOnly = true)
    public List<PollingStationDTO> findActivePollingStations() {
        List<PollingStation> pollingStations = pollingStationRepository.findByIsActiveTrue();
        List<PollingStationDTO> pollingStationDTOs = pollingStationMapper.toDto(pollingStations);
        calculateUtilizationRates(pollingStationDTOs);
        return pollingStationDTOs;
    }

    /**
     * Find polling stations by district ID.
     * This method is cached to improve performance.
     */
    @Cacheable(value = "pollingStations", key = "'district:' + #districtId")
    @Transactional(readOnly = true)
    public List<PollingStationDTO> findByDistrictId(Long districtId) {
        List<PollingStation> pollingStations = pollingStationRepository.findByDistrictId(districtId);
        List<PollingStationDTO> pollingStationDTOs = pollingStationMapper.toDto(pollingStations);
        calculateUtilizationRates(pollingStationDTOs);
        return pollingStationDTOs;
    }

    /**
     * Find polling stations with complex filtering.
     * This method demonstrates advanced filtering using QueryDSL.
     */
    @Transactional(readOnly = true)
    public List<PollingStationDTO> findPollingStationsWithComplexFiltering(
            String nameFilter, 
            String addressFilter, 
            Integer minCapacity, 
            Integer maxCapacity, 
            Boolean isActive, 
            List<Long> districtIds) {
        
        QPollingStation pollingStation = QPollingStation.pollingStation;
        BooleanBuilder builder = new BooleanBuilder();

        // Filter by name
        if (nameFilter != null && !nameFilter.isEmpty()) {
            builder.and(pollingStation.name.containsIgnoreCase(nameFilter));
        }

        // Filter by address
        if (addressFilter != null && !addressFilter.isEmpty()) {
            builder.and(pollingStation.address.containsIgnoreCase(addressFilter));
        }

        // Filter by capacity range
        if (minCapacity != null) {
            builder.and(pollingStation.capacity.goe(minCapacity));
        }
        if (maxCapacity != null) {
            builder.and(pollingStation.capacity.loe(maxCapacity));
        }

        // Filter by active status
        if (isActive != null) {
            builder.and(pollingStation.isActive.eq(isActive));
        }

        // Filter by districts
        if (districtIds != null && !districtIds.isEmpty()) {
            // This is a more complex query that requires joining with the districts collection
            // We'll use a subquery approach
            BooleanExpression districtPredicate = null;
            for (Long districtId : districtIds) {
                BooleanExpression singleDistrictPredicate = pollingStation.districts.any().id.eq(districtId);
                if (districtPredicate == null) {
                    districtPredicate = singleDistrictPredicate;
                } else {
                    districtPredicate = districtPredicate.or(singleDistrictPredicate);
                }
            }
            if (districtPredicate != null) {
                builder.and(districtPredicate);
            }
        }

        // Execute the query and convert to DTOs
        Iterable<PollingStation> pollingStations = pollingStationRepository.findAll(builder);
        List<PollingStationDTO> pollingStationDTOs = StreamSupport.stream(pollingStations.spliterator(), false)
                .map(pollingStationMapper::toDto)
                .collect(Collectors.toList());
        
        // Calculate utilization rates
        calculateUtilizationRates(pollingStationDTOs);
        
        return pollingStationDTOs;
    }

    /**
     * Find polling stations with utilization statistics.
     * This method demonstrates using repository methods with custom queries.
     */
    @Transactional(readOnly = true)
    public List<PollingStationDTO> findPollingStationsWithUtilizationStatistics() {
        // Use the custom repository method to find polling stations with utilization rate
        List<Object[]> results = pollingStationRepository.findPollingStationsWithUtilizationRate();
        
        // Convert the results to DTOs
        return results.stream()
                .map(result -> {
                    Long pollingStationId = (Long) result[0];
                    String pollingStationName = (String) result[1];
                    Integer capacity = (Integer) result[2];
                    Long voteCount = (Long) result[3];
                    Double utilizationRate = (Double) result[4];
                    
                    // Find the polling station by ID
                    return pollingStationRepository.findById(pollingStationId)
                            .map(pollingStation -> {
                                PollingStationDTO dto = pollingStationMapper.toDto(pollingStation);
                                dto.setVoteCount(voteCount.intValue());
                                dto.setUtilizationRate(utilizationRate);
                                return dto;
                            })
                            .orElse(null);
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     * Calculate utilization rates for a list of polling station DTOs.
     * This is a helper method used by other methods in this service.
     */
    private void calculateUtilizationRates(List<PollingStationDTO> pollingStationDTOs) {
        pollingStationDTOs.forEach(this::calculateUtilizationRate);
    }

    /**
     * Calculate utilization rate for a single polling station DTO.
     * This is a helper method used by other methods in this service.
     * 
     * The utilization rate is calculated as the ratio of votes cast to the capacity of the polling station.
     */
    private void calculateUtilizationRate(PollingStationDTO dto) {
        if (dto.getVoteCount() != null && dto.getCapacity() != null && dto.getCapacity() > 0) {
            double utilizationRate = (double) dto.getVoteCount() / dto.getCapacity();
            dto.setUtilizationRate(utilizationRate);
        } else {
            dto.setUtilizationRate(0.0);
        }
    }
}