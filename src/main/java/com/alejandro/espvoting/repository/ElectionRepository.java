package com.alejandro.espvoting.repository;

import com.alejandro.espvoting.model.Election;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for the Election entity with QueryDSL support.
 */
@Repository
public interface ElectionRepository extends JpaRepository<Election, Long>, QuerydslPredicateExecutor<Election> {
    
    /**
     * Find active elections.
     */
    List<Election> findByIsActiveTrue();
    
    /**
     * Find elections by type.
     */
    List<Election> findByElectionType(String electionType);
    
    /**
     * Find elections between two dates.
     */
    List<Election> findByElectionDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find elections with a specific candidate.
     */
    @Query("SELECT e FROM Election e JOIN e.candidates c WHERE c.id = ?1")
    List<Election> findByCandidateId(Long candidateId);
    
    /**
     * Find elections with vote count statistics.
     * This is a custom query that demonstrates aggregation.
     */
    @Query("SELECT e, COUNT(v) as voteCount FROM Election e LEFT JOIN e.votes v GROUP BY e")
    List<Object[]> findElectionsWithVoteCount();
}