package com.braidsbeautyByAngie.repository;

import com.braidsbeautyByAngie.entity.ShopOrderEntity;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopOrderRepository extends JpaRepository<ShopOrderEntity, Long> {
    @Query("SELECT so FROM ShopOrderEntity so WHERE so.companyId = :companyId")
    Page<ShopOrderEntity> findAllByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
}
