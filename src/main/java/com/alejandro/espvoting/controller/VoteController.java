package com.alejandro.espvoting.controller;

import com.alejandro.espvoting.dto.VoteDTO;
import com.alejandro.espvoting.dto.VoteFilterRequestDTO;
import com.alejandro.espvoting.service.VoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing Vote entities.
 */
@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
@Validated
public class VoteController {

    private final VoteService voteService;

    /**
     * GET /api/votes : Get all votes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of votes in the body.
     */
    @GetMapping
    public ResponseEntity<List<VoteDTO>> getAllVotes() {
        List<VoteDTO> votes = voteService.findAll();
        return ResponseEntity.ok(votes);
    }

    /**
     * GET /api/votes/{id} : Get the vote with the specified ID.
     *
     * @param id the ID of the vote to retrieve.
     * @return the ResponseEntity with status 200 (OK) and the vote in the body, or with status 404 (Not Found).
     */
    @GetMapping("/{id}")
    public ResponseEntity<VoteDTO> getVote(@PathVariable @Min(1) Long id) {
        return voteService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/votes : Cast a new vote.
     *
     * @param voteDTO the vote to cast.
     * @return the ResponseEntity with status 201 (Created) and the new vote in the body.
     */
    @PostMapping
    public ResponseEntity<VoteDTO> castVote(@Valid @RequestBody VoteDTO voteDTO) {
        if (voteDTO.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        VoteDTO result = voteService.castVote(voteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * PUT /api/votes/{id} : Update an existing vote.
     * Only the isValid flag can be updated.
     *
     * @param id the ID of the vote to update.
     * @param voteDTO the vote to update.
     * @return the ResponseEntity with status 200 (OK) and the updated vote in the body, or with status 404 (Not Found).
     */
    @PutMapping("/{id}")
    public ResponseEntity<VoteDTO> updateVote(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody VoteDTO voteDTO) {

        if (voteDTO.getId() == null) {
            voteDTO.setId(id);
        } else if (!id.equals(voteDTO.getId())) {
            return ResponseEntity.badRequest().build();
        }

        return voteService.update(id, voteDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/votes/{id} : Delete the vote with the specified ID.
     *
     * @param id the ID of the vote to delete.
     * @return the ResponseEntity with status 204 (NO_CONTENT).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVote(@PathVariable @Min(1) Long id) {
        voteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/votes/by-voter/{voterId} : Get votes by voter ID.
     *
     * @param voterId the ID of the voter to filter by.
     * @return the ResponseEntity with status 200 (OK) and the list of votes in the body.
     */
    @GetMapping("/by-voter/{voterId}")
    public ResponseEntity<List<VoteDTO>> getVotesByVoter(@PathVariable @Min(1) Long voterId) {
        List<VoteDTO> votes = voteService.findByVoterId(voterId);
        return ResponseEntity.ok(votes);
    }

    /**
     * GET /api/votes/by-candidate/{candidateId} : Get votes by candidate ID.
     *
     * @param candidateId the ID of the candidate to filter by.
     * @return the ResponseEntity with status 200 (OK) and the list of votes in the body.
     */
    @GetMapping("/by-candidate/{candidateId}")
    public ResponseEntity<List<VoteDTO>> getVotesByCandidate(@PathVariable @Min(1) Long candidateId) {
        List<VoteDTO> votes = voteService.findByCandidateId(candidateId);
        return ResponseEntity.ok(votes);
    }

    /**
     * GET /api/votes/by-election/{electionId} : Get votes by election ID.
     *
     * @param electionId the ID of the election to filter by.
     * @return the ResponseEntity with status 200 (OK) and the list of votes in the body.
     */
    @GetMapping("/by-election/{electionId}")
    public ResponseEntity<List<VoteDTO>> getVotesByElection(@PathVariable @Min(1) Long electionId) {
        List<VoteDTO> votes = voteService.findByElectionId(electionId);
        return ResponseEntity.ok(votes);
    }

    /**
     * GET /api/votes/by-polling-station/{pollingStationId} : Get votes by polling station ID.
     *
     * @param pollingStationId the ID of the polling station to filter by.
     * @return the ResponseEntity with status 200 (OK) and the list of votes in the body.
     */
    @GetMapping("/by-polling-station/{pollingStationId}")
    public ResponseEntity<List<VoteDTO>> getVotesByPollingStation(@PathVariable @Min(1) Long pollingStationId) {
        List<VoteDTO> votes = voteService.findByPollingStationId(pollingStationId);
        return ResponseEntity.ok(votes);
    }

    /**
     * POST /api/votes/search/complex : Search votes with complex filtering.
     *
     * @param filterRequest the DTO containing all filter parameters.
     * @return the ResponseEntity with status 200 (OK) and the list of votes in the body.
     */
    @PostMapping("/search/complex")
    public ResponseEntity<List<VoteDTO>> searchVotesWithComplexFiltering(
            @RequestBody VoteFilterRequestDTO filterRequest) {

        List<VoteDTO> votes = voteService.findVotesWithComplexFiltering(
                filterRequest.getVoterId(), 
                filterRequest.getCandidateId(), 
                filterRequest.getElectionId(), 
                filterRequest.getPollingStationId(), 
                filterRequest.getStartTime(), 
                filterRequest.getEndTime(), 
                filterRequest.getIsValid());
        return ResponseEntity.ok(votes);
    }

    /**
     * GET /api/votes/statistics/by-region : Get vote counts by region.
     *
     * @return the ResponseEntity with status 200 (OK) and the map of region names to vote counts in the body.
     */
    @GetMapping("/statistics/by-region")
    public ResponseEntity<Map<String, Integer>> getVoteCountsByRegion() {
        Map<String, Integer> voteCountsByRegion = voteService.getVoteCountsByRegion();
        return ResponseEntity.ok(voteCountsByRegion);
    }

    /**
     * GET /api/votes/statistics/by-district : Get vote counts by district.
     *
     * @return the ResponseEntity with status 200 (OK) and the map of district names to vote counts in the body.
     */
    @GetMapping("/statistics/by-district")
    public ResponseEntity<Map<String, Integer>> getVoteCountsByDistrict() {
        Map<String, Integer> voteCountsByDistrict = voteService.getVoteCountsByDistrict();
        return ResponseEntity.ok(voteCountsByDistrict);
    }

    /**
     * GET /api/votes/statistics/by-candidate-in-election/{electionId} : Get vote counts by candidate in a specific election.
     *
     * @param electionId the ID of the election to filter by.
     * @return the ResponseEntity with status 200 (OK) and the map of candidate names to vote counts in the body.
     */
    @GetMapping("/statistics/by-candidate-in-election/{electionId}")
    public ResponseEntity<Map<String, Integer>> getVoteCountsByCandidateInElection(@PathVariable @Min(1) Long electionId) {
        Map<String, Integer> voteCountsByCandidate = voteService.getVoteCountsByCandidateInElection(electionId);
        return ResponseEntity.ok(voteCountsByCandidate);
    }
}
