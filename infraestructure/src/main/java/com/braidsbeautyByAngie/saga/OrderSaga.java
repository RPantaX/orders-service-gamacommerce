package com.braidsbeautyByAngie.saga;
import com.braidsbeautyByAngie.aggregates.types.ShopOrderHistoryStatusEnum;
import com.braidsbeautyByAngie.ports.in.ShopOrderHistoryServiceIn;
import pe.com.gamacommerce.corelibraryservicegamacommerce.aggregates.aggregates.commands.*;
import pe.com.gamacommerce.corelibraryservicegamacommerce.aggregates.aggregates.dto.PaymentType;
import pe.com.gamacommerce.corelibraryservicegamacommerce.aggregates.aggregates.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.time.LocalTime;

@Component
@KafkaListener(topics = {"${orders.events.topic.name}", "${products.events.topic.name}", "${payments.events.topic.name}"})
@RequiredArgsConstructor
@Slf4j
public class OrderSaga {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ShopOrderHistoryServiceIn service;
    @Value("${products.commands.topic.name}")
    private String productsCommandsTopicName;

    @Value("${payments.commands.topic.name}")
    private String paymentsCommandsTopicName;

    @Value("${orders.commands.topic.name}")
    private String ordersCommandsTopicName;

    @KafkaHandler
    public void handleEvent(@Payload OrderCreatedEvent event){
        log.info("Received OrderCreatedEvent: {}", event);
        ReserveProductCommand command = ReserveProductCommand.builder()
                .requestProductsEventList(event.getRequestProductsEventList())
                .shopOrderId(event.getShopOrderId())
                .build();
        try {
            kafkaTemplate.send(productsCommandsTopicName, command);
            log.info("Sent ReserveProductCommand to topic {}: {}", productsCommandsTopicName, command);
        } catch (Exception e) {
            log.error("Failed to send ReserveProductCommand: {}", command, e);
            throw e; // Deja que el error se maneje en niveles superiores
        }

        service.addIn(event.getShopOrderId(), ShopOrderHistoryStatusEnum.CREATED);
        log.info("Order history updated with status: {}", ShopOrderHistoryStatusEnum.CREATED);
    }

    @KafkaHandler
    public void handleEvent(@Payload ProductReservedEvent event){
        log.info("Received ProductReservedEvent: {}", event);
            try {
                ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                        .shopOrderId(event.getShopOrderId())
                        .paymentProvider("PAYPAL")
                        .userId(1L)
                        .paymentAccountNumber(BigInteger.valueOf(123456789))
                        .paymentExpirationDate(LocalTime.parse("18:11:20"))
                        .paymentType(PaymentType.valueOf("CREDIT_CARD"))
                        .productList(event.getProductList())
                        .build();
                kafkaTemplate.send(paymentsCommandsTopicName, processPaymentCommand);
                log.info("Sent ProcessPaymentCommand to topic {}: {}", paymentsCommandsTopicName, processPaymentCommand);
            } catch (Exception e) {
                log.error("Error handling ServiceReservedEvent: {}", event, e);
            }

    }

    @KafkaHandler
    public void handleEvent(@Payload PaymentProcessedEvent event){
        log.info("Received PaymentProcessedEvent: {}", event);
        try {
            ApproveOrderCommand approveOrderCommand = ApproveOrderCommand.builder()
                    .shopOrderId(event.getShopOrderId())
                    .paymentTotalPrice(event.getPaymentTotalPrice())
                    .isProduct(event.isProduct())
                    .build();
            kafkaTemplate.send(ordersCommandsTopicName, approveOrderCommand);
            log.info("Sent ApproveOrderCommand to topic {}: {}", ordersCommandsTopicName, approveOrderCommand);
        } catch (Exception e) {
            log.error("Error handling PaymentProcessedEvent: {}", event, e);
        }
    }
    @KafkaHandler
    public void handleEvent(@Payload OrderApprovedEvent event){
        log.info("Received OrderApprovedEvent: {}", event);
        try {
                service.addIn(event.getShopOrderId(), ShopOrderHistoryStatusEnum.APPROVED_PRODUCT);
                log.info("Order approved for product only.");
        } catch (Exception e) {
            log.error("Error handling OrderApprovedEvent: {}", event, e);
        }
    }
    @KafkaHandler
    public void handleEvent(@Payload PaymentFailedEvent event){
        log.info("Received PaymentFailedEvent: {}", event);
        try {
            CancelProductReservationCommand cancelProductReservationCommand = CancelProductReservationCommand.builder()
                    .shopOrderId(event.getShopOrderId())
                    .productList(event.getProductList())
                    .build();
            kafkaTemplate.send(productsCommandsTopicName, cancelProductReservationCommand);
        } catch (Exception e) {
            log.error("Error handling PaymentFailedEvent: {}", event, e);
        }
    }
    @KafkaHandler
    public void handleEvent(@Payload ProductReservationCancelledEvent event){
        log.info("Received ProductReservationCancelledEvent: {}", event);
        try {
            RejectOrderCommand rejectOrderCommand = RejectOrderCommand.builder()
                    .shopOrderId(event.getShopOrderId())
                    .build();
            kafkaTemplate.send(ordersCommandsTopicName, rejectOrderCommand);
            service.addIn(event.getShopOrderId(), ShopOrderHistoryStatusEnum.REJECTED);
            log.info("Sent RejectOrderCommand to topic {}: {}", ordersCommandsTopicName, rejectOrderCommand);
        } catch (Exception e) {
            log.error("Error handling ProductAndServiceReservationCancelledEvent: {}", event, e);
        }
    }
    @KafkaHandler
    public void handleEvent(@Payload ProductReservationFailedEvent event){
        log.info("Received ProductReservationFailedEvent: {}", event);
        try {
            RejectOrderCommand rejectOrderCommand = RejectOrderCommand.builder()
                    .shopOrderId(event.getShopOrderId())
                    .build();
            kafkaTemplate.send(ordersCommandsTopicName, rejectOrderCommand);
            service.addIn(event.getShopOrderId(), ShopOrderHistoryStatusEnum.REJECTED);
            log.info("Sent RejectOrderCommand to topic {}: {}", ordersCommandsTopicName, rejectOrderCommand);
        } catch (Exception e) {
            log.error("Error handling ProductReservationFailedEvent: {}", event, e);
        }

    }
}
