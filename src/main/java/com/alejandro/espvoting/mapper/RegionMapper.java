package com.alejandro.espvoting.mapper;

import com.alejandro.espvoting.dto.RegionDTO;
import com.alejandro.espvoting.model.District;
import com.alejandro.espvoting.model.Region;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for the entity {@link Region} and its DTO {@link RegionDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface RegionMapper {

    @Mapping(target = "districtIds", expression = "java(getDistrictIds(region))")
    @Mapping(target = "voterCount", expression = "java(region.getVoters() != null ? region.getVoters().size() : 0)")
    @Mapping(target = "voteCount", ignore = true) // This will be calculated in the service
    @Mapping(target = "participationRate", ignore = true) // This will be calculated in the service
    RegionDTO toDto(Region region);

    List<RegionDTO> toDto(List<Region> regions);

    @Mapping(target = "districts", ignore = true)
    @Mapping(target = "voters", ignore = true)
    Region toEntity(RegionDTO regionDTO);

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "districts", ignore = true)
    @Mapping(target = "voters", ignore = true)
    void partialUpdate(@MappingTarget Region entity, RegionDTO dto);

    default List<Long> getDistrictIds(Region region) {
        return region.getDistricts() != null
                ? region.getDistricts().stream()
                    .map(District::getId)
                    .collect(Collectors.toList())
                : null;
    }
}