package com.braidsbeautyByAngie.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "cdp", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cdp_serie", "cdp_serienumber"})
})
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CdpEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cdp_number", nullable = false)
    private Long cdpNumber;

    @Column(name = "cdp_serie", nullable = false, length = 10)
    private String cdpSerie;

    @Column(name = "cdp_serienumber", nullable = false, length = 20)
    private String cdpSerienumber;

    @Column(name = "cdp_emision", nullable = false)
    private Timestamp cdpEmision;

    @Column(name = "cdp_subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal cdpSubtotal;

    @Column(name = "cdp_igv", nullable = false, precision = 12, scale = 2)
    private BigDecimal cdpIgv;

    @Column(name = "cdp_discounts", nullable = false, precision = 12, scale = 2)
    private BigDecimal cdpDiscounts;

    @Column(name = "cdp_anticipios", nullable = false, precision = 12, scale = 2)
    private BigDecimal cdpAnticipios;

    @Column(name = "cdp_sell_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal cdpSellValue;

    @Column(name = "cdp_isc", precision = 12, scale = 2)
    private BigDecimal cdpIsc;

    @Column(name = "cdp_icbper", precision = 12, scale = 2)
    private BigDecimal cdpIcbper;

    @Column(name = "cdp_other_charges", precision = 12, scale = 2)
    private BigDecimal cdpOtherCharges;

    @Column(name = "cdp_other_taxes", precision = 12, scale = 2)
    private BigDecimal cdpOtherTaxes;

    @Column(name = "cdp_rounded_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal cdpRoundedAmount;

    @Column(name = "cdp_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal cdpTotal;

    @Column(name = "cdp_observation")
    private String cdpObservation;

    @Column(name = "cdp_issuer_ruc", nullable = false, length = 11)
    private String cdpIssuerRuc;

    @Column(name = "cdp_state", nullable = false)
    private Boolean cdpState;

    @ManyToOne(optional = true)
    @JoinColumn(name = "shop_order_id", referencedColumnName = "shop_order_id", foreignKey = @ForeignKey(name = "fk_shop_order_cdp"))
    private ShopOrderEntity shopOrder;

    @Column(name = "state", nullable = false)
    private Boolean state;

    @Column(name = "modified_by_user", nullable = false, length = 15)
    private String modifiedByUser;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "modified_at")
    private Timestamp modifiedAt;
}