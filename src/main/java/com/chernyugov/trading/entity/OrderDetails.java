package com.chernyugov.trading.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetails {
    // Текущие рыночные цены инструментов стратегии
    Map<String, BigDecimal> currentPrices;
    // Цены продажи (для расчёта P&L)
    Map<String, BigDecimal> sellPrices;
}