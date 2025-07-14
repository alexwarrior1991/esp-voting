package com.alejandro.espvoting.mapper;

import com.alejandro.espvoting.dto.ElectionDTO;
import com.alejandro.espvoting.model.Candidate;
import com.alejandro.espvoting.model.Election;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for the entity {@link Election} and its DTO {@link ElectionDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface ElectionMapper {

    @Mapping(target = "candidateIds", expression = "java(getCandidateIds(election))")
    @Mapping(target = "totalVotes", expression = "java(election.getVotes() != null ? election.getVotes().size() : 0)")
    @Mapping(target = "participationRate", ignore = true) // This will be calculated in the service
    ElectionDTO toDto(Election election);

    List<ElectionDTO> toDto(List<Election> elections);

    @Mapping(target = "votes", ignore = true)
    @Mapping(target = "candidates", ignore = true)
    Election toEntity(ElectionDTO electionDTO);

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "votes", ignore = true)
    @Mapping(target = "candidates", ignore = true)
    void partialUpdate(@MappingTarget Election entity, ElectionDTO dto);

    default List<Long> getCandidateIds(Election election) {
        return election.getCandidates() != null
                ? election.getCandidates().stream()
                    .map(Candidate::getId)
                    .collect(Collectors.toList())
                : null;
    }
}