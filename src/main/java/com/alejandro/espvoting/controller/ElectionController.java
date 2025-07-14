package com.alejandro.espvoting.controller;

import com.alejandro.espvoting.dto.ElectionDTO;
import com.alejandro.espvoting.dto.ElectionFilterRequestDTO;
import com.alejandro.espvoting.service.ElectionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing Election entities.
 */
@RestController
@RequestMapping("/api/elections")
@RequiredArgsConstructor
@Validated
public class ElectionController {

    private final ElectionService electionService;

    /**
     * GET /api/elections : Get all elections.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of elections in the body.
     */
    @GetMapping
    public ResponseEntity<List<ElectionDTO>> getAllElections() {
        List<ElectionDTO> elections = electionService.findAll();
        return ResponseEntity.ok(elections);
    }

    /**
     * GET /api/elections/{id} : Get the election with the specified ID.
     *
     * @param id the ID of the election to retrieve.
     * @return the ResponseEntity with status 200 (OK) and the election in the body, or with status 404 (Not Found).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ElectionDTO> getElection(@PathVariable @Min(1) Long id) {
        return electionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/elections : Create a new election.
     *
     * @param electionDTO the election to create.
     * @return the ResponseEntity with status 201 (Created) and the new election in the body.
     */
    @PostMapping
    public ResponseEntity<ElectionDTO> createElection(@Valid @RequestBody ElectionDTO electionDTO) {
        if (electionDTO.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        ElectionDTO result = electionService.save(electionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * PUT /api/elections/{id} : Update an existing election.
     *
     * @param id the ID of the election to update.
     * @param electionDTO the election to update.
     * @return the ResponseEntity with status 200 (OK) and the updated election in the body, or with status 404 (Not Found).
     */
    @PutMapping("/{id}")
    public ResponseEntity<ElectionDTO> updateElection(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody ElectionDTO electionDTO) {

        if (electionDTO.getId() == null) {
            electionDTO.setId(id);
        } else if (!id.equals(electionDTO.getId())) {
            return ResponseEntity.badRequest().build();
        }

        return electionService.update(id, electionDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/elections/{id} : Delete the election with the specified ID.
     *
     * @param id the ID of the election to delete.
     * @return the ResponseEntity with status 204 (NO_CONTENT).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteElection(@PathVariable @Min(1) Long id) {
        electionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/elections/active : Get all active elections.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of active elections in the body.
     */
    @GetMapping("/active")
    public ResponseEntity<List<ElectionDTO>> getActiveElections() {
        List<ElectionDTO> elections = electionService.findActiveElections();
        return ResponseEntity.ok(elections);
    }

    /**
     * GET /api/elections/by-type/{type} : Get elections by type.
     *
     * @param type the type of elections to retrieve.
     * @return the ResponseEntity with status 200 (OK) and the list of elections in the body.
     */
    @GetMapping("/by-type/{type}")
    public ResponseEntity<List<ElectionDTO>> getElectionsByType(@PathVariable String type) {
        List<ElectionDTO> elections = electionService.findByElectionType(type);
        return ResponseEntity.ok(elections);
    }

    /**
     * POST /api/elections/search/complex : Search elections with complex filtering.
     *
     * @param filterRequest the DTO containing all filter parameters.
     * @return the ResponseEntity with status 200 (OK) and the list of elections in the body.
     */
    @PostMapping("/search/complex")
    public ResponseEntity<List<ElectionDTO>> searchElectionsWithComplexFiltering(
            @RequestBody ElectionFilterRequestDTO filterRequest) {

        List<ElectionDTO> elections = electionService.findElectionsWithComplexFiltering(
                filterRequest.getNameFilter(), 
                filterRequest.getElectionTypes(), 
                filterRequest.getStartDate(), 
                filterRequest.getEndDate(), 
                filterRequest.getIsActive());
        return ResponseEntity.ok(elections);
    }

    /**
     * GET /api/elections/statistics : Get elections with vote statistics.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of elections with vote statistics in the body.
     */
    @GetMapping("/statistics")
    public ResponseEntity<List<ElectionDTO>> getElectionsWithVoteStatistics() {
        List<ElectionDTO> elections = electionService.findElectionsWithVoteStatistics();
        return ResponseEntity.ok(elections);
    }
}
