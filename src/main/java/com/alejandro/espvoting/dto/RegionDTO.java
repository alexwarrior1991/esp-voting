package com.alejandro.espvoting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link com.alejandro.espvoting.model.Region}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionDTO implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Integer population;
    private List<Long> districtIds;
    private Integer voterCount;
    private Integer voteCount;
    private Double participationRate;
}