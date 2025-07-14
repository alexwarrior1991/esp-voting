package com.alejandro.espvoting.repository;

import com.alejandro.espvoting.model.Voter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Voter entity with QueryDSL support.
 */
@Repository
public interface VoterRepository extends JpaRepository<Voter, Long>, QuerydslPredicateExecutor<Voter> {
    // Spring Data JPA will automatically implement basic CRUD operations
    // QuerydslPredicateExecutor adds support for QueryDSL predicates
}