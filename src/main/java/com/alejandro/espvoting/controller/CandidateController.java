package com.alejandro.espvoting.controller;

import com.alejandro.espvoting.dto.CandidateDTO;
import com.alejandro.espvoting.dto.CandidateFilterRequestDTO;
import com.alejandro.espvoting.service.CandidateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Candidate entities.
 */
@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@Validated
public class CandidateController {

    private final CandidateService candidateService;

    /**
     * GET /api/candidates : Get all candidates.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of candidates in the body.
     */
    @GetMapping
    public ResponseEntity<List<CandidateDTO>> getAllCandidates() {
        List<CandidateDTO> candidates = candidateService.findAll();
        return ResponseEntity.ok(candidates);
    }

    /**
     * GET /api/candidates/{id} : Get the candidate with the specified ID.
     *
     * @param id the ID of the candidate to retrieve.
     * @return the ResponseEntity with status 200 (OK) and the candidate in the body, or with status 404 (Not Found).
     */
    @GetMapping("/{id}")
    public ResponseEntity<CandidateDTO> getCandidate(@PathVariable @Min(1) Long id) {
        return candidateService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/candidates : Create a new candidate.
     *
     * @param candidateDTO the candidate to create.
     * @return the ResponseEntity with status 201 (Created) and the new candidate in the body.
     */
    @PostMapping
    public ResponseEntity<CandidateDTO> createCandidate(@Valid @RequestBody CandidateDTO candidateDTO) {
        if (candidateDTO.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        CandidateDTO result = candidateService.save(candidateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * PUT /api/candidates/{id} : Update an existing candidate.
     *
     * @param id the ID of the candidate to update.
     * @param candidateDTO the candidate to update.
     * @return the ResponseEntity with status 200 (OK) and the updated candidate in the body, or with status 404 (Not Found).
     */
    @PutMapping("/{id}")
    public ResponseEntity<CandidateDTO> updateCandidate(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody CandidateDTO candidateDTO) {

        if (candidateDTO.getId() == null) {
            candidateDTO.setId(id);
        } else if (!id.equals(candidateDTO.getId())) {
            return ResponseEntity.badRequest().build();
        }

        return candidateService.update(id, candidateDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/candidates/{id} : Delete the candidate with the specified ID.
     *
     * @param id the ID of the candidate to delete.
     * @return the ResponseEntity with status 204 (NO_CONTENT).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable @Min(1) Long id) {
        candidateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/candidates/by-party/{party} : Get candidates by party.
     *
     * @param party the party to filter by.
     * @return the ResponseEntity with status 200 (OK) and the list of candidates in the body.
     */
    @GetMapping("/by-party/{party}")
    public ResponseEntity<List<CandidateDTO>> getCandidatesByParty(@PathVariable String party) {
        List<CandidateDTO> candidates = candidateService.findByParty(party);
        return ResponseEntity.ok(candidates);
    }

    /**
     * GET /api/candidates/by-election/{electionId} : Get candidates by election ID.
     *
     * @param electionId the ID of the election to filter by.
     * @return the ResponseEntity with status 200 (OK) and the list of candidates in the body.
     */
    @GetMapping("/by-election/{electionId}")
    public ResponseEntity<List<CandidateDTO>> getCandidatesByElection(@PathVariable @Min(1) Long electionId) {
        List<CandidateDTO> candidates = candidateService.findByElectionId(electionId);
        return ResponseEntity.ok(candidates);
    }

    /**
     * POST /api/candidates/search/complex : Search candidates with complex filtering.
     *
     * @param filterRequest the DTO containing all filter parameters.
     * @return the ResponseEntity with status 200 (OK) and the list of candidates in the body.
     */
    @PostMapping("/search/complex")
    public ResponseEntity<List<CandidateDTO>> searchCandidatesWithComplexFiltering(
            @Valid @RequestBody CandidateFilterRequestDTO filterRequest) {

        List<CandidateDTO> candidates = candidateService.findCandidatesWithComplexFiltering(
                filterRequest.getNameFilter(), 
                filterRequest.getParties(), 
                filterRequest.getElectionIds(), 
                filterRequest.getMinVotes());
        return ResponseEntity.ok(candidates);
    }

    /**
     * GET /api/candidates/top-by-votes : Get top candidates by vote count.
     *
     * @param limit the maximum number of candidates to return.
     * @return the ResponseEntity with status 200 (OK) and the list of candidates in the body.
     */
    @GetMapping("/top-by-votes")
    public ResponseEntity<List<CandidateDTO>> getTopCandidatesByVoteCount(
            @RequestParam(defaultValue = "10") @Min(1) int limit) {

        List<CandidateDTO> candidates = candidateService.findTopCandidatesByVoteCount(limit);
        return ResponseEntity.ok(candidates);
    }
}
