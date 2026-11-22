package com.chernyugov.trading.strategy;

import java.math.BigDecimal;

public interface LiquidityAwareStrategy {
    boolean isLiquidityGuardEnabled();

    BigDecimal getMaxSpreadPercent();

    BigDecimal getMaxSlippagePercent();

    Long getMinAvailableLots();
}
