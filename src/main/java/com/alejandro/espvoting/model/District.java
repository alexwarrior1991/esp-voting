package com.alejandro.espvoting.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an electoral district in the voting system.
 * Districts are subdivisions of regions.
 */
@Entity
@Table(name = "districts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class District {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column
    private String code;
    
    @Column
    private Integer population;
    
    // Many districts belong to one region (Many-to-One relationship)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
    
    // One district can have many voters (One-to-Many relationship)
    @OneToMany(mappedBy = "district", cascade = CascadeType.ALL)
    private List<Voter> voters = new ArrayList<>();
    
    // Many districts can have many polling stations (Many-to-Many relationship)
    @ManyToMany
    @JoinTable(
        name = "district_polling_station",
        joinColumns = @JoinColumn(name = "district_id"),
        inverseJoinColumns = @JoinColumn(name = "polling_station_id")
    )
    private List<PollingStation> pollingStations = new ArrayList<>();
}