package com.alejandro.espvoting.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an election event.
 */
@Entity
@Table(name = "elections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Election {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    private LocalDate electionDate;
    
    @Column
    private LocalDate registrationStartDate;
    
    @Column
    private LocalDate registrationEndDate;
    
    @Column
    private Boolean isActive = true;
    
    @Column
    private String electionType; // presidential, parliamentary, local, etc.
    
    // Many elections can have many candidates (Many-to-Many relationship)
    @ManyToMany(mappedBy = "elections")
    private List<Candidate> candidates = new ArrayList<>();
    
    // One election can have many votes (One-to-Many relationship)
    @OneToMany(mappedBy = "election", cascade = CascadeType.ALL)
    private List<Vote> votes = new ArrayList<>();
}