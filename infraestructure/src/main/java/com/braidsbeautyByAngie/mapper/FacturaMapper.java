package com.braidsbeautyByAngie.mapper;

import com.braidsbeautyByAngie.aggregates.dto.FacturaDTO;
import com.braidsbeautyByAngie.entity.CdpEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class FacturaMapper {
    private static final ModelMapper modelMapper = new ModelMapper();

    public CdpEntity mapToEntity(CdpEntity cdpEntity) {
        return modelMapper.map(cdpEntity, CdpEntity.class);
    }
    public FacturaDTO mapToDTO(FacturaDTO facturaDTO) {
        return modelMapper.map(facturaDTO, FacturaDTO.class);
    }
}
