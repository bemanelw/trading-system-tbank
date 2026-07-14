package com.chernyugov.trading.strategy.mean_reversion_strategy;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BuyP90AndTP02PercentAndSL02PercentStrategy extends AInstrumentByFiatStrategy {

    private static final Map<String, Integer> FIGIES = new ImmutableMap.Builder<String, Integer>()
            .put("BBG004S68BH6", 1)     // PIKK — ПИК
            .put("BBG004S68598", 1)     // MTLR — Мечел
            .put("TCS00A106YF0", 1)     // VKCO — VK
            .put("BBG000RMWQD4", 1)     // ENPG — En+ Group
            .put("BBG004S68614", 10)    // AFKS — АФК Система
            .build();

    @Override
    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public BuyCriteria getBuyCriteria() {
        return BuyCriteria.builder()
                .lessThenPercentile(90)
                .build();
    }

    @Override
    public SellCriteria getSellCriteria() {
        return SellCriteria.builder()
                .takeProfitPercent(0.2f)
                .stopLossPercent(0.2f)
                .build();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}