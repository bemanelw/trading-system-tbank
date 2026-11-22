package com.chernyugov.trading.entity;

import com.chernyugov.trading.strategy.AStrategy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "strategy_config",
        indexes = {
                @Index(columnList = "name", unique = true),
                @Index(columnList = "figi"),
                @Index(columnList = "enabled")
        })
public class StrategyConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AStrategy.Type type = AStrategy.Type.instrumentByFiat;

    @Column(nullable = false)
    private String figi;

    @Column(nullable = false)
    @Builder.Default
    private Integer count = 1;

    @Column(name = "history_days", nullable = false)
    @Builder.Default
    private Integer historyDays = 14;

    @Column(name = "buy_percentile", nullable = false)
    @Builder.Default
    private Integer buyPercentile = 10;

    @Column(name = "take_profit_percent", scale = 4, precision = 10)
    @Builder.Default
    private BigDecimal takeProfitPercent = new BigDecimal("1.0");

    @Column(name = "stop_loss_percent", scale = 4, precision = 10)
    @Builder.Default
    private BigDecimal stopLossPercent = new BigDecimal("3.0");

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "only_sell", nullable = false)
    @Builder.Default
    private Boolean onlySell = false;

    @Column(name = "liquidity_guard_enabled", nullable = false)
    @Builder.Default
    private Boolean liquidityGuardEnabled = true;

    @Column(name = "max_spread_percent", scale = 4, precision = 10)
    @Builder.Default
    private BigDecimal maxSpreadPercent = new BigDecimal("0.30");

    @Column(name = "max_slippage_percent", scale = 4, precision = 10)
    @Builder.Default
    private BigDecimal maxSlippagePercent = new BigDecimal("0.50");

    @Column(name = "min_available_lots")
    private Long minAvailableLots;

    @Version
    @Column(nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updated;
}
