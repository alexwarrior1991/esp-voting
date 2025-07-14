package com.alejandro.espvoting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for voter filtering request parameters.
 * This DTO encapsulates all the parameters needed for complex filtering of voters,
 * making controller methods cleaner by replacing multiple @RequestParam with a single @RequestBody.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoterFilterRequestDTO implements Serializable {
    private String nameFilter;
    private List<Long> regionIds;
    private List<Long> districtIds;
    private Boolean hasVoted;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate registeredBefore;
}