package com.alejandro.espvoting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for election filtering request parameters.
 * This DTO encapsulates all the parameters needed for complex filtering of elections,
 * making controller methods cleaner by replacing multiple @RequestParam with a single @RequestBody.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElectionFilterRequestDTO implements Serializable {
    private String nameFilter;
    private List<String> electionTypes;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    
    private Boolean isActive;
}