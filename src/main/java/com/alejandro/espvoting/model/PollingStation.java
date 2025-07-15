package com.alejandro.espvoting.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a polling station where voters cast their votes.
 */
@Entity
@Table(name = "polling_stations")
@Audited
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollingStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String address;

    @Column
    private Integer capacity;

    @Column
    private Boolean isActive = true;

    // Many polling stations can be in many districts (Many-to-Many relationship)
    @ManyToMany(mappedBy = "pollingStations")
    private List<District> districts = new ArrayList<>();

    // One polling station can have many votes cast (One-to-Many relationship)
    @OneToMany(mappedBy = "pollingStation", cascade = CascadeType.ALL)
    private List<Vote> votes = new ArrayList<>();
}
