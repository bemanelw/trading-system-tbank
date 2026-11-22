package com.chernyugov.trading.service.strategy;

import com.chernyugov.trading.entity.StrategyConfigEntity;
import com.chernyugov.trading.repository.StrategyConfigRepository;
import com.chernyugov.trading.strategy.AStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StrategyConfigService {
    private final StrategyConfigRepository repository;

    public List<StrategyConfigEntity> findAllConfigs() {
        return repository.findAllByOrderByNameAsc();
    }

    public StrategyConfigEntity findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Strategy config not found: " + id));
    }

    public List<AStrategy> buildStrategies() {
        return findAllConfigs().stream()
                .filter(c -> c.getType() == AStrategy.Type.instrumentByFiat)
                .map(ConfigurableInstrumentByFiatStrategy::new)
                .map(AStrategy.class::cast)
                .toList();
    }

    public StrategyConfigEntity newDefaultConfig() {
        return StrategyConfigEntity.builder()
                .type(AStrategy.Type.instrumentByFiat)
                .count(1)
                .historyDays(14)
                .buyPercentile(10)
                .takeProfitPercent(new BigDecimal("1.0"))
                .stopLossPercent(new BigDecimal("3.0"))
                .enabled(true)
                .onlySell(false)
                .liquidityGuardEnabled(true)
                .maxSpreadPercent(new BigDecimal("0.30"))
                .maxSlippagePercent(new BigDecimal("0.50"))
                .build();
    }

    @Transactional
    public StrategyConfigEntity save(StrategyConfigEntity entity) {
        validate(entity);
        return repository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void validate(StrategyConfigEntity entity) {
        if (!StringUtils.hasText(entity.getName())) {
            throw new IllegalArgumentException("Strategy name is required");
        }
        if (!StringUtils.hasText(entity.getFigi())) {
            throw new IllegalArgumentException("FIGI is required");
        }
        if (entity.getCount() == null || entity.getCount() <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        if (entity.getHistoryDays() == null || entity.getHistoryDays() <= 0) {
            throw new IllegalArgumentException("History days must be positive");
        }
        if (entity.getBuyPercentile() == null || entity.getBuyPercentile() < 0 || entity.getBuyPercentile() > 100) {
            throw new IllegalArgumentException("Buy percentile must be between 0 and 100");
        }
        if (entity.getTakeProfitPercent() == null && entity.getStopLossPercent() == null) {
            throw new IllegalArgumentException("At least one sell criterion is required");
        }
        if (entity.getMaxSpreadPercent() == null) {
            entity.setMaxSpreadPercent(new BigDecimal("0.30"));
        }
        if (entity.getMaxSlippagePercent() == null) {
            entity.setMaxSlippagePercent(new BigDecimal("0.50"));
        }
        if (entity.getEnabled() == null) {
            entity.setEnabled(false);
        }
        if (entity.getOnlySell() == null) {
            entity.setOnlySell(false);
        }
        if (entity.getLiquidityGuardEnabled() == null) {
            entity.setLiquidityGuardEnabled(false);
        }
        if (entity.getType() == null) {
            entity.setType(AStrategy.Type.instrumentByFiat);
        }
    }
}
