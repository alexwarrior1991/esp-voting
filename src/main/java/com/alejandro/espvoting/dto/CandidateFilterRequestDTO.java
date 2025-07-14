package com.alejandro.espvoting.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for candidate filtering request parameters.
 * This DTO encapsulates all the parameters needed for complex filtering of candidates,
 * making controller methods cleaner by replacing multiple @RequestParam with a single @RequestBody.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateFilterRequestDTO implements Serializable {
    private String nameFilter;
    private List<String> parties;
    private List<Long> electionIds;
    
    @Min(0)
    private Integer minVotes;
}