package com.chernyugov.trading.service.strategy;

import com.chernyugov.trading.entity.StrategyConfigEntity;
import com.chernyugov.trading.strategy.LiquidityAwareStrategy;
import com.chernyugov.trading.strategy.mean_reversion_strategy.AInstrumentByFiatStrategy;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@RequiredArgsConstructor
public class ConfigurableInstrumentByFiatStrategy extends AInstrumentByFiatStrategy implements LiquidityAwareStrategy {
    private final StrategyConfigEntity config;

    public Long getConfigId() {
        return config.getId();
    }

    @Override
    public Map<String, Integer> getFigies() {
        return Map.of(config.getFigi(), config.getCount());
    }

    @Override
    public String getName() {
        return config.getName();
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(config.getEnabled());
    }

    @Override
    public boolean isOnlySell() {
        return Boolean.TRUE.equals(config.getOnlySell());
    }

    @Override
    public Duration getHistoryDuration() {
        return Duration.ofDays(config.getHistoryDays());
    }

    @Override
    public BuyCriteria getBuyCriteria() {
        return BuyCriteria.builder()
                .lessThenPercentile(config.getBuyPercentile())
                .build();
    }

    @Override
    public SellCriteria getSellCriteria() {
        return SellCriteria.builder()
                .takeProfitPercent(toFloat(config.getTakeProfitPercent()))
                .stopLossPercent(toFloat(config.getStopLossPercent()))
                .build();
    }

    @Override
    public boolean isLiquidityGuardEnabled() {
        return Boolean.TRUE.equals(config.getLiquidityGuardEnabled());
    }

    @Override
    public BigDecimal getMaxSpreadPercent() {
        return config.getMaxSpreadPercent();
    }

    @Override
    public BigDecimal getMaxSlippagePercent() {
        return config.getMaxSlippagePercent();
    }

    @Override
    public Long getMinAvailableLots() {
        return config.getMinAvailableLots();
    }

    private Float toFloat(BigDecimal value) {
        return value == null ? null : value.floatValue();
    }
}
