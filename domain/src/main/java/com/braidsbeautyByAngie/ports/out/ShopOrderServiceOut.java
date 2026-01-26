package com.braidsbeautyByAngie.ports.out;

import com.braidsbeautyByAngie.aggregates.dto.ShopOrderDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestShopOrder;
import com.braidsbeautyByAngie.aggregates.response.ResponseListPageableShopOrder;
import com.braidsbeautyByAngie.aggregates.response.ResponseShopOrderDetail;

import java.math.BigDecimal;

public interface ShopOrderServiceOut {
    void rejectShopOrderOut(Long orderId);
    void aprovedShopOrderOut(Long orderId, BigDecimal paymentTotalPrice, boolean isProduct);
    ShopOrderDTO createShopOrderOut(RequestShopOrder requestShopOrder, Long companyId);
    ResponseListPageableShopOrder getShopOrderListOut(int pageNumber, int pageSize, String orderBy, String sortDir);
    ResponseListPageableShopOrder getShopOrderListByCompanyIdOut(int pageNumber, int pageSize, String orderBy, String sortDir, Long companyId);
    ResponseShopOrderDetail findShopOrderByIdOut(Long orderId) ;
}
