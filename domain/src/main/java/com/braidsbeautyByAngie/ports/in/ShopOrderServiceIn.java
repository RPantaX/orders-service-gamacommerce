package com.braidsbeautyByAngie.ports.in;

import com.braidsbeautyByAngie.aggregates.dto.ShopOrderDTO;
import com.braidsbeautyByAngie.aggregates.request.RequestShopOrder;
import com.braidsbeautyByAngie.aggregates.response.ResponseListPageableShopOrder;
import com.braidsbeautyByAngie.aggregates.response.ResponseShopOrderDetail;

import java.math.BigDecimal;

public interface ShopOrderServiceIn {

    void rejectShopOrderIn(Long orderId);
    void aprovedShopOrderIn(Long orderId, BigDecimal paymentTotalPrice , boolean isProduct);
    ShopOrderDTO createShopOrderIn(RequestShopOrder requestShopOrder);
    ResponseListPageableShopOrder getShopOrderListIn(int pageNumber, int pageSize, String orderBy, String sortDir);
    ResponseListPageableShopOrder getShopOrderListByCompanyIdIn(int pageNumber, int pageSize, String orderBy, String sortDir, Long companyId);

    ResponseShopOrderDetail findShopOrderByIdIn(Long orderId);
}
