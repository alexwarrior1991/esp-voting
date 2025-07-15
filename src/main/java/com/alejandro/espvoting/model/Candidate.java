package com.alejandro.espvoting.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a candidate in an election.
 */
@Entity
@Table(name = "candidates")
@Audited
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column
    private String party;

    @Column
    private String platform;

    @Column
    private String biography;

    @Column
    private String imageUrl;

    // Many candidates can participate in many elections (Many-to-Many relationship)
    @ManyToMany
    @JoinTable(
        name = "election_candidate",
        joinColumns = @JoinColumn(name = "candidate_id"),
        inverseJoinColumns = @JoinColumn(name = "election_id")
    )
    private List<Election> elections = new ArrayList<>();

    // One candidate can receive many votes (One-to-Many relationship)
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<Vote> votes = new ArrayList<>();
}
