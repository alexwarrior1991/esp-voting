package com.alejandro.espvoting.repository;

import com.alejandro.espvoting.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for the Vote entity with QueryDSL support.
 */
@Repository
public interface VoteRepository extends JpaRepository<Vote, Long>, QuerydslPredicateExecutor<Vote> {
    
    /**
     * Find votes by voter ID.
     */
    List<Vote> findByVoterId(Long voterId);
    
    /**
     * Find votes by candidate ID.
     */
    List<Vote> findByCandidateId(Long candidateId);
    
    /**
     * Find votes by election ID.
     */
    List<Vote> findByElectionId(Long electionId);
    
    /**
     * Find votes by polling station ID.
     */
    List<Vote> findByPollingStationId(Long pollingStationId);
    
    /**
     * Find votes cast after a specific timestamp.
     */
    List<Vote> findByTimestampAfter(LocalDateTime timestamp);
    
    /**
     * Count votes by region.
     */
    @Query("SELECT v.voter.region.id, v.voter.region.name, COUNT(v) FROM Vote v GROUP BY v.voter.region.id, v.voter.region.name")
    List<Object[]> countVotesByRegion();
    
    /**
     * Count votes by district.
     */
    @Query("SELECT v.voter.district.id, v.voter.district.name, COUNT(v) FROM Vote v GROUP BY v.voter.district.id, v.voter.district.name")
    List<Object[]> countVotesByDistrict();
    
    /**
     * Count votes by candidate in a specific election.
     */
    @Query("SELECT v.candidate.id, v.candidate.firstName, v.candidate.lastName, COUNT(v) FROM Vote v WHERE v.election.id = ?1 GROUP BY v.candidate.id, v.candidate.firstName, v.candidate.lastName")
    List<Object[]> countVotesByCandidateInElection(Long electionId);
}