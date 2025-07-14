package com.alejandro.espvoting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for {@link com.alejandro.espvoting.model.Election}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElectionDTO implements Serializable {
    private Long id;
    private String name;
    private String description;
    private LocalDate electionDate;
    private LocalDate registrationStartDate;
    private LocalDate registrationEndDate;
    private Boolean isActive;
    private String electionType;
    private List<Long> candidateIds;
    private Integer totalVotes;
    private Double participationRate;
}