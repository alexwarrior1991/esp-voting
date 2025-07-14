package com.alejandro.espvoting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.alejandro.espvoting.model.Vote}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteDTO implements Serializable {
    private Long id;
    private LocalDateTime timestamp;
    private Boolean isValid;
    private Long voterId;
    private String voterName;
    private Long candidateId;
    private String candidateName;
    private String candidateParty;
    private Long electionId;
    private String electionName;
    private Long pollingStationId;
    private String pollingStationName;
}