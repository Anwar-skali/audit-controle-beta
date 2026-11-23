package com.audit.platform.supplier.mapper;

import com.audit.platform.supplier.dto.request.SupplierRequestDTO;
import com.audit.platform.supplier.dto.response.SupplierResponseDTO;
import com.audit.platform.supplier.entity.Supplier;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Supplier toEntity(SupplierRequestDTO dto);

    SupplierResponseDTO toResponseDTO(Supplier entity);

    List<SupplierResponseDTO> toResponseDTOList(List<Supplier> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDTO(SupplierRequestDTO dto, @MappingTarget Supplier entity);
}

