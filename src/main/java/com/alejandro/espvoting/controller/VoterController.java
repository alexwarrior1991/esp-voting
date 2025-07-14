package com.alejandro.espvoting.controller;

import com.alejandro.espvoting.dto.VoterDTO;
import com.alejandro.espvoting.dto.VoterFilterRequestDTO;
import com.alejandro.espvoting.service.VoterService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing Voter entities.
 */
@RestController
@RequestMapping("/api/voters")
@RequiredArgsConstructor
@Validated
public class VoterController {

    private final VoterService voterService;

    /**
     * GET /api/voters : Get all voters.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of voters in the body.
     */
    @GetMapping
    public ResponseEntity<List<VoterDTO>> getAllVoters() {
        List<VoterDTO> voters = voterService.findAll();
        return ResponseEntity.ok(voters);
    }

    /**
     * GET /api/voters/{id} : Get the voter with the specified ID.
     *
     * @param id the ID of the voter to retrieve.
     * @return the ResponseEntity with status 200 (OK) and the voter in the body, or with status 404 (Not Found).
     */
    @GetMapping("/{id}")
    public ResponseEntity<VoterDTO> getVoter(@PathVariable @Min(1) Long id) {
        return voterService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/voters : Create a new voter.
     *
     * @param voterDTO the voter to create.
     * @return the ResponseEntity with status 201 (Created) and the new voter in the body.
     */
    @PostMapping
    public ResponseEntity<VoterDTO> createVoter(@Valid @RequestBody VoterDTO voterDTO) {
        if (voterDTO.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        VoterDTO result = voterService.save(voterDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * PUT /api/voters/{id} : Update an existing voter.
     *
     * @param id the ID of the voter to update.
     * @param voterDTO the voter to update.
     * @return the ResponseEntity with status 200 (OK) and the updated voter in the body, or with status 404 (Not Found).
     */
    @PutMapping("/{id}")
    public ResponseEntity<VoterDTO> updateVoter(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody VoterDTO voterDTO) {

        if (voterDTO.getId() == null) {
            voterDTO.setId(id);
        } else if (!id.equals(voterDTO.getId())) {
            return ResponseEntity.badRequest().build();
        }

        return voterService.update(id, voterDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/voters/{id} : Delete the voter with the specified ID.
     *
     * @param id the ID of the voter to delete.
     * @return the ResponseEntity with status 204 (NO_CONTENT).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoter(@PathVariable @Min(1) Long id) {
        voterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/voters/search/by-name : Search voters by name.
     *
     * @param name the name to search for.
     * @return the ResponseEntity with status 200 (OK) and the list of voters in the body.
     */
    @GetMapping("/search/by-name")
    public ResponseEntity<List<VoterDTO>> searchVotersByName(@RequestParam String name) {
        List<VoterDTO> voters = voterService.findVotersByName(name);
        return ResponseEntity.ok(voters);
    }

    /**
     * GET /api/voters/search/by-region-age-sex : Search voters by region, age range, and sex.
     *
     * @param regionId the ID of the region to filter by.
     * @param minAge the minimum age to filter by.
     * @param maxAge the maximum age to filter by.
     * @param sex the sex to filter by.
     * @return the ResponseEntity with status 200 (OK) and the list of voters in the body.
     */
    @GetMapping("/search/by-region-age-sex")
    public ResponseEntity<List<VoterDTO>> searchVotersByRegionAgeSex(
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) @Min(0) Integer minAge,
            @RequestParam(required = false) @Min(0) Integer maxAge,
            @RequestParam(required = false) String sex) {

        List<VoterDTO> voters = voterService.findVotersByRegionAgeAndSex(regionId, minAge, maxAge, sex);
        return ResponseEntity.ok(voters);
    }

    /**
     * POST /api/voters/search/complex : Search voters with complex filtering.
     *
     * @param filterRequest the DTO containing all filter parameters.
     * @return the ResponseEntity with status 200 (OK) and the list of voters in the body.
     */
    @PostMapping("/search/complex")
    public ResponseEntity<List<VoterDTO>> searchVotersWithComplexFiltering(
            @Valid @RequestBody VoterFilterRequestDTO filterRequest) {

        List<VoterDTO> voters = voterService.findVotersWithComplexFiltering(
                filterRequest.getNameFilter(), 
                filterRequest.getRegionIds(), 
                filterRequest.getDistrictIds(), 
                filterRequest.getHasVoted(), 
                filterRequest.getRegisteredBefore());
        return ResponseEntity.ok(voters);
    }
}
