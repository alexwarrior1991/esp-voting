package com.alejandro.espvoting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for vote filtering request parameters.
 * This DTO encapsulates all the parameters needed for complex filtering of votes,
 * making controller methods cleaner by replacing multiple @RequestParam with a single @RequestBody.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteFilterRequestDTO implements Serializable {
    private Long voterId;
    private Long candidateId;
    private Long electionId;
    private Long pollingStationId;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;
    
    private Boolean isValid;
}