package com.alejandro.espvoting.mapper;

import com.alejandro.espvoting.dto.DistrictDTO;
import com.alejandro.espvoting.model.District;
import com.alejandro.espvoting.model.PollingStation;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for the entity {@link District} and its DTO {@link DistrictDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface DistrictMapper {

    @Mapping(target = "regionId", source = "region.id")
    @Mapping(target = "regionName", source = "region.name")
    @Mapping(target = "pollingStationIds", expression = "java(getPollingStationIds(district))")
    @Mapping(target = "voterCount", expression = "java(district.getVoters() != null ? district.getVoters().size() : 0)")
    @Mapping(target = "voteCount", ignore = true) // This will be calculated in the service
    @Mapping(target = "participationRate", ignore = true) // This will be calculated in the service
    DistrictDTO toDto(District district);

    List<DistrictDTO> toDto(List<District> districts);

    @Mapping(target = "region", ignore = true)
    @Mapping(target = "voters", ignore = true)
    @Mapping(target = "pollingStations", ignore = true)
    District toEntity(DistrictDTO districtDTO);

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "voters", ignore = true)
    @Mapping(target = "pollingStations", ignore = true)
    void partialUpdate(@MappingTarget District entity, DistrictDTO dto);

    default List<Long> getPollingStationIds(District district) {
        return district.getPollingStations() != null
                ? district.getPollingStations().stream()
                    .map(PollingStation::getId)
                    .collect(Collectors.toList())
                : null;
    }
}