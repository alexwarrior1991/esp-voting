package com.alejandro.espvoting.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a voter in the electoral system.
 */
@Entity
@Table(name = "voters")
@Audited
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String identificationNumber;

    @Column
    private LocalDate dateOfBirth;

    @Column
    private String sex;

    @Column
    private String email;

    @Column
    private String phoneNumber;

    @Column
    private Boolean isActive = true;

    // Many voters belong to one region (Many-to-One relationship)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    // Many voters belong to one district (Many-to-One relationship)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    // One voter can cast many votes in different elections (One-to-Many relationship)
    @OneToMany(mappedBy = "voter", cascade = CascadeType.ALL)
    private List<Vote> votes = new ArrayList<>();
}
