package com.alejandro.espvoting.mapper;

import com.alejandro.espvoting.dto.PollingStationDTO;
import com.alejandro.espvoting.model.District;
import com.alejandro.espvoting.model.PollingStation;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for the entity {@link PollingStation} and its DTO {@link PollingStationDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface PollingStationMapper {

    @Mapping(target = "districtIds", expression = "java(getDistrictIds(pollingStation))")
    @Mapping(target = "districtNames", expression = "java(getDistrictNames(pollingStation))")
    @Mapping(target = "voteCount", expression = "java(pollingStation.getVotes() != null ? pollingStation.getVotes().size() : 0)")
    @Mapping(target = "utilizationRate", ignore = true) // This will be calculated in the service
    PollingStationDTO toDto(PollingStation pollingStation);

    List<PollingStationDTO> toDto(List<PollingStation> pollingStations);

    @Mapping(target = "districts", ignore = true)
    @Mapping(target = "votes", ignore = true)
    PollingStation toEntity(PollingStationDTO pollingStationDTO);

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "districts", ignore = true)
    @Mapping(target = "votes", ignore = true)
    void partialUpdate(@MappingTarget PollingStation entity, PollingStationDTO dto);

    default List<Long> getDistrictIds(PollingStation pollingStation) {
        return pollingStation.getDistricts() != null
                ? pollingStation.getDistricts().stream()
                    .map(District::getId)
                    .collect(Collectors.toList())
                : null;
    }

    default List<String> getDistrictNames(PollingStation pollingStation) {
        return pollingStation.getDistricts() != null
                ? pollingStation.getDistricts().stream()
                    .map(District::getName)
                    .collect(Collectors.toList())
                : null;
    }
}