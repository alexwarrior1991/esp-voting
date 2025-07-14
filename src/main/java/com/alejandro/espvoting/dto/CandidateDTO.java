package com.alejandro.espvoting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link com.alejandro.espvoting.model.Candidate}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDTO implements Serializable {
    private Long id;
    private String firstName;
    private String lastName;
    private String party;
    private String platform;
    private String biography;
    private String imageUrl;
    private List<Long> electionIds;
    private Integer voteCount;
}