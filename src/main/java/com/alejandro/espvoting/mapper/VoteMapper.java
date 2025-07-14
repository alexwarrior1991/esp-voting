package com.alejandro.espvoting.mapper;

import com.alejandro.espvoting.dto.VoteDTO;
import com.alejandro.espvoting.model.Vote;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper for the entity {@link Vote} and its DTO {@link VoteDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface VoteMapper {

    @Mapping(target = "voterId", source = "voter.id")
    @Mapping(target = "voterName", expression = "java(vote.getVoter().getFirstName() + \" \" + vote.getVoter().getLastName())")
    @Mapping(target = "candidateId", source = "candidate.id")
    @Mapping(target = "candidateName", expression = "java(vote.getCandidate().getFirstName() + \" \" + vote.getCandidate().getLastName())")
    @Mapping(target = "candidateParty", source = "candidate.party")
    @Mapping(target = "electionId", source = "election.id")
    @Mapping(target = "electionName", source = "election.name")
    @Mapping(target = "pollingStationId", source = "pollingStation.id")
    @Mapping(target = "pollingStationName", source = "pollingStation.name")
    VoteDTO toDto(Vote vote);

    List<VoteDTO> toDto(List<Vote> votes);

    @Mapping(target = "voter", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "election", ignore = true)
    @Mapping(target = "pollingStation", ignore = true)
    Vote toEntity(VoteDTO voteDTO);

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "voter", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "election", ignore = true)
    @Mapping(target = "pollingStation", ignore = true)
    void partialUpdate(@MappingTarget Vote entity, VoteDTO dto);
}