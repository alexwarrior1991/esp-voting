package com.alejandro.espvoting.repository;

import com.alejandro.espvoting.model.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the District entity with QueryDSL support.
 */
@Repository
public interface DistrictRepository extends JpaRepository<District, Long>, QuerydslPredicateExecutor<District> {
    
    /**
     * Find district by name.
     */
    Optional<District> findByName(String name);
    
    /**
     * Find district by code.
     */
    Optional<District> findByCode(String code);
    
    /**
     * Find districts by region ID.
     */
    List<District> findByRegionId(Long regionId);
    
    /**
     * Find districts with population greater than the specified value.
     */
    List<District> findByPopulationGreaterThan(Integer population);
    
    /**
     * Find districts with voter count statistics.
     */
    @Query("SELECT d, COUNT(v) FROM District d LEFT JOIN d.voters v GROUP BY d")
    List<Object[]> findDistrictsWithVoterCount();
    
    /**
     * Find districts with polling station count statistics.
     */
    @Query("SELECT d, COUNT(ps) FROM District d LEFT JOIN d.pollingStations ps GROUP BY d")
    List<Object[]> findDistrictsWithPollingStationCount();
    
    /**
     * Find districts with vote count statistics.
     * This is a more complex query that demonstrates nested joins.
     */
    @Query("SELECT d.id, d.name, COUNT(v) FROM District d LEFT JOIN d.voters vr LEFT JOIN vr.votes v GROUP BY d.id, d.name")
    List<Object[]> findDistrictsWithVoteCount();
}