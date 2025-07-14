package com.alejandro.espvoting.repository;

import com.alejandro.espvoting.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Region entity with QueryDSL support.
 */
@Repository
public interface RegionRepository extends JpaRepository<Region, Long>, QuerydslPredicateExecutor<Region> {
    
    /**
     * Find region by name.
     */
    Optional<Region> findByName(String name);
    
    /**
     * Find regions with population greater than the specified value.
     */
    List<Region> findByPopulationGreaterThan(Integer population);
    
    /**
     * Find regions with voter count statistics.
     */
    @Query("SELECT r, COUNT(v) FROM Region r LEFT JOIN r.voters v GROUP BY r")
    List<Object[]> findRegionsWithVoterCount();
    
    /**
     * Find regions with district count statistics.
     */
    @Query("SELECT r, COUNT(d) FROM Region r LEFT JOIN r.districts d GROUP BY r")
    List<Object[]> findRegionsWithDistrictCount();
    
    /**
     * Find regions with vote count statistics.
     * This is a more complex query that demonstrates nested joins.
     */
    @Query("SELECT r.id, r.name, COUNT(v) FROM Region r LEFT JOIN r.voters vr LEFT JOIN vr.votes v GROUP BY r.id, r.name")
    List<Object[]> findRegionsWithVoteCount();
}