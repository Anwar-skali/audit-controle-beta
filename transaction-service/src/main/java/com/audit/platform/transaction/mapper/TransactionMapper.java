package com.audit.platform.transaction.mapper;

import com.audit.platform.transaction.dto.request.TransactionRequestDTO;
import com.audit.platform.transaction.dto.response.TransactionResponseDTO;
import com.audit.platform.transaction.entity.Transaction;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "id", ignore = true)
    Transaction toEntity(TransactionRequestDTO dto);

    TransactionResponseDTO toResponseDTO(Transaction entity);

    List<TransactionResponseDTO> toResponseDTOList(List<Transaction> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDTO(TransactionRequestDTO dto, @MappingTarget Transaction entity);
}

