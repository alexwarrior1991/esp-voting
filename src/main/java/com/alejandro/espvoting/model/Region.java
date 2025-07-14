package com.alejandro.espvoting.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a geographical region in the voting system.
 * This can be used for analyzing voting patterns by region.
 */
@Entity
@Table(name = "regions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Region {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column
    private String description;
    
    @Column
    private Integer population;
    
    // One region can have many districts (One-to-Many relationship)
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<District> districts = new ArrayList<>();
    
    // One region can have many voters (One-to-Many relationship)
    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    private List<Voter> voters = new ArrayList<>();
}