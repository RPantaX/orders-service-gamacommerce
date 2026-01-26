package com.braidsbeautyByAngie.controller;

import com.braidsbeautyByAngie.aggregates.request.RequestShopOrder;
import com.braidsbeautyByAngie.ports.in.ShopOrderServiceIn;
import pe.com.gamacommerce.corelibraryservicegamacommerce.aggregates.aggregates.Constants;
import pe.com.gamacommerce.corelibraryservicegamacommerce.aggregates.aggregates.util.ApiResponse;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@OpenAPIDefinition(
        info = @Info(
                title = "API-ORDERS",
                version = "1.0",
                description = "Shop Order management"
        )
)
@RestController
@RequestMapping("/v1/orders-service/order")
@RequiredArgsConstructor
public class OrdersController {

    private final ShopOrderServiceIn service;

    @Operation(summary = "Get all orders")
    @RequestMapping("/list")
    public ResponseEntity<ApiResponse> listOrders(@RequestParam(value = "pageNo", defaultValue = Constants.NUM_PAG_BY_DEFECT, required = false) int pageNo,
                                                  @RequestParam(value = "pageSize", defaultValue = Constants.SIZE_PAG_BY_DEFECT, required = false) int pageSize,
                                                  @RequestParam(value = "sortBy", defaultValue = Constants.ORDER_BY_DEFECT_ALL, required = false) String sortBy,
                                                  @RequestParam(value = "sortDir", defaultValue = Constants.ORDER_DIRECT_BY_DEFECT, required = false) String sortDir){
        return ResponseEntity.ok(ApiResponse.ok("List of orders retrieved successfully",
                service.getShopOrderListIn(pageNo, pageSize, sortBy, sortDir)));
    }

    @Operation(summary = "Get all orders by company Id")
    @RequestMapping("/list/company/{companyId}")
    public ResponseEntity<ApiResponse> listOrdersbyCompanyId(@RequestParam(value = "pageNo", defaultValue = Constants.NUM_PAG_BY_DEFECT, required = false) int pageNo,
                                                  @RequestParam(value = "pageSize", defaultValue = Constants.SIZE_PAG_BY_DEFECT, required = false) int pageSize,
                                                  @RequestParam(value = "sortBy", defaultValue = Constants.ORDER_BY_DEFECT_ALL, required = false) String sortBy,
                                                  @RequestParam(value = "sortDir", defaultValue = Constants.ORDER_DIRECT_BY_DEFECT, required = false) String sortDir,
                                                             @PathVariable(name = "companyId") Long companyId){
        return ResponseEntity.ok(ApiResponse.ok("List of orders retrieved successfully",
                service.getShopOrderListByCompanyIdIn(pageNo, pageSize, sortBy, sortDir, companyId)));
    }

    @Operation(summary = "Generate order")
    @RequestMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse> generateOrder(@RequestBody RequestShopOrder requestShopOrder, @PathVariable(name = "companyId") Long companyId){
        return new ResponseEntity<>(ApiResponse.create("Order created", service.createShopOrderIn(requestShopOrder, companyId)), HttpStatus.CREATED);
    }

    @GetMapping(value = "/{shopOrderId}")
    public ResponseEntity<ApiResponse> getShopOrderById(@PathVariable(name = "shopOrderId") Long shopOrderId){
        return ResponseEntity.ok(ApiResponse.ok("Shop order retrieved successfully",
                service.findShopOrderByIdIn(shopOrderId)));
    }
}