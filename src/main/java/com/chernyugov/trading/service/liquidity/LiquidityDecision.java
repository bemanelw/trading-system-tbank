package com.chernyugov.trading.service.liquidity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class LiquidityDecision {
    private boolean allowed;
    private String reason;
    private BigDecimal expectedPrice;
    private BigDecimal bestBid;
    private BigDecimal bestAsk;
    private BigDecimal spreadPercent;
    private BigDecimal slippagePercent;
    private Long availableLots;
    private Long requiredLots;
    private Map<String, Object> details;

    public static LiquidityDecision allowed(String reason) {
        return LiquidityDecision.builder()
                .allowed(true)
                .reason(reason)
                .build();
    }

    public static LiquidityDecision blocked(String reason) {
        return LiquidityDecision.builder()
                .allowed(false)
                .reason(reason)
                .build();
    }
}
