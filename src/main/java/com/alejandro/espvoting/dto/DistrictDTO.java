package com.alejandro.espvoting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link com.alejandro.espvoting.model.District}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistrictDTO implements Serializable {
    private Long id;
    private String name;
    private String code;
    private Integer population;
    private Long regionId;
    private String regionName;
    private List<Long> pollingStationIds;
    private Integer voterCount;
    private Integer voteCount;
    private Double participationRate;
}