package com.alejandro.espvoting.mapper;

import com.alejandro.espvoting.dto.CandidateDTO;
import com.alejandro.espvoting.model.Candidate;
import com.alejandro.espvoting.model.Election;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for the entity {@link Candidate} and its DTO {@link CandidateDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface CandidateMapper {

    @Mapping(target = "electionIds", expression = "java(getElectionIds(candidate))")
    @Mapping(target = "voteCount", expression = "java(candidate.getVotes() != null ? candidate.getVotes().size() : 0)")
    CandidateDTO toDto(Candidate candidate);

    List<CandidateDTO> toDto(List<Candidate> candidates);

    @Mapping(target = "votes", ignore = true)
    @Mapping(target = "elections", ignore = true)
    Candidate toEntity(CandidateDTO candidateDTO);

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "votes", ignore = true)
    @Mapping(target = "elections", ignore = true)
    void partialUpdate(@MappingTarget Candidate entity, CandidateDTO dto);

    default List<Long> getElectionIds(Candidate candidate) {
        return candidate.getElections() != null
                ? candidate.getElections().stream()
                    .map(Election::getId)
                    .collect(Collectors.toList())
                : null;
    }
}