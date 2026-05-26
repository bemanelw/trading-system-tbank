package com.chernyugov.trading.controller;

import com.chernyugov.trading.entity.StrategyConfigEntity;
import com.chernyugov.trading.repository.LiquidityGuardEventRepository;
import com.chernyugov.trading.service.report.ReportService;
import com.chernyugov.trading.service.strategy.StrategyConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final ReportService reportService;
    private final StrategyConfigService strategyConfigService;
    private final LiquidityGuardEventRepository liquidityGuardEventRepository;

    @GetMapping(path = {"/", "/strategies"})
    public ModelAndView strategies() {
        var report = reportService.buildReportStrategiesInfo();
        return new ModelAndView("report_strategies", Map.of("reportStrategiesInfo", report));
    }

    @GetMapping(path = {"/strategies/create"})
    public ModelAndView createStrategy() {
        return new ModelAndView("strategy_form", Map.of(
                "strategyConfig", strategyConfigService.newDefaultConfig(),
                "isNew", true
        ));
    }

    @PostMapping(path = {"/strategies"})
    public String saveNewStrategy(@ModelAttribute StrategyConfigEntity strategyConfig) {
        strategyConfigService.save(strategyConfig);
        return "redirect:/strategies";
    }

    @GetMapping(path = {"/strategies/{id}/edit"})
    public ModelAndView editStrategy(@PathVariable Long id) {
        return new ModelAndView("strategy_form", Map.of(
                "strategyConfig", strategyConfigService.findById(id),
                "isNew", false
        ));
    }

    @PostMapping(path = {"/strategies/{id}"})
    public String updateStrategy(@PathVariable Long id, @ModelAttribute StrategyConfigEntity strategyConfig) {
        strategyConfig.setId(id);
        strategyConfigService.save(strategyConfig);
        return "redirect:/strategies";
    }

    @PostMapping(path = {"/strategies/{id}/delete"})
    public String deleteStrategy(@PathVariable Long id) {
        strategyConfigService.delete(id);
        return "redirect:/strategies";
    }

    @GetMapping(path = {"/orders"})
    public ModelAndView orders() {
        var orders = reportService.getOrdersSortByIdDesc();
        return new ModelAndView("report_orders", Map.of("orders", orders));
    }

    @GetMapping(path = {"/liquidity"})
    public ModelAndView liquidity() {
        var events = liquidityGuardEventRepository.findLastEvents();
        return new ModelAndView("report_liquidity", Map.of("liquidityEvents", events));
    }

    @GetMapping(path = {"/instrument_by_instrument"})
    public ModelAndView instrumentByInstrument() {
        var report = reportService.buildReportInstrumentByInstrument();
        return new ModelAndView("report_instrument_by_instrument", Map.of("reportInstrumentByInstrument", report));
    }

    @GetMapping(path = {"/instrument_by_fiat"})
    public ModelAndView instrumentByFiat() {
        var report = reportService.buildReportInstrumentByFiat();
        return new ModelAndView("report_instrument_by_fiat", Map.of("reportInstrumentByFiat", report));
    }
}
