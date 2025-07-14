package com.alejandro.espvoting.mapper;

import com.alejandro.espvoting.dto.VoterDTO;
import com.alejandro.espvoting.model.Voter;
import org.mapstruct.*;

import java.util.List;

/**
 * Mapper for the entity {@link Voter} and its DTO {@link VoterDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface VoterMapper {

    @Mapping(target = "regionId", source = "region.id")
    @Mapping(target = "regionName", source = "region.name")
    @Mapping(target = "districtId", source = "district.id")
    @Mapping(target = "districtName", source = "district.name")
    VoterDTO toDto(Voter voter);

    List<VoterDTO> toDto(List<Voter> voters);

    @Mapping(target = "votes", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "district", ignore = true)
    Voter toEntity(VoterDTO voterDTO);

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "votes", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "district", ignore = true)
    void partialUpdate(@MappingTarget Voter entity, VoterDTO dto);
}