package com.alejandro.espvoting.controller;

import com.alejandro.espvoting.dto.VoteStatisticsDTO;
import com.alejandro.espvoting.service.VoteStatisticsService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing vote statistics.
 */
@RestController
@RequestMapping("/api/vote-statistics")
@RequiredArgsConstructor
@Validated
public class VoteStatisticsController {

    private final VoteStatisticsService voteStatisticsService;

    /**
     * GET /api/vote-statistics/by-candidate-and-election/{electionId} : Get vote statistics by candidate and election.
     *
     * @param electionId the ID of the election to get statistics for.
     * @return the ResponseEntity with status 200 (OK) and the list of vote statistics in the body.
     */
    @GetMapping("/by-candidate-and-election/{electionId}")
    public ResponseEntity<List<VoteStatisticsDTO>> getVoteStatisticsByCandidateAndElection(
            @PathVariable @Min(1) Long electionId) {
        
        List<VoteStatisticsDTO> statistics = voteStatisticsService.getVoteStatisticsByCandidateAndElection(electionId);
        return ResponseEntity.ok(statistics);
    }
}