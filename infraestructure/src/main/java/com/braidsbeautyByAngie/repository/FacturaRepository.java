package com.braidsbeautyByAngie.repository;

import com.braidsbeautyByAngie.entity.CdpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaRepository extends JpaRepository<CdpEntity, Long> {
}
