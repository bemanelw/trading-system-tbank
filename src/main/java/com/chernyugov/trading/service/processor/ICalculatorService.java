package com.chernyugov.trading.service.processor;

import com.chernyugov.trading.entity.CandleDomainEntity;
import com.chernyugov.trading.strategy.AStrategy;

import java.math.BigDecimal;

interface ICalculatorService<T extends AStrategy> {
    boolean isShouldBuy(T strategy, CandleDomainEntity candle);

    boolean isShouldSell(T strategy, CandleDomainEntity candle, BigDecimal purchasePrice);

    AStrategy.Type getStrategyType();
}

