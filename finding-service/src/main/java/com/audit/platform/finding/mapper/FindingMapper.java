package com.audit.platform.finding.mapper;

import com.audit.platform.finding.dto.request.FindingRequestDTO;
import com.audit.platform.finding.dto.response.FindingResponseDTO;
import com.audit.platform.finding.entity.Finding;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FindingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Finding toEntity(FindingRequestDTO dto);

    FindingResponseDTO toResponseDTO(Finding entity);

    List<FindingResponseDTO> toResponseDTOList(List<Finding> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDTO(FindingRequestDTO dto, @MappingTarget Finding entity);
}

