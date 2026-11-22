package com.chernyugov.trading.service.liquidity;

import com.chernyugov.trading.entity.CandleDomainEntity;
import com.chernyugov.trading.entity.LiquidityGuardEventEntity;
import com.chernyugov.trading.repository.LiquidityGuardEventRepository;
import com.chernyugov.trading.service.dictionary.InstrumentService;
import com.chernyugov.trading.service.tbank.ITinkoffCommonAPI;
import com.chernyugov.trading.strategy.AStrategy;
import com.chernyugov.trading.strategy.LiquidityAwareStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.Order;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiquidityGuardService {
    private final ITinkoffCommonAPI tinkoffCommonAPI;
    private final InstrumentService instrumentService;
    private final LiquidityGuardEventRepository eventRepository;

    @Value("${liquidity.guard.enabled:true}")
    private boolean enabled;

    @Value("${liquidity.guard.fail-open:false}")
    private boolean failOpen;

    @Value("${liquidity.guard.depth:10}")
    private int depth;

    @Value("${liquidity.guard.max-spread-percent:0.30}")
    private BigDecimal defaultMaxSpreadPercent;

    @Value("${liquidity.guard.max-slippage-percent:0.50}")
    private BigDecimal defaultMaxSlippagePercent;

    public LiquidityDecision check(CandleDomainEntity candle, AStrategy strategy, TradeSide side) {
        var decision = doCheck(candle, strategy, side);
        saveEvent(candle, strategy, side, decision);
        return decision;
    }

    private LiquidityDecision doCheck(CandleDomainEntity candle, AStrategy strategy, TradeSide side) {
        if (!enabled) {
            return LiquidityDecision.allowed("Liquidity Guard disabled");
        }
        if (strategy instanceof LiquidityAwareStrategy aware && !aware.isLiquidityGuardEnabled()) {
            return LiquidityDecision.allowed("Liquidity Guard disabled for strategy");
        }

        var maxSpreadPercent = getMaxSpreadPercent(strategy);
        var maxSlippagePercent = getMaxSlippagePercent(strategy);

        try {
            var instrument = instrumentService.getInstrument(candle.getFigi());
            var requiredLots = calculateRequiredLots(strategy, candle.getFigi(), instrument);
            var minAvailableLots = getMinAvailableLots(strategy, requiredLots);

            var orderBook = tinkoffCommonAPI.getApi()
                    .getMarketDataService()
                    .getOrderBookSync(candle.getFigi(), depth);

            var bids = orderBook.getBidsList();
            var asks = orderBook.getAsksList();
            if (bids.isEmpty() || asks.isEmpty()) {
                return baseDecision(false, "Order book is empty", candle, null, null, null, null, 0L, requiredLots);
            }

            var bestBid = toBigDecimal(bids.stream()
                    .max(Comparator.comparing(o -> toBigDecimal(o.getPrice())))
                    .orElseThrow().getPrice());
            var bestAsk = toBigDecimal(asks.stream()
                    .min(Comparator.comparing(o -> toBigDecimal(o.getPrice())))
                    .orElseThrow().getPrice());

            var spreadPercent = bestAsk.subtract(bestBid)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(bestBid.add(bestAsk).divide(BigDecimal.valueOf(2), 8, RoundingMode.HALF_UP), 4, RoundingMode.HALF_UP);

            var executionPrice = side == TradeSide.BUY ? bestAsk : bestBid;
            var slippagePercent = calculateSlippagePercent(side, candle.getClosingPrice(), executionPrice);
            var availableLots = calculateAvailableLots(side == TradeSide.BUY ? asks : bids);

            if (spreadPercent.compareTo(maxSpreadPercent) > 0) {
                return baseDecision(false, "Spread is too wide", candle, bestBid, bestAsk, spreadPercent,
                        slippagePercent, availableLots, requiredLots);
            }
            if (slippagePercent.compareTo(maxSlippagePercent) > 0) {
                return baseDecision(false, "Slippage is too high", candle, bestBid, bestAsk, spreadPercent,
                        slippagePercent, availableLots, requiredLots);
            }
            if (availableLots < minAvailableLots) {
                return baseDecision(false, "Not enough volume in order book", candle, bestBid, bestAsk, spreadPercent,
                        slippagePercent, availableLots, requiredLots);
            }

            return baseDecision(true, "Liquidity checks passed", candle, bestBid, bestAsk, spreadPercent,
                    slippagePercent, availableLots, requiredLots);
        } catch (Exception e) {
            log.error("Liquidity guard check failed for {}, {}, {}", candle.getFigi(), strategy.getName(), side, e);
            return failOpen
                    ? LiquidityDecision.allowed("Liquidity check failed, fail-open mode")
                    : LiquidityDecision.blocked("Liquidity check failed: " + e.getMessage());
        }
    }

    private LiquidityDecision baseDecision(boolean allowed, String reason, CandleDomainEntity candle,
                                           BigDecimal bestBid, BigDecimal bestAsk, BigDecimal spreadPercent,
                                           BigDecimal slippagePercent, Long availableLots, Long requiredLots) {
        return LiquidityDecision.builder()
                .allowed(allowed)
                .reason(reason)
                .expectedPrice(candle.getClosingPrice())
                .bestBid(bestBid)
                .bestAsk(bestAsk)
                .spreadPercent(spreadPercent)
                .slippagePercent(slippagePercent)
                .availableLots(availableLots)
                .requiredLots(requiredLots)
                .details(Map.of(
                        "depth", depth,
                        "maxSpreadPercent", defaultMaxSpreadPercent,
                        "maxSlippagePercent", defaultMaxSlippagePercent
                ))
                .build();
    }

    private void saveEvent(CandleDomainEntity candle, AStrategy strategy, TradeSide side, LiquidityDecision decision) {
        try {
            var instrument = instrumentService.getInstrument(candle.getFigi());
            eventRepository.save(LiquidityGuardEventEntity.builder()
                    .eventDateTime(candle.getDateTime())
                    .figi(candle.getFigi())
                    .figiTitle(instrument == null ? candle.getFigi() : instrument.getName())
                    .strategy(strategy.getName())
                    .side(side.name())
                    .allowed(decision.isAllowed())
                    .reason(decision.getReason())
                    .expectedPrice(decision.getExpectedPrice())
                    .bestBid(decision.getBestBid())
                    .bestAsk(decision.getBestAsk())
                    .spreadPercent(decision.getSpreadPercent())
                    .slippagePercent(decision.getSlippagePercent())
                    .availableLots(decision.getAvailableLots())
                    .requiredLots(decision.getRequiredLots())
                    .details(decision.getDetails())
                    .build());
        } catch (Exception e) {
            log.error("Failed to save liquidity guard event", e);
        }
    }

    private long calculateRequiredLots(AStrategy strategy, String figi, InstrumentService.Instrument instrument) {
        var count = strategy.getCount(figi);
        if (count == null || count <= 0) {
            return 1L;
        }
        if (instrument == null || instrument.getLot() <= 0) {
            return count;
        }
        return Math.max(1L, count / instrument.getLot());
    }

    private long getMinAvailableLots(AStrategy strategy, long requiredLots) {
        if (strategy instanceof LiquidityAwareStrategy aware && aware.getMinAvailableLots() != null) {
            return aware.getMinAvailableLots();
        }
        return requiredLots;
    }

    private BigDecimal getMaxSpreadPercent(AStrategy strategy) {
        if (strategy instanceof LiquidityAwareStrategy aware && aware.getMaxSpreadPercent() != null) {
            return aware.getMaxSpreadPercent();
        }
        return defaultMaxSpreadPercent;
    }

    private BigDecimal getMaxSlippagePercent(AStrategy strategy) {
        if (strategy instanceof LiquidityAwareStrategy aware && aware.getMaxSlippagePercent() != null) {
            return aware.getMaxSlippagePercent();
        }
        return defaultMaxSlippagePercent;
    }

    private BigDecimal calculateSlippagePercent(TradeSide side, BigDecimal expectedPrice, BigDecimal executionPrice) {
        var diff = side == TradeSide.BUY
                ? executionPrice.subtract(expectedPrice)
                : expectedPrice.subtract(executionPrice);
        if (diff.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return diff.multiply(BigDecimal.valueOf(100)).divide(expectedPrice, 4, RoundingMode.HALF_UP);
    }

    private Long calculateAvailableLots(List<Order> orders) {
        return orders.stream().mapToLong(Order::getQuantity).sum();
    }

    private BigDecimal toBigDecimal(Quotation value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value.getUnits()).add(BigDecimal.valueOf(value.getNano(), 9));
    }
}
