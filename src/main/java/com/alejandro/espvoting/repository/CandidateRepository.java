package com.alejandro.espvoting.repository;

import com.alejandro.espvoting.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the Candidate entity with QueryDSL support.
 */
@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long>, QuerydslPredicateExecutor<Candidate> {
    
    /**
     * Find candidates by party.
     */
    List<Candidate> findByParty(String party);
    
    /**
     * Find candidates participating in a specific election.
     */
    @Query("SELECT c FROM Candidate c JOIN c.elections e WHERE e.id = ?1")
    List<Candidate> findByElectionId(Long electionId);
    
    /**
     * Find candidates with vote count greater than the specified value.
     * This is a custom query that demonstrates a join operation.
     */
    @Query("SELECT c, COUNT(v) as voteCount FROM Candidate c JOIN c.votes v GROUP BY c HAVING COUNT(v) > ?1")
    List<Object[]> findCandidatesWithVoteCountGreaterThan(Long voteCount);
}