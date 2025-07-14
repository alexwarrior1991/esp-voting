package com.alejandro.espvoting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link com.alejandro.espvoting.model.PollingStation}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollingStationDTO implements Serializable {
    private Long id;
    private String name;
    private String address;
    private Integer capacity;
    private Boolean isActive;
    private List<Long> districtIds;
    private List<String> districtNames;
    private Integer voteCount;
    private Double utilizationRate;
}