package com.chernyugov.trading.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "liquidity_guard_event",
        indexes = {
                @Index(columnList = "figi"),
                @Index(columnList = "strategy"),
                @Index(columnList = "allowed"),
                @Index(columnList = "event_date_time")
        })
public class LiquidityGuardEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_date_time", nullable = false)
    private OffsetDateTime eventDateTime;

    @Column(nullable = false)
    private String figi;

    @Column(name = "figi_title")
    private String figiTitle;

    @Column(nullable = false)
    private String strategy;

    @Column(nullable = false)
    private String side;

    @Column(nullable = false)
    private Boolean allowed;

    @Column(nullable = false)
    private String reason;

    @Column(name = "expected_price", scale = 8, precision = 19)
    private BigDecimal expectedPrice;

    @Column(name = "best_bid", scale = 8, precision = 19)
    private BigDecimal bestBid;

    @Column(name = "best_ask", scale = 8, precision = 19)
    private BigDecimal bestAsk;

    @Column(name = "spread_percent", scale = 4, precision = 10)
    private BigDecimal spreadPercent;

    @Column(name = "slippage_percent", scale = 4, precision = 10)
    private BigDecimal slippagePercent;

    @Column(name = "available_lots")
    private Long availableLots;

    @Column(name = "required_lots")
    private Long requiredLots;

    @Type(JsonType.class)
    @Column(name = "details", columnDefinition = "jsonb")
    private Map<String, Object> details;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime created;
}
