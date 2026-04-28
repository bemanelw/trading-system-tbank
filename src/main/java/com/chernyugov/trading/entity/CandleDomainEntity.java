package com.chernyugov.trading.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "candle",
        indexes = {
                @Index(columnList = "figi"),
                @Index(columnList = "date_time"),
                @Index(columnList = "interval"),
                @Index(columnList = "date_time, figi, interval", unique = true)
        })
public class CandleDomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ключ идентификации инструмента
    @Column(name = "figi", nullable = false)
    private String figi;

    // OHLC цены свечи
    @Column(name = "highest_price", nullable = false, scale = 8, precision = 19)
    private BigDecimal highestPrice;

    @Column(name = "lowest_price", nullable = false, scale = 8, precision = 19)
    private BigDecimal lowestPrice;

    @Column(name = "open_price", nullable = false, scale = 8, precision = 19)
    private BigDecimal openPrice;

    @Column(name = "closing_price", nullable = false, scale = 8, precision = 19)
    private BigDecimal closingPrice;

    // Временная метка
    @Column(name = "date_time", nullable = false)
    private OffsetDateTime dateTime;

    // интервал (1min, 1hour, 1day)
    @Column(name = "interval", nullable = false)
    private String interval;

    @Version
    private Integer version;
}
