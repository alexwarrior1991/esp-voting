package com.alejandro.espvoting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for vote statistics by candidate and election.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteStatisticsDTO {
    
    private Long candidateId;
    private String candidateFirstName;
    private String candidateLastName;
    private String candidateParty;
    private Long electionId;
    private String electionName;
    private Long voteCount;
    private Double votePercentage;
}