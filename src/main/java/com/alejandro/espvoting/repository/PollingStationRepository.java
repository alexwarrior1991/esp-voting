package com.alejandro.espvoting.repository;

import com.alejandro.espvoting.model.PollingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the PollingStation entity with QueryDSL support.
 */
@Repository
public interface PollingStationRepository extends JpaRepository<PollingStation, Long>, QuerydslPredicateExecutor<PollingStation> {
    
    /**
     * Find polling station by name.
     */
    Optional<PollingStation> findByName(String name);
    
    /**
     * Find active polling stations.
     */
    List<PollingStation> findByIsActiveTrue();
    
    /**
     * Find polling stations by district ID.
     */
    @Query("SELECT ps FROM PollingStation ps JOIN ps.districts d WHERE d.id = ?1")
    List<PollingStation> findByDistrictId(Long districtId);
    
    /**
     * Find polling stations with capacity greater than the specified value.
     */
    List<PollingStation> findByCapacityGreaterThan(Integer capacity);
    
    /**
     * Find polling stations with vote count statistics.
     */
    @Query("SELECT ps, COUNT(v) FROM PollingStation ps LEFT JOIN ps.votes v GROUP BY ps")
    List<Object[]> findPollingStationsWithVoteCount();
    
    /**
     * Find polling stations with district count statistics.
     */
    @Query("SELECT ps, COUNT(d) FROM PollingStation ps LEFT JOIN ps.districts d GROUP BY ps")
    List<Object[]> findPollingStationsWithDistrictCount();
    
    /**
     * Find polling stations with utilization rate.
     * This is a more complex query that calculates the ratio of votes to capacity.
     */
    @Query("SELECT ps.id, ps.name, ps.capacity, COUNT(v), " +
           "CASE WHEN ps.capacity > 0 THEN (COUNT(v) * 1.0 / ps.capacity) ELSE 0 END as utilizationRate " +
           "FROM PollingStation ps LEFT JOIN ps.votes v " +
           "GROUP BY ps.id, ps.name, ps.capacity")
    List<Object[]> findPollingStationsWithUtilizationRate();
}