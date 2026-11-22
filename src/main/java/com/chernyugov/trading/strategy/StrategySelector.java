package com.chernyugov.trading.strategy;

import com.chernyugov.trading.service.strategy.StrategyConfigService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class StrategySelector {
    private final List<AStrategy> staticStrategies;
    private final StrategyConfigService strategyConfigService;

    public List<AStrategy> suitableByFigi(String figi, AStrategy.Type type) {
        return getActiveStrategies().stream()
                .filter(AStrategy::isEnabled)
                .filter(s -> type == null || s.getType() == type)
                .filter(s -> s.isSuitableByFigi(figi))
                .collect(Collectors.toList());
    }

    public boolean isEnabled(String name, String figi) {
        return getActiveStrategies().stream()
                .filter(s -> s.getName().equals(name))
                .filter(s -> figi == null || s.isSuitableByFigi(figi))
                .map(AStrategy::isEnabled)
                .findFirst().orElse(false);
    }

    public List<AStrategy> getAllStrategies() {
        return Stream.concat(staticStrategies.stream(), strategyConfigService.buildStrategies().stream())
                .peek(this::validateStrategy)
                .collect(Collectors.toList());
    }

    public List<AStrategy> getActiveStrategies() {
        return getAllStrategies().stream()
                .filter(AStrategy::isEnabled)
                .collect(Collectors.toList());
    }

    public Set<String> getFigiesForActiveStrategies() {
        return getActiveStrategies().stream().flatMap(s -> s.getFigies().keySet().stream()).collect(Collectors.toSet());
    }

    public AStrategy.Type getStrategyType(String name, AStrategy.Type defaultValue) {
        return getAllStrategies().stream()
                .filter(s -> s.getName().equals(name))
                .findFirst().map(AStrategy::getType).orElse(defaultValue);
    }

    @PostConstruct
    private void init() {
        getActiveStrategies().forEach(s -> log.info("Enabled strategy: {}: {}", s.getName(), s.getFigies().keySet()));
    }

    private void validateStrategy(AStrategy strategy) {
        if (strategy.getType() == AStrategy.Type.instrumentByInstrument && strategy.getFigies().size() < 2
                || strategy.getType() == AStrategy.Type.instrumentByFiat && strategy.getFigies().size() < 1) {
            throw new UnsupportedOperationException("Incorrect count of figies in " + strategy.getName());
        }
    }
}
